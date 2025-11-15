package com.joblink.joblink.service;

import com.joblink.joblink.config.VNPayConfig;
import com.joblink.joblink.dto.PaymentFilterDTO;
import com.joblink.joblink.entity.*;
import com.joblink.joblink.Repository.*;
import com.joblink.joblink.specification.PaymentTransactionSpecification;
import com.joblink.joblink.util.VNPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentService {

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private SubscriptionPackageRepository packageRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    /**
     * Lấy tất cả gói subscription đang hoạt động
     */
    public List<SubscriptionPackage> getAllActivePackages() {
        return packageRepository.findByIsActiveTrueOrderByTierLevelAsc();
    }

    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(Integer userId, Long packageId, String ipAddress) throws UnsupportedEncodingException {
        // Lấy thông tin user và package
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPackage subscriptionPackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        // Tạo transaction reference (mã giao dịch)
        String txnRef = "TXN" + System.currentTimeMillis() + VNPayUtil.getRandomNumber(4);

        // Tạo pending transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUser(user);
        transaction.setSubscriptionPackage(subscriptionPackage);
        transaction.setVnpayTxnRef(txnRef);
        transaction.setAmount(subscriptionPackage.getPrice());
        transaction.setPaymentStatus("PENDING");
        transaction.setTransactionInfo("Thanh toán gói " + subscriptionPackage.getPackageName());

        // Set employer nếu có (tạm thời comment out)
        // if (employerOpt.isPresent()) {
        //     transaction.setEmployer(employerOpt.get());
        // }

        transactionRepository.save(transaction);

        // Tạo parameters cho VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(subscriptionPackage.getPrice() * 100)); // VNPay tính bằng đồng, nhân 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);

        // Chuẩn hóa OrderInfo: loại bỏ ký tự đặc biệt, chỉ giữ chữ cái, số, dấu cách
        String packageName = subscriptionPackage.getPackageName()
                .replaceAll("[^a-zA-Z0-9\\s]", "") // Loại bỏ ký tự đặc biệt
                .replaceAll("\\s+", " ")            // Chuẩn hóa khoảng trắng
                .trim();
        vnpParams.put("vnp_OrderInfo", "Thanh toan goi " + packageName);
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);

        // Tạo createDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = LocalDateTime.now().format(formatter);
        vnpParams.put("vnp_CreateDate", createDate);

        // Tạo expireDate (15 phút)
        String expireDate = LocalDateTime.now().plusMinutes(15).format(formatter);
        vnpParams.put("vnp_ExpireDate", expireDate);

        // Build hash data và tạo secure hash (KHÔNG có vnp_SecureHash trong hash)
        String hashData = VNPayUtil.buildHashData(vnpParams);
        String secureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

        // Build query string (KHÔNG bao gồm vnp_SecureHash)
        String queryUrl = VNPayUtil.buildQueryString(vnpParams);

        // Append vnp_SecureHash TRỰC TIẾP (không encode) theo Java demo
        queryUrl += "&vnp_SecureHash=" + secureHash;

        return vnPayConfig.getApiUrl() + "?" + queryUrl;
    }

    /**
     * Xử lý callback từ VNPay
     */
    @Transactional
    public Map<String, Object> processPaymentReturn(Map<String, String> vnpParams) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Lấy secure hash từ params
            String vnpSecureHash = vnpParams.get("vnp_SecureHash");

            // Remove các params không cần thiết cho việc verify
            vnpParams.remove("vnp_SecureHash");
            vnpParams.remove("vnp_SecureHashType");

            // Verify secure hash
            String hashData = VNPayUtil.buildHashData(vnpParams);
            String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

            if (!calculatedHash.equals(vnpSecureHash)) {
                result.put("success", false);
                result.put("message", "Chữ ký không hợp lệ");
                return result;
            }

            // Lấy thông tin transaction
            String txnRef = vnpParams.get("vnp_TxnRef");
            String responseCode = vnpParams.get("vnp_ResponseCode");
            String bankCode = vnpParams.get("vnp_BankCode");
            String paymentMethod = vnpParams.get("vnp_CardType");

            PaymentTransaction transaction = transactionRepository.findByVnpayTxnRef(txnRef)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            // Cập nhật transaction
            transaction.setVnpayResponseCode(responseCode);
            transaction.setBankCode(bankCode);
            transaction.setPaymentMethod(paymentMethod);

            if ("00".equals(responseCode)) {
                // Thanh toán thành công
                transaction.setPaymentStatus("SUCCESS");
                transaction.setPaidAt(LocalDateTime.now());

                // Nâng cấp tier cho employer
                upgradeEmployerTier(transaction);

                result.put("success", true);
                result.put("message", "Thanh toán thành công");
                result.put("transactionId", transaction.getTransactionId());
            } else {
                // Thanh toán thất bại
                transaction.setPaymentStatus("FAILED");
                result.put("success", false);
                result.put("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
            }

            transactionRepository.save(transaction);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi xử lý thanh toán: " + e.getMessage());
        }

        return result;
    }

    /**
     * Nâng cấp tier cho employer sau khi thanh toán thành công
     */
    private void upgradeEmployerTier(PaymentTransaction transaction) {
        User user = transaction.getUser();

        // Chỉ nâng cấp nếu user là employer
        if (!"employer".equalsIgnoreCase(user.getRole())) {
            return;
        }

        Optional<Employer> employerOpt = employerRepository.findByUserId(user.getUserId());
        Employer employer;

        if (employerOpt.isPresent()) {
            employer = employerOpt.get();
        } else {
            // Tạo EmployerProfile mới nếu chưa có
            employer = new Employer();
            employer.setUser(user);
            employer.setCompanyName("Chưa cập nhật");
            employer.setIndustry("Chưa cập nhật");
            employer.setLocation("Chưa cập nhật");
            employer.setPhoneNumber("Chưa cập nhật");
            employer.setDescription("Chưa cập nhật");
        }

        SubscriptionPackage pkg = transaction.getSubscriptionPackage();

        // Cập nhật tier level
        employer.setTierLevel(pkg.getTierLevel());

        // Tính ngày hết hạn subscription
        LocalDateTime expiresAt;
        if (employer.getSubscriptionExpiresAt() != null &&
                employer.getSubscriptionExpiresAt().isAfter(LocalDateTime.now())) {
            // Nếu subscription hiện tại còn hạn, cộng thêm vào
            expiresAt = employer.getSubscriptionExpiresAt().plusDays(pkg.getDurationDays());
        } else {
            // Nếu hết hạn hoặc chưa có, tính từ bây giờ
            expiresAt = LocalDateTime.now().plusDays(pkg.getDurationDays());
        }
        employer.setSubscriptionExpiresAt(expiresAt);

        employerRepository.save(employer);

        // Lưu tier đã nâng cấp vào transaction và cập nhật employer reference
        transaction.setTierUpgradedTo(pkg.getTierLevel());
        // transaction.setEmployer(employer); // Tạm thời comment out
    }

    /**
     * Lấy lịch sử thanh toán của user
     */
    public List<PaymentTransaction> getPaymentHistory(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Lấy thông tin tier hiện tại của employer
     */
    public Map<String, Object> getEmployerTierInfo(Integer userId) {
        Map<String, Object> info = new HashMap<>();

        Optional<Employer> employerOpt = employerRepository.findByUserId(userId);
        if (employerOpt.isPresent()) {
            Employer employer = employerOpt.get();
            // Default tier 0 (Free) cho employer mới
            Integer tierLevel = employer.getTierLevel() != null ? employer.getTierLevel() : 0;

            // Kiểm tra xem subscription có còn hạn không
            boolean isActive = employer.getSubscriptionExpiresAt() != null &&
                    employer.getSubscriptionExpiresAt().isAfter(LocalDateTime.now());

            // Nếu subscription hết hạn và không phải tier Free, tìm gói còn hạn cao nhất để reset
            if (!isActive && tierLevel > 0) {
                Map<String, Object> validSubscription = findHighestValidSubscription(userId);

                if ((Boolean) validSubscription.get("found")) {
                    // Có gói còn hạn, reset về gói đó
                    Integer newTierLevel = (Integer) validSubscription.get("tierLevel");
                    LocalDateTime newExpiryDate = (LocalDateTime) validSubscription.get("expiryDate");

                    employer.setTierLevel(newTierLevel);
                    employer.setSubscriptionExpiresAt(newExpiryDate);

                    tierLevel = newTierLevel;
                    isActive = true; // Cập nhật lại trạng thái active

                    System.out.println("Auto-reset expired subscription for userId: " + userId +
                            " to tier " + newTierLevel + " (expires: " + newExpiryDate + ")");
                } else {
                    // Không có gói nào còn hạn, reset về Free tier
                    tierLevel = 0;
                    employer.setTierLevel(0);
                    employer.setSubscriptionExpiresAt(null);

                    System.out.println("Auto-reset expired subscription for userId: " + userId + " to Free tier");
                }

                employerRepository.save(employer);
            }

            info.put("tierLevel", tierLevel);
            info.put("subscriptionExpiresAt", employer.getSubscriptionExpiresAt());
            info.put("isSubscriptionActive", isActive);

            // Đếm số bài đăng hiện tại của employer
            Long employerId = employer.getId();
            int currentJobPosts = jobPostingRepository.findByEmployerId(employerId).size();
            info.put("currentJobPosts", currentJobPosts);

            // Lấy giới hạn bài đăng theo tier (sử dụng tierLevel đã được reset nếu cần)
            int maxJobPosts = getMaxJobPostsByTier(tierLevel);
            info.put("maxJobPosts", maxJobPosts);

        } else {
            // Nếu không tìm thấy employer profile, default là Free tier
            info.put("tierLevel", 0);
            info.put("subscriptionExpiresAt", null);
            info.put("isSubscriptionActive", false);
            info.put("currentJobPosts", 0);
            info.put("maxJobPosts", getMaxJobPostsByTier(0));
        }

        return info;
    }

    /**
     * Tìm gói subscription còn hạn với tier cao nhất từ PaymentTransaction
     */
    public Map<String, Object> findHighestValidSubscription(Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Lấy tất cả transaction SUCCESS của user
            List<PaymentTransaction> successTransactions = transactionRepository
                    .findByUserUserIdAndPaymentStatusOrderByCreatedAtDesc(userId, "SUCCESS");

            LocalDateTime now = LocalDateTime.now();
            PaymentTransaction bestTransaction = null;
            Integer highestTier = 0;
            LocalDateTime latestExpiry = null;

            for (PaymentTransaction transaction : successTransactions) {
                if (transaction.getPaidAt() != null && transaction.getSubscriptionPackage() != null) {
                    // Tính ngày hết hạn của transaction này
                    LocalDateTime expiryDate = transaction.getPaidAt()
                            .plusDays(transaction.getSubscriptionPackage().getDurationDays());

                    // Kiểm tra xem còn hạn không
                    if (expiryDate.isAfter(now)) {
                        Integer tierLevel = transaction.getSubscriptionPackage().getTierLevel();

                        // Tìm tier cao nhất, nếu tier bằng nhau thì lấy cái hết hạn muộn nhất
                        if (tierLevel > highestTier ||
                                (tierLevel.equals(highestTier) && (latestExpiry == null || expiryDate.isAfter(latestExpiry)))) {
                            highestTier = tierLevel;
                            latestExpiry = expiryDate;
                            bestTransaction = transaction;
                        }
                    }
                }
            }

            if (bestTransaction != null) {
                result.put("found", true);
                result.put("tierLevel", highestTier);
                result.put("expiryDate", latestExpiry);
                result.put("packageName", bestTransaction.getSubscriptionPackage().getPackageName());
                result.put("transaction", bestTransaction);
            } else {
                result.put("found", false);
                result.put("tierLevel", 0); // Default to Free
                result.put("expiryDate", null);
            }

        } catch (Exception e) {
            System.err.println("Error in findHighestValidSubscription: " + e.getMessage());
            result.put("found", false);
            result.put("tierLevel", 0);
            result.put("expiryDate", null);
        }

        return result;
    }

    /**
     * Lấy giới hạn số bài đăng theo tier từ database
     */
    private int getMaxJobPostsByTier(Integer tierLevel) {
        try {
            // Lấy package từ database theo tier level
            Optional<SubscriptionPackage> packageOpt = packageRepository.findByTierLevel(tierLevel);

            if (packageOpt.isPresent()) {
                SubscriptionPackage pkg = packageOpt.get();
                String features = pkg.getFeatures();

                if (features != null && !features.isEmpty()) {
                    // Parse JSON để lấy max_jobs
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(features);

                    if (jsonNode.has("max_jobs")) {
                        return jsonNode.get("max_jobs").asInt();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing features JSON: " + e.getMessage());
        }

        // Fallback: Nếu không parse được, dùng giá trị mặc định
        // Tier >= 3: Không giới hạn
        if (tierLevel >= 3) {
            return Integer.MAX_VALUE; // Không giới hạn
        }

        return switch (tierLevel) {
            case 0 -> 3;      // Free: 3 bài
            case 1 -> 10;     // Standard: 10 bài
            case 2 -> 30;     // Pro: 30 bài
            default -> 3;
        };
    }

    /**
     * Lấy thông tin package theo ID
     */
    public SubscriptionPackage getPackageById(Long packageId) {
        return packageRepository.findById(packageId).orElse(null);
    }

    /**
     * Lấy package theo tier level
     */
    public SubscriptionPackage getPackageByTierLevel(Integer tierLevel) {
        return packageRepository.findByTierLevel(tierLevel).orElse(null);
    }

    /**
     * Lấy lịch sử thanh toán gần đây
     */
    public List<PaymentTransaction> getRecentPaymentHistory(Integer userId, int limit) {
        List<PaymentTransaction> allTransactions = transactionRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        return allTransactions.stream()
                .limit(limit)
                .toList();
    }

    /**
     * Lấy tất cả payment transactions cho admin (bao gồm thông tin user và employer)
     */
    public List<PaymentTransaction> getAllPaymentTransactionsForAdmin() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Lấy payment transactions với pagination và filtering cho admin
     */
    public Page<PaymentTransaction> getPaymentTransactionsForAdmin(PaymentFilterDTO filter) {
        // Tạo Pageable object
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // Tạo Specification từ filter
        Specification<PaymentTransaction> spec = PaymentTransactionSpecification.withFilters(filter);

        return transactionRepository.findAll(spec, pageable);
    }

    /**
     * Lấy thống kê payment cho admin với filter
     */
    public Map<String, Object> getPaymentStatisticsForAdmin(PaymentFilterDTO filter) {
        Map<String, Object> stats = new HashMap<>();

        // Nếu không có filter, lấy tất cả
        List<PaymentTransaction> transactions;
        if (filter == null || isEmptyFilter(filter)) {
            transactions = getAllPaymentTransactionsForAdmin();
        } else {
            // Apply filter nhưng không pagination để tính thống kê chính xác
            Specification<PaymentTransaction> spec = PaymentTransactionSpecification.withFilters(filter);
            transactions = transactionRepository.findAll(spec);
        }

        long successCount = transactions.stream()
                .filter(t -> "SUCCESS".equals(t.getPaymentStatus()))
                .count();

        long failedCount = transactions.stream()
                .filter(t -> "FAILED".equals(t.getPaymentStatus()) || "CANCELLED".equals(t.getPaymentStatus()))
                .count();

        long totalRevenue = transactions.stream()
                .filter(t -> "SUCCESS".equals(t.getPaymentStatus()))
                .mapToLong(PaymentTransaction::getAmount)
                .sum();

        stats.put("totalTransactions", transactions.size());
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    /**
     * Lấy thống kê payment cho admin (backward compatibility)
     */
    public Map<String, Object> getPaymentStatisticsForAdmin() {
        return getPaymentStatisticsForAdmin(null);
    }

    /**
     * Kiểm tra xem filter có rỗng không
     */
    private boolean isEmptyFilter(PaymentFilterDTO filter) {
        return (filter.getSearch() == null || filter.getSearch().trim().isEmpty()) &&
                (filter.getPaymentStatus() == null || filter.getPaymentStatus().trim().isEmpty()) &&
                filter.getTierLevel() == null &&
                (filter.getPaymentMethod() == null || filter.getPaymentMethod().trim().isEmpty()) &&
                filter.getFromDate() == null &&
                filter.getToDate() == null &&
                filter.getMinAmount() == null &&
                filter.getMaxAmount() == null;
    }

    /**
     * Kích hoạt gói Free (price = 0) trực tiếp không cần thanh toán
     */
    @Transactional
    public boolean activateFreePackage(Integer userId, Long packageId) {
        try {
            // Lấy thông tin user và package
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            SubscriptionPackage subscriptionPackage = packageRepository.findById(packageId)
                    .orElseThrow(() -> new RuntimeException("Package not found"));

            // Kiểm tra package có phải Free (price = 0) không
            if (subscriptionPackage.getPrice() != 0) {
                throw new RuntimeException("This package is not free");
            }

            // Lấy hoặc tạo employer profile
            Optional<Employer> employerOpt = employerRepository.findByUserId(userId);
            Employer employer;

            if (employerOpt.isPresent()) {
                employer = employerOpt.get();
            } else {
                // Tạo EmployerProfile mới nếu chưa có
                employer = new Employer();
                employer.setUser(user);
                employer.setCompanyName("Chưa cập nhật");
                employer.setIndustry("Chưa cập nhật");
                employer.setLocation("Chưa cập nhật");
                employer.setPhoneNumber("Chưa cập nhật");
                employer.setDescription("Chưa cập nhật");
            }

            // Cập nhật tier và subscription expiry
            employer.setTierLevel(subscriptionPackage.getTierLevel());
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(subscriptionPackage.getDurationDays());
            employer.setSubscriptionExpiresAt(expiresAt);
            employerRepository.save(employer);

            // Tạo payment transaction cho tracking (không cần VNPay)
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setUser(user);
            transaction.setSubscriptionPackage(subscriptionPackage);
            // transaction.setEmployer(employer); // Tạm thời comment out
            transaction.setVnpayTxnRef("FREE" + System.currentTimeMillis());
            transaction.setAmount(0L);
            transaction.setPaymentStatus("SUCCESS");
            transaction.setVnpayResponseCode("00");
            transaction.setPaymentMethod("FREE");
            transaction.setTransactionInfo("Free tier activation");
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setTierUpgradedTo(subscriptionPackage.getTierLevel());
            transactionRepository.save(transaction);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
