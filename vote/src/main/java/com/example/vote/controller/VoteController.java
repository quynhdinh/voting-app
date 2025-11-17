package com.example.vote.controller;
import com.example.vote.model.Vote;
import com.example.vote.service.VoteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.AllArgsConstructor;
import java.util.List;
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