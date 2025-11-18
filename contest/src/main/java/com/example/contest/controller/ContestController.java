package com.example.contest.controller;
import com.ecwid.consul.v1.Response;
import com.example.contest.dto.ContestDTO;
import com.example.contest.dto.CreateContestDTO;
import com.example.contest.model.Contest;
import com.example.contest.service.ContestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/contests")
@AllArgsConstructor
@Slf4j
public class ContestController {
	private final ContestService contestService;

	@RequestMapping("")
	public List<Contest> getAllContests() {
		return contestService.getAllContests();
	}

	@GetMapping("/{id}")
	public ContestDTO getContestById(@PathVariable Long id) {
        ContestDTO contestById = contestService.getContestById(id);
		return contestById;
	}

	@PostMapping
	public ResponseEntity<Contest> createContest(@RequestBody CreateContestDTO contest) {
		log.info("Creating contest: {}", contest);
		Contest resContest = contestService.createContest(contest);
		log.info("Created contest: {}", resContest);
		return ResponseEntity.ok(resContest);
	}
	// exapmle curl
	// curl -X POST "http://localhost:8080/contests" -H "Content-Type: application/json" -d '{"createdBy":1,"title":"Sample Contest","description":"This is a sample contest.","startTime":1700000000,"endTime":1700003600,"candidates":[{"name":"Candidate 1","description":"Description 1"},{"name":"Candidate 2","description":"Description 2"}]}'
}