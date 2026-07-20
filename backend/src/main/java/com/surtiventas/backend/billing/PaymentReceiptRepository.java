package com.surtiventas.backend.billing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {

    Optional<PaymentReceipt> findByPaymentId(Long paymentId);
}
