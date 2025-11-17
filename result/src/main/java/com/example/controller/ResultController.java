package com.example.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.service.ResultService;
import com.example.model.Result;
import java.util.List;

@RestController
@RequestMapping("/results")
@AllArgsConstructor
public class ResultController {
    private final ResultService resultService;

    // get test endpoint
    @GetMapping
    public String test() {
        return "Result Service is up and running!";
    }
    //get all results
    @GetMapping
    @RequestMapping("/all")
    public List<Result> getAllResults() {
        return resultService.getAllResults();
    }
    @GetMapping("/{contestId}")
    public List<Result> getResultsByContestId(@PathVariable Long contestId) {
        return resultService.getResultsByContestId(contestId);
    }
}
