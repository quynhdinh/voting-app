package com.example.vote.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.example.vote.model.User_Vote_State;
import java.util.Optional;

@Repository
public interface UserVoteStateRepository extends MongoRepository<User_Vote_State, Long> {
	Optional<User_Vote_State> findByVoterIdAndContestId(Long voterId, Long contestId);
}