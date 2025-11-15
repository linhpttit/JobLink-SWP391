package com.joblink.joblink.service;

import com.joblink.joblink.Repository.*; // Import các Repository cần thiết
import com.joblink.joblink.entity.*;   // Import các Entity cần thiết
import lombok.RequiredArgsConstructor;   // Lombok để tự tạo constructor
import org.springframework.stereotype.Service; // Đánh dấu đây là một Service Bean
import org.springframework.transaction.annotation.Transactional; // Đảm bảo các thao tác DB trong một giao dịch

import java.time.LocalDateTime; // Dùng cho ngày giờ
import java.util.Optional; // Kiểu trả về cho findById
import java.util.logging.Logger; // Để ghi log

@Service // Đánh dấu lớp này là một Spring Service component
@RequiredArgsConstructor // Tự động tạo constructor với các trường final được inject
public class SubscriptionService {

    // Khởi tạo Logger để ghi log
    private static final Logger logger = Logger.getLogger(SubscriptionService.class.getName());

    // Inject các Repository cần thiết thông qua constructor (do @RequiredArgsConstructor)
    private final SubscriptionRepository subscriptionsRepository;
    private final PaymentRepository paymentRepository; // Có thể cần để kiểm tra/cập nhật payment
    private final InvoiceRepository invoiceRepository;
    private final PremiumPackageRepository packageRepository;

    /**
     * Tìm gói đăng ký (Subscription) đang hoạt động và còn hiệu lực của một Job Seeker.
     * @param seekerId ID của Job Seeker
     * @return Đối tượng PremiumSubscriptions nếu tìm thấy, ngược lại trả về null.
     */
    public PremiumSubscriptions getCurrentActiveSubscription(Integer seekerId) {
        logger.info("Tìm kiếm gói Premium đang hoạt động cho seekerId: " + seekerId);
        // Sử dụng phương thức được định nghĩa trong SubscriptionRepository
        // Tìm gói có trạng thái ACTIVE, isActive=true, và ngày kết thúc (endDate) chưa đến
        // Sắp xếp theo endDate giảm dần và lấy gói đầu tiên (có ngày hết hạn xa nhất)
        return subscriptionsRepository
                .findTopBySeekerIdAndStatusAndIsActiveTrueOrderByEndDateDesc(seekerId, "ACTIVE")
                .orElse(null); // Nếu không tìm thấy, trả về null
    }

    /**
     * Kích hoạt gói đăng ký (Subscription) sau khi thanh toán được xác nhận thành công.
     * Cập nhật trạng thái cho Payment, Invoice và Subscription, tính toán ngày bắt đầu/kết thúc.
     * Được gọi bởi WebhookController hoặc PaymentController (như một fallback).
     *
     * @param payment Đối tượng Payment đã được xác nhận là thành công (trạng thái PENDING)
     * @throws Exception Nếu có lỗi xảy ra trong quá trình kích hoạt (ví dụ: không tìm thấy bản ghi liên quan)
     */
    @Transactional // Đảm bảo tất cả các thao tác DB thành công hoặc rollback nếu có lỗi
    public void activateSubscription(Payment payment) throws Exception {
        // Kiểm tra đầu vào
        if (payment == null) {
            throw new IllegalArgumentException("Đối tượng Payment không được null.");
        }

        // --- Ngăn chặn kích hoạt lại ---
        // Chỉ kích hoạt nếu trạng thái Payment đang là PENDING
        if (!"PENDING".equals(payment.getStatus())) {
            logger.warning("Đang cố gắng kích hoạt Payment không ở trạng thái PENDING (ID: " + payment.getPaymentId() + ", Status: " + payment.getStatus() + "). Bỏ qua.");
            return; // Không làm gì cả nếu đã xử lý rồi
        }

        logger.info("Bắt đầu kích hoạt Subscription liên kết với Payment ID: " + payment.getPaymentId() + ", Mã đơn nội bộ (txRef): " + payment.getTxRef());

        // --- 1. Cập nhật trạng thái Payment ---
        // Sử dụng "SUCCESS" để khớp với CHECK constraint CK_Pay_Status
        payment.setStatus("SUCCESS");
        payment.setPaymentMethod("PAYOS"); // Ghi nhận phương thức thanh toán
        // Lưu lại thay đổi vào database
        paymentRepository.save(payment);
        logger.info("Đã cập nhật Payment ID: " + payment.getPaymentId() + " thành SUCCESS.");

        // --- 2. Cập nhật trạng thái Invoice ---
        // Tìm Invoice tương ứng với Payment
        Invoice invoice = invoiceRepository.findById(payment.getInvoiceId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy Invoice cho Payment ID: " + payment.getPaymentId()));

        // Sử dụng "PAID" để khớp với CHECK constraint CK_Inv_Status
        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now()); // Ghi nhận thời gian thanh toán
        // Lưu lại thay đổi vào database
        invoiceRepository.save(invoice);
        logger.info("Đã cập nhật Invoice ID: " + invoice.getInvoiceId() + " thành PAID.");

        // --- 3. Kích hoạt Subscription ---
        // Tìm Subscription tương ứng với Invoice
        PremiumSubscriptions sub = subscriptionsRepository.findById(invoice.getSubscriptionId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy Subscription cho Invoice ID: " + invoice.getInvoiceId()));

        // Kiểm tra lại trạng thái Subscription (phòng trường hợp race condition)
        if (!"PENDING".equals(sub.getStatus())) {
            logger.warning("Subscription (ID: " + sub.getSubscriptionId() + ") liên kết với Payment ID: " + payment.getPaymentId() + " không ở trạng thái PENDING (Status: " + sub.getStatus() + "). Bỏ qua kích hoạt lại.");
            // Dù không kích hoạt lại Sub, nhưng Payment và Invoice vẫn nên được đánh dấu là đã thanh toán.
            return;
        }

        // Lấy thông tin gói Premium để biết thời hạn (durationDays)
        PremiumPackages pkg = packageRepository.findById(sub.getPackageId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy Package cho Subscription ID: " + sub.getSubscriptionId()));

        // Cập nhật trạng thái và ngày hiệu lực của Subscription
        sub.setStatus("ACTIVE"); // Khớp với CHECK constraint CK_Sub_Status
        sub.setIsActive(true);
        LocalDateTime startDate = LocalDateTime.now(); // Ngày bắt đầu là thời điểm hiện tại
        sub.setStartDate(startDate);

        // Tính ngày kết thúc dựa trên durationDays của gói
        if (pkg.getDurationDays() != null && pkg.getDurationDays() > 0) {
            sub.setEndDate(startDate.plusDays(pkg.getDurationDays()));
        } else {
            // Xử lý trường hợp durationDays không hợp lệ (ví dụ: null hoặc <= 0)
            logger.warning("Gói Premium ID " + pkg.getPackageId() + " có duration_days không hợp lệ. Đặt mặc định 30 ngày.");
            sub.setEndDate(startDate.plusDays(30)); // Đặt một giá trị mặc định, ví dụ 30 ngày
        }
        sub.setUpdatedAt(LocalDateTime.now()); // Cập nhật thời gian chỉnh sửa cuối
        // Lưu lại thay đổi vào database
        subscriptionsRepository.save(sub);

        logger.info("Đã kích hoạt Subscription (ID: " + sub.getSubscriptionId() + "). Bắt đầu: " + sub.getStartDate() + ", Kết thúc: " + sub.getEndDate());
    }

    /**
     * Cập nhật trạng thái cho Invoice và Subscription khi webhook trả về trạng thái không thành công
     * (ví dụ: CANCELLED, EXPIRED, FAILED).
     * Chỉ cập nhật nếu trạng thái hiện tại là PENDING.
     *
     * @param invoiceId ID của Invoice liên quan
     * @param newStatus Trạng thái mới cần cập nhật (phải hợp lệ với CHECK constraints)
     */
    @Transactional // Đảm bảo các thao tác DB là một khối
    public void updateSubscriptionStatus(Integer invoiceId, String newStatus) {
        // Kiểm tra đầu vào
        if (invoiceId == null || newStatus == null || newStatus.isEmpty()) {
            logger.warning("Đầu vào không hợp lệ cho updateSubscriptionStatus.");
            return;
        }
        // Kiểm tra xem newStatus có hợp lệ với các CHECK constraint không
        // Ví dụ: if (!List.of("CANCELLED", "EXPIRED", "FAILED").contains(newStatus)) { ... }
        // Tạm thời bỏ qua kiểm tra này, giả định newStatus hợp lệ

        logger.info("Bắt đầu cập nhật trạng thái thành '" + newStatus + "' cho các bản ghi liên quan đến Invoice ID: " + invoiceId);

        // --- Cập nhật Invoice ---
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            // Chỉ cập nhật nếu Invoice đang ở trạng thái PENDING
            if ("PENDING".equals(invoice.getStatus())) {
                invoice.setStatus(newStatus); // Cập nhật trạng thái Invoice
                invoiceRepository.save(invoice);
                logger.info("Đã cập nhật Invoice ID " + invoiceId + " thành " + newStatus);

                // --- Cập nhật Subscription tương ứng ---
                if (invoice.getSubscriptionId() != null) {
                    Optional<PremiumSubscriptions> subOpt = subscriptionsRepository.findById(invoice.getSubscriptionId());
                    if (subOpt.isPresent()) {
                        PremiumSubscriptions sub = subOpt.get();
                        // Chỉ cập nhật nếu Subscription đang ở trạng thái PENDING
                        if ("PENDING".equals(sub.getStatus())) {
                            sub.setStatus(newStatus); // Cập nhật trạng thái Subscription
                            sub.setIsActive(false); // Đảm bảo không active
                            sub.setUpdatedAt(LocalDateTime.now());
                            subscriptionsRepository.save(sub);
                            logger.info("Đã cập nhật Subscription ID " + sub.getSubscriptionId() + " thành " + newStatus);
                        } else {
                            logger.warning("Subscription ID " + sub.getSubscriptionId() + " không ở trạng thái PENDING (Status: " + sub.getStatus() + "). Không cập nhật.");
                        }
                    } else {
                        logger.warning("Không tìm thấy Subscription liên kết với Invoice ID " + invoiceId + " khi cập nhật trạng thái.");
                    }
                } else {
                    logger.warning("Invoice ID " + invoiceId + " không có subscription_id liên kết.");
                }
            } else {
                logger.info("Invoice ID " + invoiceId + " không ở trạng thái PENDING (Status: " + invoice.getStatus() + "). Không cập nhật.");
            }
        } else {
            logger.warning("Không tìm thấy Invoice ID " + invoiceId + " khi cập nhật trạng thái.");
        }
    }
}