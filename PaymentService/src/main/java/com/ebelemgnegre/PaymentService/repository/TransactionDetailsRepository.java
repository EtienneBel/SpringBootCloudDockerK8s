package com.ebelemgnegre.PaymentService.repository;

import com.ebelemgnegre.PaymentService.entity.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionDetailsRepository extends JpaRepository<TransactionDetails, Long> {
}
