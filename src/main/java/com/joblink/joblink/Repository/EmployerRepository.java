package com.joblink.joblink.repository;

import com.joblink.joblink.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findByUserUsername(String email);
    Optional<Employer> findByUserId(Long userId);
    boolean existsByUserEmailAndIdNot(String email, Long id);
    boolean existsByPhoneNumberAndIdNot(String phone, Long id);

}
