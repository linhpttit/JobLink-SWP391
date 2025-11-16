package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query(value = """
        SELECT 
            a.application_id, a.job_id, a.seeker_id, a.status, a.applied_at, 
            a.last_status_at, a.cv_url, a.note,
            jsp.fullname AS candidateName, jsp.email AS candidateEmail, 
            jsp.phone AS candidatePhone, 
            COALESCE(u.url_avt, jsp.avatar_url, 'https://i.pinimg.com/736x/d7/62/ea/d762eacadd9c9cc5e9c04244b711a972.jpg') AS avatarUrl,
            jsp.location AS location, jsp.experience_years AS experienceYears,
            jp.position AS position,
            edu.degree_level AS education,
            CASE WHEN eb.bookmark_id IS NOT NULL THEN 1 ELSE 0 END AS saved
        FROM Applications a
        INNER JOIN JobSeekerProfile jsp ON a.seeker_id = jsp.seeker_id
        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
        LEFT JOIN Users u ON jsp.user_id = u.user_id
        LEFT JOIN (
            SELECT seeker_id, MAX(degree_level) AS degree_level 
            FROM Education 
            GROUP BY seeker_id
        ) edu ON jsp.seeker_id = edu.seeker_id
        LEFT JOIN EmployerBookmarks eb 
            ON a.application_id = eb.application_id AND eb.employer_id = :employerId
        WHERE 
            (COALESCE(:search, '') = '' OR 
             LOWER(jsp.fullname) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.email) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.phone) LIKE CONCAT('%', LOWER(:search), '%'))
            AND (COALESCE(:positions, '') = '' OR jp.position IN (:positions))
            AND (:minExperience IS NULL OR jsp.experience_years >= :minExperience)
            AND (:maxExperience IS NULL OR jsp.experience_years <= :maxExperience)
            AND (COALESCE(:statuses, '') = '' OR a.status IN (:statuses))
            AND (COALESCE(:educationLevels, '') = '' OR edu.degree_level IN (:educationLevels))
            AND (COALESCE(:location, '') = '' OR LOWER(jsp.location) LIKE CONCAT('%', LOWER(:location), '%'))
        ORDER BY a.applied_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) 
        FROM Applications a
        INNER JOIN JobSeekerProfile jsp ON a.seeker_id = jsp.seeker_id
        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
        LEFT JOIN Users u ON jsp.user_id = u.user_id
        LEFT JOIN (
            SELECT seeker_id, MAX(degree_level) AS degree_level 
            FROM Education 
            GROUP BY seeker_id
        ) edu ON jsp.seeker_id = edu.seeker_id
        LEFT JOIN EmployerBookmarks eb 
            ON a.application_id = eb.application_id AND eb.employer_id = :employerId
        WHERE 
            (COALESCE(:search, '') = '' OR 
             LOWER(jsp.fullname) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.email) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.phone) LIKE CONCAT('%', LOWER(:search), '%'))
            AND (COALESCE(:positions, '') = '' OR jp.position IN (:positions))
            AND (:minExperience IS NULL OR jsp.experience_years >= :minExperience)
            AND (:maxExperience IS NULL OR jsp.experience_years <= :maxExperience)
            AND (COALESCE(:statuses, '') = '' OR a.status IN (:statuses))
            AND (COALESCE(:educationLevels, '') = '' OR edu.degree_level IN (:educationLevels))
            AND (COALESCE(:location, '') = '' OR LOWER(jsp.location) LIKE CONCAT('%', LOWER(:location), '%'))
        """,
            nativeQuery = true)
    Page<Object[]> findApplicationsWithFiltersNative(
            @Param("search") String search,
            @Param("positions") List<String> positions,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("statuses") List<String> statuses,
            @Param("educationLevels") List<String> educationLevels,
            @Param("location") String location,
            @Param("employerId") Long employerId,
            Pageable pageable
    );

    /**
     * Query lấy danh sách ứng viên ĐÃ BOOKMARK - ĐÃ CẬP NHẬT
     */
    @Query(value = """
        SELECT 
            a.application_id, a.job_id, a.seeker_id, a.status, a.applied_at, 
            a.last_status_at, a.cv_url, a.note,
            jsp.fullname AS candidateName, jsp.email AS candidateEmail, 
            jsp.phone AS candidatePhone, 
            COALESCE(u.url_avt, jsp.avatar_url, 'https://i.pinimg.com/736x/d7/62/ea/d762eacadd9c9cc5e9c04244b711a972.jpg') AS avatarUrl,
            jsp.location AS location, jsp.experience_years AS experienceYears,
            jp.position AS position,
            edu.degree_level AS education,
            1 AS saved
        FROM Applications a
        INNER JOIN JobSeekerProfile jsp ON a.seeker_id = jsp.seeker_id
        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
        LEFT JOIN Users u ON jsp.user_id = u.user_id
        LEFT JOIN (
            SELECT seeker_id, MAX(degree_level) AS degree_level 
            FROM Education 
            GROUP BY seeker_id
        ) edu ON jsp.seeker_id = edu.seeker_id
        INNER JOIN EmployerBookmarks eb 
            ON a.application_id = eb.application_id AND eb.employer_id = :employerId
        WHERE 
            (COALESCE(:search, '') = '' OR 
             LOWER(jsp.fullname) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.email) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.phone) LIKE CONCAT('%', LOWER(:search), '%'))
            AND (COALESCE(:positions, '') = '' OR jp.position IN (:positions))
            AND (:minExperience IS NULL OR jsp.experience_years >= :minExperience)
            AND (:maxExperience IS NULL OR jsp.experience_years <= :maxExperience)
            AND (COALESCE(:statuses, '') = '' OR a.status IN (:statuses))
            AND (COALESCE(:educationLevels, '') = '' OR edu.degree_level IN (:educationLevels))
            AND (COALESCE(:location, '') = '' OR LOWER(jsp.location) LIKE CONCAT('%', LOWER(:location), '%'))
        ORDER BY a.applied_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) 
        FROM Applications a
        INNER JOIN JobSeekerProfile jsp ON a.seeker_id = jsp.seeker_id
        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
        LEFT JOIN Users u ON jsp.user_id = u.user_id
        LEFT JOIN (
            SELECT seeker_id, MAX(degree_level) AS degree_level 
            FROM Education 
            GROUP BY seeker_id
        ) edu ON jsp.seeker_id = edu.seeker_id
        INNER JOIN EmployerBookmarks eb 
            ON a.application_id = eb.application_id AND eb.employer_id = :employerId
        WHERE 
            (COALESCE(:search, '') = '' OR 
             LOWER(jsp.fullname) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.email) LIKE CONCAT('%', LOWER(:search), '%') OR 
             LOWER(jsp.phone) LIKE CONCAT('%', LOWER(:search), '%'))
            AND (COALESCE(:positions, '') = '' OR jp.position IN (:positions))
            AND (:minExperience IS NULL OR jsp.experience_years >= :minExperience)
            AND (:maxExperience IS NULL OR jsp.experience_years <= :maxExperience)
            AND (COALESCE(:statuses, '') = '' OR a.status IN (:statuses))
            AND (COALESCE(:educationLevels, '') = '' OR edu.degree_level IN (:educationLevels))
            AND (COALESCE(:location, '') = '' OR LOWER(jsp.location) LIKE CONCAT('%', LOWER(:location), '%'))
        """,
            nativeQuery = true)
    Page<Object[]> findSavedApplicationsWithFiltersNative(
            @Param("search") String search,
            @Param("positions") List<String> positions,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            @Param("statuses") List<String> statuses,
            @Param("educationLevels") List<String> educationLevels,
            @Param("location") String location,
            @Param("employerId") Long employerId,
            Pageable pageable
    );

    /**
     * Query đơn giản - ĐÃ CẬP NHẬT
     */
    @Query(value = """
        SELECT 
            a.application_id, a.job_id, a.seeker_id, a.status, a.applied_at, 
            a.last_status_at, a.cv_url, a.note,
            jsp.fullname AS candidateName, jsp.email AS candidateEmail, 
            jsp.phone AS candidatePhone, 
            COALESCE(u.url_avt, jsp.avatar_url, 'https://i.pinimg.com/736x/d7/62/ea/d762eacadd9c9cc5e9c04244b711a972.jpg') AS avatarUrl,
            jsp.location AS location, jsp.experience_years AS experienceYears,
            jp.position AS position,
            edu.degree_level AS education,
            0 AS saved
        FROM Applications a
        INNER JOIN JobSeekerProfile jsp ON a.seeker_id = jsp.seeker_id
        INNER JOIN JobsPosting jp ON a.job_id = jp.job_id
        LEFT JOIN Users u ON jsp.user_id = u.user_id
        LEFT JOIN (
            SELECT seeker_id, MAX(degree_level) AS degree_level 
            FROM Education 
            GROUP BY seeker_id
        ) edu ON jsp.seeker_id = edu.seeker_id
        ORDER BY a.applied_at DESC
        """,
            nativeQuery = true)
    Page<Object[]> findApplicationsSimple(Pageable pageable);

    /**
     * Lấy danh sách các position distinct trong bảng JobsPosting, dùng cho filter dropdown
     */
    @Query(value = "SELECT DISTINCT position FROM JobsPosting WHERE position IS NOT NULL", nativeQuery = true)
    List<String> findDistinctPositions();

    /**
     * Lấy danh sách location distinct trong JobSeekerProfile, dùng cho filter dropdown
     */
    @Query(value = "SELECT DISTINCT location FROM JobSeekerProfile WHERE location IS NOT NULL", nativeQuery = true)
    List<String> findDistinctLocations();

    /**
     * Lấy danh sách degree_level distinct trong Education, dùng cho filter dropdown
     */
    @Query(value = "SELECT DISTINCT degree_level FROM Education WHERE degree_level IS NOT NULL", nativeQuery = true)
    List<String> findDistinctEducationLevels();

    /**
     * Lấy danh sách status distinct trong Applications, dùng cho filter dropdown
     */
    @Query(value = "SELECT DISTINCT status FROM Applications", nativeQuery = true)
    List<String> findDistinctStatuses();

    /**
     * Kiểm tra xem ứng viên đã apply job chưa
     */


    /**
     * Kiểm tra application đã được bookmark chưa - FIXED VERSION
     */
    @Query(value = "SELECT COUNT(*) FROM EmployerBookmarks WHERE application_id = :applicationId AND employer_id = :employerId", nativeQuery = true)
    int countBookmarks(@Param("applicationId") Long applicationId, @Param("employerId") Long employerId);

    /**
     * Kiểm tra application đã được bookmark chưa (wrapper method)
     */
    default boolean isApplicationBookmarked(Long applicationId, Long employerId) {
        return countBookmarks(applicationId, employerId) > 0;
    }

    /**
     * Thêm bookmark - FIXED VERSION cho SQL Server
     */
    @Modifying
    @Query(value = "INSERT INTO EmployerBookmarks (employer_id, application_id, created_at) VALUES (:employerId, :applicationId, GETDATE())", nativeQuery = true)
    void addBookmark(@Param("employerId") Long employerId, @Param("applicationId") Long applicationId);

    /**
     * Xóa bookmark - FIXED VERSION
     */
    @Modifying
    @Query(value = "DELETE FROM EmployerBookmarks WHERE employer_id = :employerId AND application_id = :applicationId", nativeQuery = true)
    void removeBookmark(@Param("employerId") Long employerId, @Param("applicationId") Long applicationId);
    boolean existsByJobIdAndSeekerId(Integer jobId, Integer seekerId);

    long count();

    long countByStatus(String status);

}