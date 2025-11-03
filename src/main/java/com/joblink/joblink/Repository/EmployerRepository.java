package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    /**
     * Tìm employer theo username (giữ lại nếu có nơi khác dùng)
     */
    @Query("SELECT e FROM Employer e JOIN FETCH e.user WHERE e.user.username = :username")
    Optional<Employer> findByUserUsername(@Param("username") String username);

    /**
     * ✅ METHOD CHÍNH - Tìm employer theo userId
     * JOIN FETCH để load cả User entity (tránh LazyInitializationException)
     */
    @Query("SELECT e FROM Employer e JOIN FETCH e.user WHERE e.user.userId = :userId")
    Optional<Employer> findByUserId(@Param("userId") Integer userId);

    /**
     * ✅ Kiểm tra email đã tồn tại (trừ employer hiện tại)
     * SỬA: Đổi kiểu :employerId từ Long → Integer hoặc để Spring tự cast
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employer e WHERE e.user.email = :email AND e.id <> :employerId")
    boolean existsByUserEmailAndIdNot(@Param("email") String email, @Param("employerId") Long employerId);

    /**
     * ✅ Kiểm tra phone đã tồn tại (trừ employer hiện tại)
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Employer e WHERE e.phoneNumber = :phoneNumber AND e.id <> :employerId")
    boolean existsByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("employerId") Long employerId);

	Optional<Employer> findFirstByUserEmailIgnoreCase(String email);
}