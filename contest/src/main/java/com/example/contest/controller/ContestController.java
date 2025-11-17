package com.example.contest.controller;
import com.example.contest.dto.ContestDTO;
import com.example.contest.model.Contest;
import com.example.contest.service.ContestService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/contests")
@AllArgsConstructor
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
}
