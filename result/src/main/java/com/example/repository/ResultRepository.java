package com.example.repository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.Result;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
}