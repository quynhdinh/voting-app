package com.example.vote.controller;
import com.example.vote.model.Vote;
import com.example.vote.service.VoteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.example.vote.dto.VoteDTO;

@RestController
@RequestMapping("/votes")
@AllArgsConstructor
@Slf4j
class VoteController {

	private final VoteService voteService;

	@GetMapping
	public List<Vote> getAllVotes() {
		return voteService.getAllVotes();
	}

	// sample curl
	// curl -X POST http://localhost:8080/votes -H "Content-Type: application/json" -d '{"contestId": 1, "voterId": 1, "candidateId": 2}'
	@PostMapping
	public Vote vote(@RequestBody VoteDTO vote) throws Exception {
		log.info("Received vote request: {}", vote);
		return voteService.vote(vote);
	}
	@PostMapping("/unvote")
	public Vote unvote(@RequestBody VoteDTO vote) throws Exception {
		log.info("Received unvote request: {}", vote);
		return voteService.unvote(vote);
	}
}