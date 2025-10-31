//package com.joblink.joblink.service;
//
//import com.joblink.joblink.dto.GoogleScriptResponse;
//import com.joblink.joblink.dto.PaymentResponse;
//import com.joblink.joblink.dto.TransactionData;
//import com.joblink.joblink.entity.*;
//import com.joblink.joblink.Repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentService implements IPaymentService {
//
//    // Repositories
//    private final InvoiceRepository invoiceRepository;
//    private final PaymentRepository paymentRepository;
//    private final SubscriptionRepository subscriptionsRepository;
//    private final PremiumPackageRepository packageRepository;
//
//    // Utilities
//    private final RestTemplate restTemplate;
//
//    // Lấy thông tin từ application.properties
//    @Value("${payment.bank.name}")
//    private String bankName;
//    @Value("${payment.bank.account}")
//    private String bankAccount;
//    @Value("${payment.bank.owner}")
//    private String bankOwner;
//    @Value("${payment.google.script.url}")
//    private String googleScriptUrl;
//
//    // BIN của MB Bank (dùng để tạo VietQR)
//    private static final String MB_BANK_BIN = "970422";
//
//    @Override
//    public PremiumSubscriptions getCurrentActiveSubscription(Integer seekerId) {
//        // SỬA LỖI: Dùng đúng phương thức bạn đã cung cấp trong SubscriptionRepository
//        // Phương thức này sẽ tìm gói ACTIVE và còn active (isActive = true)
//        // có ngày hết hạn muộn nhất
//        return subscriptionsRepository
//                .findTopBySeekerIdAndStatusAndIsActiveTrueOrderByEndDateDesc(seekerId, "ACTIVE")
//                .orElse(null);
//    }
//    @Override
//    @Transactional
//    public PaymentResponse createPayment(JobSeekerProfile seeker, PremiumPackages pkg) throws Exception {
//        Invoice invoice = Invoice.builder()
//                .userId(seeker.getUserId())
//                .seekerId(seeker.getSeekerId())
//
//                .amount(pkg.getPrice())
//                .status("PENDING")
//                .issuedAt(LocalDateTime.now())
//                .dueAt(LocalDateTime.now().plusHours(1))
//                .build();
//        Invoice savedInvoice = invoiceRepository.save(invoice);
//
//        // --- BƯỚC 2: TẠO SUBSCRIPTION VÀ GÁN INVOICE ID ---
//        PremiumSubscriptions subscription = new PremiumSubscriptions();
//        subscription.setUserId(seeker.getUserId());
//        subscription.setSeekerId(seeker.getSeekerId());
//        subscription.setPackageId(pkg.getPackageId());
//        subscription.setStatus("PENDING");
//        subscription.setIsActive(false);
//        subscription.setCreatedAt(LocalDateTime.now());
//        subscription.setInvoiceId(savedInvoice.getInvoiceId()); // <-- THÊM DÒNG NÀY ĐỂ GÁN INVOICE ID
//
//        // --- BƯỚC 3: LƯU SUBSCRIPTION ---
//        PremiumSubscriptions savedSub = subscriptionsRepository.save(subscription);
//
//        // --- BƯỚC 4: CẬP NHẬT LẠI INVOICE VỚI SUBSCRIPTION ID (Nếu cần liên kết 2 chiều) ---
//        savedInvoice.setSubscriptionId(savedSub.getSubscriptionId());
//
//        invoiceRepository.save(savedInvoice); // Lưu lại Invoice với thông tin đầy đủ
//
//        // Lấy orderId đã tạo
//        String orderId = String.valueOf(savedInvoice.getInvoiceId());
//
//        // --- BƯỚC 5: TẠO PAYMENT ---
//        Payment payment = new Payment();
//        payment.setInvoiceId(savedInvoice.getInvoiceId());
//        payment.setProvider("BANK_TRANSFER");
//        payment.setTxRef(orderId);
//        payment.setAmount(pkg.getPrice());
//        payment.setStatus("PENDING");
//        payment.setCreatedAt(LocalDateTime.now());
//        Payment savedPayment = paymentRepository.save(payment);
//
//        // --- BƯỚC 6: TẠO QR VÀ TRẢ VỀ ---
//        String qrCodeUrl = generateVietQRUrl(orderId, pkg.getPrice());
//
//        return PaymentResponse.builder()
//                .status("PENDING")
//                .message("Tạo thanh toán thành công. Vui lòng quét mã QR.")
//                .orderId(orderId)
//                .paymentId(savedPayment.getPaymentId())
//                .amount(pkg.getPrice())
//                .qrCodeUrl(qrCodeUrl)
//                .bankName(bankName)
//                .accountNumber(bankAccount)
//                .accountOwner(bankOwner)
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public PaymentResponse checkPaymentStatus(String orderId) {
//        Payment payment = getPaymentByOrderId(orderId);
//
//        // 1. Kiểm tra trong DB trước, nếu đã SUCCESS thì trả về
//        if (payment.getStatus().equals("SUCCESS")) {
//            return PaymentResponse.builder().status("SUCCESS").orderId(orderId).build();
//        }
//
//        // 2. Gọi Google Apps Script để lấy danh sách giao dịch
//        try {
//            GoogleScriptResponse scriptResponse = restTemplate.getForObject(googleScriptUrl, GoogleScriptResponse.class);
//
//            if (scriptResponse == null || !scriptResponse.isSuccess() || scriptResponse.getData() == null) {
//                return PaymentResponse.builder().status("PENDING").message("Không thể kiểm tra giao dịch.").build();
//            }
//
//            List<TransactionData> transactions = scriptResponse.getData();
//
//            // 3. Duyệt danh sách giao dịch
//            for (TransactionData tx : transactions) {
//                // Kiểm tra:
//                // 1. Nội dung chuyển khoản CÓ CHỨA orderId
//                // 2. Số tiền >= số tiền hóa đơn
//                if (tx.getCONTENT() != null &&
//                        tx.getCONTENT().toUpperCase().contains(orderId.toUpperCase()) &&
//                        tx.getPRICE().compareTo(payment.getAmount()) >= 0)
//                {
//                    // TÌM THẤY! Kích hoạt gói
//                    activateSubscription(payment);
//                    return PaymentResponse.builder().status("SUCCESS").orderId(orderId).build();
//                }
//            }
//
//            // 4. Không tìm thấy giao dịch
//            return PaymentResponse.builder().status("PENDING").message("Chưa tìm thấy giao dịch.").build();
//
//        } catch (Exception e) {
//            System.err.println("Lỗi khi gọi Google Script: " + e.getMessage());
//            return PaymentResponse.builder().status("ERROR").message("Lỗi hệ thống khi kiểm tra thanh toán.").build();
//        }
//    }
//
//    @Override
//    @Transactional
//    public PaymentResponse cancelPayment(String orderId) {
//        Payment payment = getPaymentByOrderId(orderId);
//
//        // Chỉ hủy nếu đang PENDING
//        if (payment.getStatus().equals("PENDING")) {
//            payment.setStatus("CANCELLED");
//            paymentRepository.save(payment);
//
//            Invoice invoice = getInvoiceByOrderId(orderId);
//            invoice.setStatus("CANCELLED");
//            invoiceRepository.save(invoice);
//
//            PremiumSubscriptions sub = subscriptionsRepository.findById(invoice.getSubscriptionId()).orElse(null);
//            if(sub != null) {
//                sub.setStatus("CANCELLED");
//                subscriptionsRepository.save(sub);
//            }
//            return PaymentResponse.builder().status("CANCELLED").orderId(orderId).build();
//        }
//
//        return PaymentResponse.builder().status(payment.getStatus()).orderId(orderId).build();
//    }
//
//    @Override
//    public Payment getPaymentByOrderId(String orderId) {
//        return paymentRepository.findByTxRef(orderId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy Payment với orderId: " + orderId));
//    }
//
//    @Override
//    public Invoice getInvoiceByOrderId(String orderId) {
//        try {
//            // Chuyển orderId (là String chứa số) thành kiểu ID của Invoice (ví dụ: Integer)
//            Integer invoiceId = Integer.parseInt(orderId);
//            return invoiceRepository.findById(invoiceId)
//                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Invoice với ID: " + orderId));
//        } catch (NumberFormatException e) {
//            // Xử lý trường hợp orderId không phải là số hợp lệ
//            throw new IllegalArgumentException("OrderId không hợp lệ để tìm Invoice: " + orderId, e);
//        }
//    }
//
//    // --- Helper Methods ---
//
//    /**
//     * Hàm helper để tạo link ảnh QR của VietQR
//     */
//    private String generateVietQRUrl(String orderId, BigDecimal amount) throws Exception {
//        // Template: https://api.vietqr.io/image/<BIN>-<STK>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<CONTENT>&accountName=<OWNER>
//        String encodedInfo = URLEncoder.encode(orderId, StandardCharsets.UTF_8);
//        String encodedOwner = URLEncoder.encode(bankOwner, StandardCharsets.UTF_8);
//
//        return UriComponentsBuilder.fromHttpUrl("https://api.vietqr.io/image/" + MB_BANK_BIN + "-" + bankAccount + "-print.png")
//                .queryParam("amount", amount.intValue())
//                .queryParam("addInfo", encodedInfo)
//                .queryParam("accountName", encodedOwner)
//                .toUriString();
//    }
//
//    /**
//     * Hàm helper để kích hoạt gói khi thanh toán thành công
//     */
//    @Transactional
//    private void activateSubscription(Payment payment) {
//        if (payment.getStatus().equals("SUCCESS")) {
//            return; // Đã xử lý rồi
//        }
//
//        // 1. Cập nhật Payment
//        payment.setStatus("SUCCESS");
//        payment.setPaymentMethod("BANK_TRANSFER"); // Ghi rõ phương thức
//        paymentRepository.save(payment);
//
//        // 2. Cập nhật Invoice
//        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId()).orElse(null);
//        if (invoice == null) return;
//
//        invoice.setStatus("PAID");
//        invoice.setPaidAt(LocalDateTime.now());
//        invoiceRepository.save(invoice);
//
//        // 3. Kích hoạt Subscription
//        PremiumSubscriptions sub = subscriptionsRepository.findById(invoice.getSubscriptionId()).orElse(null);
//        if (sub == null) return;
//
//        PremiumPackages pkg = packageRepository.findById(sub.getPackageId()).orElse(null);
//        if (pkg == null) return;
//
//        sub.setStatus("ACTIVE");
//        sub.setIsActive(true);
//        sub.setStartDate(LocalDateTime.now());
//        sub.setEndDate(LocalDateTime.now().plusDays(pkg.getDurationDays()));
//        sub.setUpdatedAt(LocalDateTime.now());
//        sub.setInvoiceId(invoice.getInvoiceId());
//        subscriptionsRepository.save(sub);
//    }
//}