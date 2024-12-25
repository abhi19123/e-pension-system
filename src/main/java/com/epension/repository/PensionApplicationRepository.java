package com.epension.repository;

import com.epension.model.PensionApplication;
import com.epension.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PensionApplicationRepository extends JpaRepository<PensionApplication, Long> {
    List<PensionApplication> findByUser(User user);
    List<PensionApplication> findByStatus(String status);
}
