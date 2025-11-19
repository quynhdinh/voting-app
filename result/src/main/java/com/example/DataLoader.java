package com.example;

import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import com.example.repository.ResultRepository;
import org.springframework.boot.CommandLineRunner;

@Component
@AllArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final ResultRepository resultRepository;

    @Override
    public void run(String... args) throws Exception {
        resultRepository.deleteAll();
    }
}