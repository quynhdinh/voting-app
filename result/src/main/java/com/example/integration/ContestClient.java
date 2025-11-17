package com.example.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.dto.req.ContestDetailsDTO;

@FeignClient(name = "contest-service", url = "http://localhost:8081")
public interface ContestClient {
    @RequestMapping(method = RequestMethod.GET, value = "/contests/{id}")
    ContestDetailsDTO getContestById(@PathVariable Long id);
}
