package com.epension.model;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "pension_applications")
public class PensionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String applicationNumber;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String pensionType;

    @Column(nullable = false)
    private BigDecimal monthlyAmount;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String ifscCode;

    @Column(nullable = false)
    private LocalDate appliedDate;

    @Column(nullable = false)
    private LocalDate lastUpdated = LocalDate.now();

    // Personal Information
    @Column(nullable = false)
    private String aadharNumber;

    @Column(nullable = false)
    private String panNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String maritalStatus;

    // Contact Information
    @Column(nullable = false)
    private String permanentAddress;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;
    
    @Column(nullable = false)
    private String pinCode;

    // Employment Details
    @Column(nullable = false)
    private String lastEmployer;
    
    @Column(nullable = false)
    private LocalDate retirementDate;
    
    @Column(nullable = false)
    private String employeeId;
    
    @Column(nullable = false)
    private Double lastDrawnSalary;

    // Pension Details
    private LocalDate approvalDate;
    
    private LocalDate nextPaymentDate;

    // Documents
    private String aadharCardDoc;
    private String panCardDoc;
    private String bankPassbookDoc;
    private String retirementProofDoc;
    private String photographDoc;

    // Additional Information
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(nullable = false)
    private boolean declarationAccepted = false;
}
