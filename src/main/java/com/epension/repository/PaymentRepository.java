package com.epension.repository;

import com.epension.model.Payment;
import com.epension.model.PensionApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPensionApplication(PensionApplication pensionApplication);
    List<Payment> findByStatus(String status);
}
