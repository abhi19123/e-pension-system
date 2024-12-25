package com.epension.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pension_application_id", nullable = false)
    private PensionApplication pensionApplication;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    private String transactionId;

    @Column(nullable = false)
    private String paymentMethod;

    private String remarks;
}
