-- Schema for votes table managed by schema.sql (used instead of Hibernate DDL)
DROP TABLE IF EXISTS votes;
CREATE TABLE IF NOT EXISTS votes (
	id BIGSERIAL PRIMARY KEY,
	contest_id VARCHAR(255) NOT NULL,
	voter_id VARCHAR(255) NOT NULL,
	candidate_id VARCHAR(255) NOT NULL,
	created_at BIGINT NOT NULL,
    unique (contest_id, voter_id, candidate_id)
);
