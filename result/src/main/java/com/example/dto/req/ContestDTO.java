package com.example.dto.req;
public record ContestDTO(
    Long id, String title, String description, Long startTime,
    Long endTime, Long createdBy, Long createdAt) {
}
