package com.example.vote.controller;
import com.example.vote.model.Vote;
import com.example.vote.service.VoteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

	// curl -X POST http://localhost:8080/votes -H "Content-Type: application/json" -d '{"contestId": 1, "voterId": 1, "candidateId": 2}'
	@PostMapping
	public ResponseEntity<VoteResponse> vote(@RequestBody VoteDTO vote) throws Exception {
		log.info("Received vote request: {}", vote);
		if (vote.getVoterId() <= 0){
			return ResponseEntity.badRequest().body(new VoteResponse("Invalid voter ID", null, null, null));
		}
		Optional<Vote> v = voteService.vote(vote);
		if (v.isEmpty()){
			return ResponseEntity.ok().body(new VoteResponse("User has already voted for this candidate in this contest", null, null, null));
		}
		return ResponseEntity.ok().body(new VoteResponse("Vote recorded successfully", v.get().getId(), v.get().getContestId(), v.get().getCandidateId()));
	}
	@PostMapping("/unvote")
	public ResponseEntity<VoteResponse> unvote(@RequestBody VoteDTO vote) throws Exception {
		log.info("Received unvote request: {}", vote);
		Optional<Vote> v = voteService.unvote(vote);
		if (v.isEmpty()){
			return ResponseEntity.ok().body(new VoteResponse("No existing vote found to unvote", null, null, null));
		}
		Vote voteObj = v.get();
		return ResponseEntity.ok().body(new VoteResponse("Unvote processed successfully", voteObj.getId(), voteObj.getContestId(), voteObj.getCandidateId()));
	}
}
record VoteResponse(String message, String voteId, Long contestId, Long candidateId) {}