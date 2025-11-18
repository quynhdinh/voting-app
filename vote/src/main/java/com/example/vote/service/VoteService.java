package com.example.vote.service;
import com.example.vote.model.Vote;
import com.example.vote.model.User_Vote_State;
import com.example.vote.repository.VoteRepository;
import com.example.vote.repository.UserVoteStateRepository;
import com.example.vote.dto.VoteDTO;
import com.example.vote.integration.VoteProducer;
import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VoteService {
	private final VoteRepository voteRepository;
	private final UserVoteStateRepository userVoteStateRepository;
	private final VoteProducer voteProducer;
	public List<Vote> getAllVotes() {
		return voteRepository.findAll();
	}
	public Vote vote(VoteDTO vote) throws Exception {
		// there is only one vote in vote.candidateId
		// check if user has already voted for this candidate in this contest
		// if yes ignore else save
		Optional<User_Vote_State> userOpt = userVoteStateRepository.findByVoterIdAndContestId(vote.getVoterId(), vote.getContestId());
		String already = userOpt.map(User_Vote_State::getCandidateIds).orElse("");
		Set<String> alreadySet = new HashSet<>(Arrays.asList(already.split(",")));
		if (!alreadySet.contains(String.valueOf(vote.getCandidateId()))) {

			alreadySet.add(String.valueOf(vote.getCandidateId()));
			String updated = String.join(",", alreadySet);
			User_Vote_State userVoteState = new User_Vote_State(userOpt.isPresent() ? userOpt.get().getId() : null, vote.getVoterId(), vote.getContestId(), updated);
			userVoteStateRepository.save(userVoteState);
			// save vote
			Vote newVote = new Vote(null, vote.getContestId(), vote.getVoterId(), vote.getCandidateId().toString(), System.currentTimeMillis());
			Vote savedVote = voteRepository.save(newVote);
			voteProducer.sendVote(vote);
			return savedVote;
		}
		return null; // already voted for this candidate
	}
	public Vote unvote(VoteDTO vote) throws Exception {
		return null;
	}

}