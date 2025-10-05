package com.ebelemgnegre.PaymentService.service;

import com.ebelemgnegre.PaymentService.entity.TransactionDetails;
import com.ebelemgnegre.PaymentService.model.PaymentMode;
import com.ebelemgnegre.PaymentService.model.PaymentRequest;
import com.ebelemgnegre.PaymentService.model.PaymentResponse;
import com.ebelemgnegre.PaymentService.repository.TransactionDetailsRepository;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    TransactionDetailsRepository transactionDetailsRepository;
    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording Payment Details: {}", paymentRequest);

        TransactionDetails transactionDetails = TransactionDetails.builder()
                        .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(paymentRequest.getOrderId())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();

        transactionDetailsRepository.save(transactionDetails);

        log.info("Transaction Completed with Id: {}", transactionDetails.getId());
        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(Long orderId) {
        TransactionDetails transactionDetails = transactionDetailsRepository.findByOrderId(Long.valueOf(orderId));
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .status(transactionDetails.getPaymentStatus())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .amount(transactionDetails.getAmount())
                .paymentDate(transactionDetails.getPaymentDate())
                .orderId(Long.valueOf(transactionDetails.getOrderId()))
                .build();
        return paymentResponse;
    }

    @Override
    public PaymentResponse getPaymentDetailsById(Long paymentId) {
        log.info("Getting payment details for id: {}", paymentId);

        TransactionDetails transactionDetails = transactionDetailsRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        return PaymentResponse.builder()
                .paymentId(transactionDetails.getId())
                .status(transactionDetails.getPaymentStatus())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .amount(transactionDetails.getAmount())
                .paymentDate(transactionDetails.getPaymentDate())
                .orderId(Long.valueOf(transactionDetails.getOrderId()))
                .build();
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        log.info("Getting all payments");

        List<TransactionDetails> allTransactions = transactionDetailsRepository.findAll();

        return allTransactions.stream()
                .map(transaction -> PaymentResponse.builder()
                        .paymentId(transaction.getId())
                        .status(transaction.getPaymentStatus())
                        .paymentMode(PaymentMode.valueOf(transaction.getPaymentMode()))
                        .amount(transaction.getAmount())
                        .paymentDate(transaction.getPaymentDate())
                        .orderId(Long.valueOf(transaction.getOrderId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse updatePayment(Long paymentId, PaymentRequest paymentRequest) {
        log.info("Updating payment with id: {}", paymentId);

        TransactionDetails transactionDetails = transactionDetailsRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        transactionDetails.setPaymentStatus(paymentRequest.getPaymentStatus() != null ?
                paymentRequest.getPaymentStatus() : transactionDetails.getPaymentStatus());
        transactionDetails.setPaymentMode(paymentRequest.getPaymentMode() != null ?
                paymentRequest.getPaymentMode().name() : transactionDetails.getPaymentMode());
        transactionDetails.setAmount(paymentRequest.getAmount());
        transactionDetails.setReferenceNumber(paymentRequest.getReferenceNumber());

        TransactionDetails updatedTransaction = transactionDetailsRepository.save(transactionDetails);

        log.info("Payment updated successfully");

        return PaymentResponse.builder()
                .paymentId(updatedTransaction.getId())
                .status(updatedTransaction.getPaymentStatus())
                .paymentMode(PaymentMode.valueOf(updatedTransaction.getPaymentMode()))
                .amount(updatedTransaction.getAmount())
                .paymentDate(updatedTransaction.getPaymentDate())
                .orderId(Long.valueOf(updatedTransaction.getOrderId()))
                .build();
    }

    @Override
    public void deletePayment(Long paymentId) {
        log.info("Deleting payment with id: {}", paymentId);

        TransactionDetails transactionDetails = transactionDetailsRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        transactionDetailsRepository.delete(transactionDetails);
        log.info("Payment deleted successfully");
    }
}
