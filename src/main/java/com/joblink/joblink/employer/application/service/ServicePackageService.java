package com.joblink.joblink.employer.application.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.joblink.joblink.employer.application.model.ServicePackageVM;
import com.joblink.joblink.entity.PremiumPackages;
import com.joblink.joblink.repository.PremiumPackageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class ServicePackageService implements IServicePackageService {

    private final PremiumPackageRepository repo;

    @Override
    @Transactional
    public List<ServicePackageVM> listEmployerPackages() {
        return repo.findByUserTypeAndIsActiveOrderByPriceAsc("EMPLOYER", true)
                   .stream().map(this::toVm).toList();
    }

    private ServicePackageVM toVm(PremiumPackages p) {
        int weeks = Math.max(1, (p.getDurationDays() == null ? 28 : p.getDurationDays()) / 7);
        boolean isNew = p.getCreatedAt() != null
                && ChronoUnit.DAYS.between(p.getCreatedAt(), OffsetDateTime.now()) <= 30;

        return ServicePackageVM.builder()
                .id(p.getPackageId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getFeatures())
                .duration(weeks)
                .price(p.getPrice())
                .originalPrice((BigDecimal) null) 
                .flash(Boolean.TRUE.equals(p.getHighlight()))
                .isNew(isNew)
                .bannerUrl(null)   
                .helpUrl(null)
                .quantity(1)
                .scope(ServicePackageVM.Scope.builder()
                        .locations(null).levels(null).industries(null).build())
                .scopeDetailUrl(null)
                .build();
    }
}
