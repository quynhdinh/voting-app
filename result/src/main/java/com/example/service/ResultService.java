package com.example.service;
import com.example.repository.ResultRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.VoteDTO;
import com.example.dto.req.CandidateDTO;
import com.example.dto.req.ContestDetailsDTO;
import com.example.dto.res.CandidateResult;
import com.example.dto.res.ContestResultDTO;
import com.example.integration.ContestClient;
import com.example.model.Result;
import java.util.*;

@Transactional
@AllArgsConstructor
@Service
@Slf4j
public class ResultService {
    private final ResultRepository resultRepository;
    private final ContestClient contestClient;

    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }
    @CircuitBreaker(name = "resultServiceCircuitBreaker", fallbackMethod = "getResultsByContestIdFallback")
    public ContestResultDTO getResultsByContestId(Long contestId) {
        ContestDetailsDTO contest = contestClient.getContestById(contestId);
        List<CandidateDTO> candidates = contest.candidates();
        List<CandidateResult> candidateResults = new ArrayList<>();
        for (CandidateDTO candidate : candidates) {
            Long votes = resultRepository.findByContestIdAndCandidateId(contestId, candidate.id())
                    .map(Result::getTotalVotes)
                    .orElse(0L);
            candidateResults.add(new CandidateResult(
                    candidate.id(),
                    candidate.name(),
                    candidate.description(),
                    votes
            ));
        }
        return new ContestResultDTO(
                contestId,
                contest.contest().title(),
                contest.contest().description(),
                candidateResults
        );
    }

	private ContestResultDTO getResultsByContestIdFallback(Long contestId, Throwable t) {
        log.error("Fallback executed for getResultsByContestId due to: {}", t.getMessage());
        return new ContestResultDTO(
                contestId,
                "N/A",
                "N/A",
                Collections.emptyList()
        );
	}

    public void processVote(VoteDTO vote) {
        Long candidateId = vote.candidateId();
        Result result = resultRepository.findByContestIdAndCandidateId(vote.contestId(), candidateId)
                .orElseGet(() -> new Result(null, vote.contestId(), candidateId, 0L));
        result.setTotalVotes(result.getTotalVotes() + 1);
        resultRepository.save(result);
    }
    public void processUnvote(VoteDTO unvote) {
        Long candidateId = unvote.candidateId();
        Result result = resultRepository.findByContestIdAndCandidateId(unvote.contestId(), candidateId)
                .orElseGet(() -> new Result(null, unvote.contestId(), candidateId, 0L));
        result.setTotalVotes(result.getTotalVotes() - 1);
        resultRepository.save(result);
    }
}