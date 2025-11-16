package com.example.contest;

import javax.annotation.processing.Generated;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.List;
import lombok.AllArgsConstructor;

@SpringBootApplication
public class ContestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContestApplication.class, args);
	}

}
@Entity
@Table(name = "contests")
@Data
class Contest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String description;
	private Long startTime;
	private Long endTime;
	private Long createdBy;
	private Long createdAt;
}

interface ContestRepository extends JpaRepository<Contest, Long> {

}
@RestController
@RequestMapping("/contests")
@AllArgsConstructor
class ContestController {
	private final ContestRepository contestRepository;

	@RequestMapping("")
	public List<Contest> getAllContests() {
		System.out.println("Fetching all contests");
		return contestRepository.findAll();
	}
}