package com.example.vote;

import javax.annotation.processing.Generated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Documented;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.stream.Collectors;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Repository;

@SpringBootApplication
public class VotingApplication {

	public static void main(String[] args) {
		SpringApplication.run(VotingApplication.class, args);
	}
	@Bean
	public NewTopic votesTopic() {
		return TopicBuilder.name("votes").partitions(1).replicas(1).build();
	}
}

@Repository
interface VoteRepository extends MongoRepository<Vote, String> {

}

@Repository
interface UserVoteStateRepository extends MongoRepository<User_Vote_State, String> {
	Optional<User_Vote_State> findByVoterIdAndContestId(String voterId, String contestId);
}

@Component
@Data
@AllArgsConstructor
class DataLoader implements CommandLineRunner {
	private final VoteService voteService;
	private final VoteRepository voteRepository;
	private final UserVoteStateRepository userVoteStateRepository;
	private final VoteProducer voteProducer;

	@Override
	public void run(String... args) throws Exception {
		voteRepository.deleteAll();
		userVoteStateRepository.deleteAll();
		// Let the database generate IDs (GenerationType.IDENTITY). Do not set id manually.
		Vote vote1 = new Vote(null, "contest1", "voter1", "candidate1", System.currentTimeMillis());
		voteService.vote(vote1);
		voteProducer.sendVote(vote1);
		Vote vote2 = new Vote(null, "contest1", "voter2", "candidate2", System.currentTimeMillis());
		voteService.vote(vote2);
		voteProducer.sendVote(vote2);
	}
}

@Service
@AllArgsConstructor
class VoteService {
	private final VoteRepository voteRepository;
	private final UserVoteStateRepository userVoteStateRepository;
	private final VoteProducer voteProducer;
	public List<Vote> getAllVotes() {
		return voteRepository.findAll();
	}
	public Vote vote(Vote vote) throws Exception {
		Optional<User_Vote_State> userVoteStateOpt = userVoteStateRepository.findByVoterIdAndContestId(vote.getVoterId(), vote.getContestId());
		String already = userVoteStateOpt.map(User_Vote_State::getCandidateIds).orElse("");
		Set<String> alreadySet = Set.of(already.split(","));
		Set<String> newCandidates = Set.of(vote.getCandidateIds().split(","));
		// if already voted for all of the new candidates, ignore
		Set<String> updatedCandidates = new HashSet<>(alreadySet);
		updatedCandidates.addAll(newCandidates);
		if(updatedCandidates.size() > alreadySet.size()) {
			String updatedCandidateIds = String.join(",", updatedCandidates);
			vote.setCandidateIds(updatedCandidateIds);
			voteRepository.save(vote);
			// send to kafka only the newly_added votes
			Vote new_added_votes = new Vote(vote.getId(), vote.getContestId(), vote.getVoterId(),
					newCandidates.stream().filter(c -> !alreadySet.contains(c)).collect(Collectors.joining(",")),
					vote.getCreatedAt());
			voteProducer.sendVote(new_added_votes);
			// if already saved, update, else save new
			userVoteStateRepository.save(new User_Vote_State(userVoteStateOpt.isEmpty() ? null : userVoteStateOpt.get().getId(), vote.getVoterId(), vote.getContestId(), updatedCandidateIds));
			return vote;
		}
		return null; // already voted for all candidates
	}
	public Vote unvote(Vote vote) throws Exception {
		Optional<User_Vote_State> userOpt = userVoteStateRepository.findById(vote.getVoterId());
		String already = userOpt.map(User_Vote_State::getCandidateIds).orElse("");
		Set<String> alreadySet = Set.of(already.split(","));
		Set<String> removeCandidates = Set.of(vote.getCandidateIds().split(","));
		// if haven't voted for any of the remove candidates, ignore
		Set<String> updatedCandidates = new HashSet<>(alreadySet);
		updatedCandidates.removeAll(removeCandidates);
		if(updatedCandidates.size() < alreadySet.size()) {
			String updatedCandidateIds = String.join(",", updatedCandidates);
			vote.setCandidateIds(updatedCandidateIds);
			voteRepository.deleteById(vote.getId());
			//send to kafka only unvote candidates
			Vote unvote_candidates = new Vote(vote.getId(), vote.getContestId(), vote.getVoterId(),
					removeCandidates.stream().filter(c -> alreadySet.contains(c)).collect(Collectors.joining(",")),
					vote.getCreatedAt());
			voteProducer.sendUnvote(unvote_candidates);
			// if already saved, update, else save new
			userVoteStateRepository.save(new User_Vote_State(userOpt.isEmpty() ? null : userOpt.get().getId(), vote.getVoterId(), vote.getContestId(), updatedCandidateIds));
			return vote;
		}
		return null; // haven't voted for any of the remove candidates
	}
}
@RestController
@RequestMapping("/votes")
@AllArgsConstructor
class VoteController {

	private final VoteService voteService;

	@GetMapping
	public List<Vote> getAllVotes() {
		return voteService.getAllVotes();
	}

	// you cannot unvote for a candidate, you can only vote for more candidate of a contest
	@PostMapping
	public Vote vote(@RequestBody Vote vote) throws Exception {
		return voteService.vote(vote);
	}
	@PostMapping("/unvote")
	public Vote unvote(@RequestBody Vote vote) throws Exception {
		return voteService.unvote(vote);
	}
}

@Component
@AllArgsConstructor
@Slf4j
class VoteProducer {
	private KafkaTemplate<String, String> kafkaTemplate;

	public void sendVote(Vote vote) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = objectMapper.writeValueAsString(vote);
		// send with different key to distribute across partitions
		// now not one consumer will get all messages
		kafkaTemplate.send("votes", String.valueOf(vote.getId()), writeValueAsString);
	}
	public void sendUnvote(Vote vote) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = objectMapper.writeValueAsString(vote);
		// send with different key to distribute across partitions
		// now not one consumer will get all messages
		kafkaTemplate.send("unvotes", String.valueOf(vote.getId()), writeValueAsString);
	}
}

@Document
@AllArgsConstructor
@Data
class Vote {
	@Id
	private String id;
	private String contestId;
	private String voterId;
	private String candidateIds;
	private Long createdAt;
}

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class User_Vote_State {
	@Id
	private String id;
	private String voterId;
	private String contestId;
	private String candidateIds;
}