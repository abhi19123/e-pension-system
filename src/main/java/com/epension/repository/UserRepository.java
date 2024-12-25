package com.epension.repository;

import com.epension.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "u.phoneNumber = :phoneNumber OR " +
           "u.phoneNumber = CONCAT('+91', CASE WHEN :phoneNumber LIKE '+91%' THEN SUBSTRING(:phoneNumber, 4) ELSE :phoneNumber END) OR " +
           "u.phoneNumber = CASE WHEN :phoneNumber LIKE '+91%' THEN :phoneNumber ELSE CONCAT('+91', :phoneNumber) END")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);
    
    List<User> findByRole(String role);
    
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}
