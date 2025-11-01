package com.joblink.joblink.service;

import com.joblink.joblink.dto.PaymentResponse;
import com.joblink.joblink.entity.Invoice;
import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.entity.Payment;
import com.joblink.joblink.entity.PremiumPackages;
import com.joblink.joblink.entity.PremiumSubscriptions;

public interface IPaymentService {
    PremiumSubscriptions getCurrentActiveSubscription(Integer seekerId);
    PaymentResponse createPayment(JobSeekerProfile seeker, PremiumPackages pkg) throws Exception;
    PaymentResponse checkPaymentStatus(String orderId);
    PaymentResponse cancelPayment(String orderId) throws Exception;
    Payment getPaymentByOrderId(String orderId);
    Invoice getInvoiceByOrderId(String orderId);
}