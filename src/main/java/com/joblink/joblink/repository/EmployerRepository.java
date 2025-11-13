package com.joblink.joblink.repository;

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

    /**
     * ✅ Lấy tất cả employers với user information (JOIN FETCH để tránh LazyInitializationException)
     */
    @Query("SELECT e FROM Employer e JOIN FETCH e.user ORDER BY e.id ASC")
    java.util.List<Employer> findAllWithUser();

    /**
     * Tìm kiếm và lọc employer IDs với các điều kiện (chỉ trả về ID)
     */
    @Query(value = """
        SELECT DISTINCT e.employer_id
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
          AND (:keyword IS NULL OR :keyword = '' OR 
               LOWER(e.company_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(e.phone_number) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR :status = '' OR
               (:status = 'active' AND u.enabled = 1 AND NOT (u.created_at >= DATEADD(DAY, -7, GETDATE()))) OR
               (:status = 'inactive' AND u.enabled = 0) OR
               (:status = 'pending' AND u.enabled = 1 AND u.created_at >= DATEADD(DAY, -7, GETDATE())))
          AND (:industry IS NULL OR :industry = '' OR LOWER(e.industry) LIKE LOWER(CONCAT('%', :industry, '%')))
          AND (:dateFilter IS NULL OR :dateFilter = '' OR CAST(u.created_at AS DATE) = CAST(:dateFilter AS DATE))
        ORDER BY e.employer_id ASC
        """, nativeQuery = true)
    java.util.List<Long> searchEmployerIds(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("industry") String industry,
            @Param("dateFilter") String dateFilter
    );

    /**
     * Tìm kiếm và lọc employer IDs với pagination
     */
    @Query(value = """
        SELECT DISTINCT e.employer_id
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
          AND (:keyword IS NULL OR :keyword = '' OR 
               LOWER(e.company_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(e.phone_number) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR :status = '' OR
               (:status = 'active' AND u.enabled = 1 AND NOT (u.created_at >= DATEADD(DAY, -7, GETDATE()))) OR
               (:status = 'inactive' AND u.enabled = 0) OR
               (:status = 'pending' AND u.enabled = 1 AND u.created_at >= DATEADD(DAY, -7, GETDATE())))
          AND (:industry IS NULL OR :industry = '' OR LOWER(e.industry) LIKE LOWER(CONCAT('%', :industry, '%')))
          AND (:dateFilter IS NULL OR :dateFilter = '' OR CAST(u.created_at AS DATE) = CAST(:dateFilter AS DATE))
        ORDER BY e.employer_id ASC
        OFFSET :offset ROWS
        FETCH NEXT :size ROWS ONLY
        """, nativeQuery = true)
    java.util.List<Long> searchEmployerIdsPaginated(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("industry") String industry,
            @Param("dateFilter") String dateFilter,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * Đếm số employer IDs phù hợp với điều kiện tìm kiếm
     */
    @Query(value = """
        SELECT COUNT(DISTINCT e.employer_id)
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
          AND (:keyword IS NULL OR :keyword = '' OR 
               LOWER(e.company_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(e.phone_number) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR :status = '' OR
               (:status = 'active' AND u.enabled = 1 AND NOT (u.created_at >= DATEADD(DAY, -7, GETDATE()))) OR
               (:status = 'inactive' AND u.enabled = 0) OR
               (:status = 'pending' AND u.enabled = 1 AND u.created_at >= DATEADD(DAY, -7, GETDATE())))
          AND (:industry IS NULL OR :industry = '' OR LOWER(e.industry) LIKE LOWER(CONCAT('%', :industry, '%')))
          AND (:dateFilter IS NULL OR :dateFilter = '' OR CAST(u.created_at AS DATE) = CAST(:dateFilter AS DATE))
        """, nativeQuery = true)
    long countSearchEmployerIds(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("industry") String industry,
            @Param("dateFilter") String dateFilter
    );

    /**
     * Lấy tất cả employer IDs với pagination (không có filter)
     */
    @Query(value = """
        SELECT DISTINCT e.employer_id
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
        ORDER BY e.employer_id ASC
        OFFSET :offset ROWS
        FETCH NEXT :size ROWS ONLY
        """, nativeQuery = true)
    java.util.List<Long> findAllEmployerIdsPaginated(
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * Đếm tổng số employer
     */
    @Query(value = """
        SELECT COUNT(DISTINCT e.employer_id)
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
        """, nativeQuery = true)
    long countAllEmployerIds();

    /**
     * Lấy employers theo list IDs với user information
     */
    @Query("SELECT e FROM Employer e JOIN FETCH e.user WHERE e.id IN :ids ORDER BY e.id ASC")
    java.util.List<Employer> findByIdsWithUser(@Param("ids") java.util.List<Long> ids);
}