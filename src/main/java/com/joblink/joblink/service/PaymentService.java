package com.joblink.joblink.service;

import com.joblink.joblink.dao.InvoiceDao;
import com.joblink.joblink.dao.PaymentDao;
import com.joblink.joblink.model.Invoice;
import com.joblink.joblink.model.Payment;
import com.joblink.joblink.model.PremiumPackage;
import com.joblink.joblink.model.PremiumSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final PremiumService premiumService;

    public PaymentService(InvoiceDao invoiceDao, PaymentDao paymentDao, PremiumService premiumService) {
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.premiumService = premiumService;
    }

    @Transactional
    public Invoice createInvoiceForPackage(int userId, Integer employerId, Integer seekerId, int packageId) {
        PremiumPackage pkg = premiumService.getPackageById(packageId);
        if (pkg == null) {
            throw new IllegalArgumentException("Package not found");
        }

        Invoice invoice = new Invoice();
        invoice.setUserId(userId);
        invoice.setEmployerId(employerId);
        invoice.setSeekerId(seekerId);
        invoice.setAmount(pkg.getPrice());
        invoice.setStatus("PENDING");
        invoice.setDueAt(java.time.LocalDateTime.now().plusDays(7)); // due_at là NOT NULL

        int invoiceId = invoiceDao.create(invoice);
        invoice.setInvoiceId(invoiceId);
        return invoice;
    }

    @Transactional
    public Payment initiatePayment(int invoiceId, String provider, String paymentMethod) {
        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found");
        }
        if (!"PENDING".equals(invoice.getStatus())) {
            throw new IllegalStateException("Invoice is not pending");
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setProvider(provider);
        payment.setTxRef(UUID.randomUUID().toString()); // hoặc "DEMO_" + System.currentTimeMillis()
        payment.setAmount(invoice.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentMethod(paymentMethod);

        int paymentId = paymentDao.create(payment);
        payment.setPaymentId(paymentId);
        return payment;
    }

    @Transactional
    public void processPaymentSuccess(String txRef, int packageId, int userId, Integer employerId, Integer seekerId) {
        Payment payment = paymentDao.findByTxRef(txRef);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found");
        }

        // 1) Payment -> SUCCESS
        paymentDao.updateStatus(payment.getPaymentId(), "SUCCESS");

        // 2) Invoice -> PAID (+ set paid_at)
        invoiceDao.updateStatus(payment.getInvoiceId(), "PAID");

        // 3) Tạo subscription
        PremiumSubscription subscription = premiumService.createSubscription(userId, employerId, seekerId, packageId);

        // 4) Lưu subscription_id vào Invoice
        invoiceDao.updateSubscriptionId(payment.getInvoiceId(), subscription.getSubscriptionId());
    }

    public List<Invoice> getUserInvoices(int userId) {
        return invoiceDao.findByUserId(userId);
    }

    public Invoice getInvoice(int invoiceId) {
        return invoiceDao.findById(invoiceId);
    }

    public List<Payment> getInvoicePayments(int invoiceId) {
        return paymentDao.findByInvoiceId(invoiceId);
    }
}
