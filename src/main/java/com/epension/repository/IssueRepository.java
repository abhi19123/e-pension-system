package com.epension.repository;

import com.epension.model.Issue;
import com.epension.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByUser(User user);
    List<Issue> findByStatus(String status);
}
