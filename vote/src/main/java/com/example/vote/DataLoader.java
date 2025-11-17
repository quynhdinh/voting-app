package com.example.vote;
import com.example.vote.model.Vote;
import com.example.vote.integration.VoteProducer;
import com.example.vote.repository.VoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.vote.repository.UserVoteStateRepository;
import com.example.vote.service.VoteService;
import lombok.Data;
import lombok.AllArgsConstructor;

@Component
@Data
@AllArgsConstructor
public class DataLoader implements CommandLineRunner {
	private final VoteService voteService;
	private final VoteRepository voteRepository;
	private final UserVoteStateRepository userVoteStateRepository;
	private final VoteProducer voteProducer;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Loading initial data...");
		voteRepository.deleteAll();
		userVoteStateRepository.deleteAll();
		// Let the database generate IDs (GenerationType.IDENTITY). Do not set id manually.
		// let the service send to kafka!
		Vote vote1 = new Vote(null, 1L, 1L, "candidate1", System.currentTimeMillis());
		voteService.vote(vote1);
		Vote vote2 = new Vote(null, 1L, 2L, "candidate2", System.currentTimeMillis());
		voteService.vote(vote2);
	}
}


