package com.example.contest.service;
import com.example.contest.dto.ContestDTO;
import com.example.contest.model.Contest;
import com.example.contest.repository.ContestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.contest.repository.CandidateRepository;
import com.example.contest.model.Candidate;
@Service
@AllArgsConstructor
public class ContestService {
	private final ContestRepository contestRepository;
	private final CandidateRepository candidateRepository;
	
	public List<Contest> getAllContests() {
		return contestRepository.findAll();
	}

	public ContestDTO getContestById(Long id) {
		Contest contest = contestRepository.findById(id).orElse(null);
		if (contest == null) {
			return null;
		}
		List<Candidate> candidates = candidateRepository.findByContestId(id);
		return new ContestDTO(contest, candidates);
	}

}
