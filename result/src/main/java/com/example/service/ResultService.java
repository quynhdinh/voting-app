package com.example.service;
import com.example.repository.ResultRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.VoteDTO;
import com.example.model.Result;
import java.util.List;
@Service
@AllArgsConstructor
@Transactional
public class ResultService {
    private final ResultRepository resultRepository;
    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }
    public List<Result> getResultsByContestId(Long contestId) {
        return resultRepository.findByContestId(contestId);
    }
    // increment the vote in redis
    // increment the vote in postgres
    public void processVote(VoteDTO vote) {
        String candidates = vote.candidateIds();
        String[] candidateIds = candidates.split(",");// they should be all numbers
        for (String candidateIdStr : candidateIds) {
            Long candidateId = Long.parseLong(candidateIdStr);
            Result result = resultRepository.findByContestIdAndCandidateId(vote.contestId(), candidateId)
                    .orElseGet(() -> new Result(null, vote.contestId(), candidateId, 0L));
            result.setTotalVotes(result.getTotalVotes() + 1);
            resultRepository.save(result);
        }
    }
    public void processUnvote(VoteDTO unvote) {
        String candidates = unvote.candidateIds();
        String[] candidateIds = candidates.split(",");// they should be all numbers
        for (String candidateIdStr : candidateIds) {
            Long candidateId = Long.parseLong(candidateIdStr);
            Result result = resultRepository.findByContestIdAndCandidateId(unvote.contestId(), candidateId)
                    .orElseGet(() -> new Result(null, unvote.contestId(), candidateId, 0L));
            result.setTotalVotes(result.getTotalVotes() - 1);
            resultRepository.save(result);
        }
    }
}
