package com.example.vote.service;
import com.example.vote.model.Vote;
import com.example.vote.model.User_Vote_State;
import com.example.vote.repository.VoteRepository;
import com.example.vote.repository.UserVoteStateRepository;
import com.example.vote.integration.VoteProducer;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class VoteService {
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