package com.joblink.joblink.specification;

import com.joblink.joblink.dto.PaymentFilterDTO;
import com.joblink.joblink.entity.PaymentTransaction;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionSpecification {

    public static Specification<PaymentTransaction> withFilters(PaymentFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join với User để search (không cần employerJoin vì đã comment out employer relationship)
            Join<Object, Object> userJoin = root.join("user", JoinType.LEFT);
            
            // Search filter - tìm kiếm trong mã giao dịch, email, username
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                
                Predicate txnRefPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("vnpayTxnRef")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("email")), searchPattern);
                Predicate usernamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(userJoin.get("username")), searchPattern);
                
                predicates.add(criteriaBuilder.or(txnRefPredicate, emailPredicate, usernamePredicate));
            }

            // Payment status filter
            if (filter.getPaymentStatus() != null && !filter.getPaymentStatus().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), filter.getPaymentStatus()));
            }

            // Tier level filter
            if (filter.getTierLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("tierUpgradedTo"), filter.getTierLevel()));
            }

            // Payment method filter
            if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            // Date range filter
            if (filter.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }

            // Amount range filter
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }

            // Chỉ thêm fetch joins cho count queries (query == null) hoặc select queries
            if (query != null && query.getResultType() != null && query.getResultType().equals(Long.class)) {
                // Đây là count query, không cần fetch joins
                query.distinct(true);
            } else if (query != null) {
                // Đây là select query, có thể thêm fetch joins
                query.distinct(true);
                root.fetch("user", JoinType.LEFT);
                root.fetch("subscriptionPackage", JoinType.LEFT);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
