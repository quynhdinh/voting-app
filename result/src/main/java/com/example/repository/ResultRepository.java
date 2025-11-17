package com.example.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.Result;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    public List<Result> findByContestId(Long contestId);
    public Optional<Result> findByContestIdAndCandidateId(Long contestId, Long candidateId);
}