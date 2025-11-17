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

insert into contests (title, description, start_time, end_time, created_by, created_at) values ('Sample Contest', 'This is a sample contest description.', 1700000000, 1700086400, 1, 1699996400);