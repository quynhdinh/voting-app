-- Creates the results table used by the results service
CREATE TABLE IF NOT EXISTS results (
    id BIGSERIAL PRIMARY KEY,
    contest_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    total_votes BIGINT NOT NULL DEFAULT 0
);
