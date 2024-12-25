package com.epension.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String role = "ROLE_PENSIONER";

    @Column
    private String employeeId;

    @Column
    private String department;

    @Column
    private String aadharNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private LocalDate retirementDate;

    @Column
    private String pensionType;

    @Column
    private String profilePicture;

    @Column
    private String googleId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean emailVerified = false;  

    @Column(nullable = false)
    private boolean active = true;  

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // This is used by Spring Security
    public String getUsername() {
        return email;
    }

    // For backward compatibility
    public boolean existsByUsername(String username) {
        return email.equals(username);
    }
}
