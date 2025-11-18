-- @Table(name = "contests")
-- @Data
-- class Contest {
-- 	@Id
-- 	@GeneratedValue(strategy = GenerationType.IDENTITY)
-- 	private Long id;
-- 	private String title;
-- 	private String description;
-- 	private Long startTime;
-- 	private Long endTime;
-- 	private Long createdBy;
-- 	private Long createdAt;
-- }

insert into contests (title, description, start_time, end_time, created_by, created_at) values ('Sample Contest', 'This is a sample contest description.', 1700000000, 1700086400, 1, 1699996400000);

-- class Candidate {
-- 	@Id
-- 	@GeneratedValue(strategy = GenerationType.IDENTITY)
-- 	private Long id;
-- 	private Long contestId;
-- 	private String name;
-- 	private String description;
-- 	private Long createdAt;
-- }
insert into candidates (contest_id, name, description, created_at) values (1, 'Candidate A', 'Description for Candidate A', 1699996400000);
insert into candidates (contest_id, name, description, created_at) values (1, 'Candidate B', 'Description for Candidate B', 1699996400000);
insert into candidates (contest_id, name, description, created_at) values (1, 'Candidate C', 'Description for Candidate C', 1699996400000);
insert into candidates (contest_id, name, description, created_at) values (1, 'Candidate D', 'Description for Candidate D', 1699996400000);