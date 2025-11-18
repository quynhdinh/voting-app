package com.example.user.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
	private final UserRepository userRepository;

	@GetMapping
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
    // sign in with user and password

    // curl command:
    // curl -X POST "http://localhost:8080/users/signin" -d "username=testuser&password=testpass"
    // return 200 and username if success, else 401
    @PostMapping("/signin")
    public ResponseEntity<SignInResponse> signIn(String username, String password) {
        // System.out.println("Sign in attempt: " + username + ", " + password);
        User user = userRepository.findByUsername(username);
        System.out.println("User found: " + user);
        if (user != null && user.getPasswordHash().equals(password)) {
            System.out.println("Sign in successful for user: " + username);
            return ResponseEntity.ok(new SignInResponse("success", user.getUsername(), user.getId()));
        } else {
            return ResponseEntity.status(401).body(new SignInResponse("unauthorized", null, null));
        }
    }
}
record SignInResponse(String message, String username, Long id) {}