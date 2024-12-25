package com.epension.controller;

import com.epension.model.Issue;
import com.epension.model.User;
import com.epension.repository.IssueRepository;
import com.epension.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/raise")
    public ResponseEntity<?> raiseIssue(@RequestBody Issue issue, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        issue.setUser(user);
        Issue savedIssue = issueRepository.save(issue);
        return ResponseEntity.ok(savedIssue);
    }

    @GetMapping("/my-issues")
    public ResponseEntity<List<Issue>> getMyIssues(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Issue> issues = issueRepository.findByUser(user);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIssue(@PathVariable Long id, Authentication auth) {
        return issueRepository.findById(id)
            .map(issue -> {
                User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                if (!issue.getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(403).body("Access denied");
                }
                return ResponseEntity.ok(issue);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateIssueStatus(@PathVariable Long id, @RequestBody String status, Authentication auth) {
        return issueRepository.findById(id)
            .map(issue -> {
                User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                if (!issue.getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(403).body("Access denied");
                }
                
                issue.setStatus(status);
                return ResponseEntity.ok(issueRepository.save(issue));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
