package com.example.contest.service;
import com.example.contest.dto.ContestDTO;
import com.example.contest.model.Contest;
import com.example.contest.repository.ContestRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.example.contest.repository.CandidateRepository;
import com.example.contest.model.Candidate;
import com.example.contest.dto.CreateContestDTO;

@Service
@AllArgsConstructor
@Transactional // make save transactional
public class ContestService {
	private final ContestRepository contestRepository;
	private final CandidateRepository candidateRepository;
	
	public List<Contest> getAllContests() {
		return contestRepository.findAll();
	}

	public Contest createContest(CreateContestDTO contest) {
		Contest newContest = new Contest();
		newContest.setCreatedBy(contest.getCreatedBy());
		newContest.setTitle(contest.getTitle());
		newContest.setDescription(contest.getDescription());
		newContest.setStartTime(contest.getStartTime());
		newContest.setEndTime(contest.getEndTime());
		newContest.setCreatedAt(System.currentTimeMillis());
		System.out.println("saving " + newContest);
		Contest savedContest = contestRepository.save(newContest);

		if (contest.getCandidates() != null) {
			contest.getCandidates().forEach(candidate -> {
				Candidate newCandidate = new Candidate();
				newCandidate.setName(candidate.getName());
				newCandidate.setContestId(savedContest.getId());
				candidateRepository.save(newCandidate);
			});
		}
		return savedContest;
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
