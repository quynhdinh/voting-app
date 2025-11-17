package com.example.vote.repository;
import com.example.vote.model.Vote;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {

}