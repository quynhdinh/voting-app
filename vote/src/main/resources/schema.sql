-- Schema for votes table managed by schema.sql (used instead of Hibernate DDL)
-- make contest_id, voter_id int
DROP TABLE IF EXISTS votes;
CREATE TABLE IF NOT EXISTS votes (
	id BIGSERIAL PRIMARY KEY,
	contest_id INT NOT NULL,
	voter_id INT NOT NULL,
	candidate_id VARCHAR(255) NOT NULL,
	created_at BIGINT NOT NULL,
    unique (contest_id, voter_id, candidate_id)
);
