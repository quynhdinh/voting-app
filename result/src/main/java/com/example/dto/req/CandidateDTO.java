package com.example.dto.req;

public record CandidateDTO(Long id, 
Long contestId, 
String name, 
String description, 
Long createdAt) {}
