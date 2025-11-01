package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Integer> {
    // Thêm phương thức tìm kiếm Profile ID bằng User ID
    Optional<JobSeekerProfile> findByUserId(Integer userId);

    @Query(value = """
    SELECT j.*
    FROM JobSeekerProfile j
    INNER JOIN Users u ON j.user_id = u.user_id
    WHERE LOWER(u.role) = 'jobseeker'
      AND (
        :keyword IS NULL 
        OR :keyword = '' 
        OR LOWER(ISNULL(j.fullname, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(ISNULL(j.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (
        :experience IS NULL 
        OR (
          (:experience = 0 AND (j.experience_years = 0 OR j.experience_years IS NULL))
          OR (:experience = 1 AND j.experience_years = 1)
          OR (:experience = 2 AND j.experience_years = 2)
          OR (:experience = 3 AND j.experience_years >= 3)
        )
      )
      AND (
        :status IS NULL 
        OR :status = '' 
        OR (
          (:status = 'active' AND u.enabled = 1 AND (j.is_locked IS NULL OR j.is_locked = 0))
          OR (:status = 'locked' AND (u.enabled = 0 OR j.is_locked = 1))
        )
      )
    ORDER BY j.seeker_id ASC
    """, nativeQuery = true)
    List<JobSeekerProfile> searchJobSeekers(@Param("keyword") String keyword,
                                            @Param("experience") Integer experience,
                                            @Param("status") String status);

    @Query(value = """
    SELECT COUNT(*)
    FROM JobSeekerProfile j
    INNER JOIN Users u ON j.user_id = u.user_id
    WHERE LOWER(u.role) = 'jobseeker'
      AND (
        :keyword IS NULL 
        OR :keyword = '' 
        OR LOWER(ISNULL(j.fullname, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(ISNULL(j.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (
        :experience IS NULL 
        OR (
          (:experience = 0 AND (j.experience_years = 0 OR j.experience_years IS NULL))
          OR (:experience = 1 AND j.experience_years = 1)
          OR (:experience = 2 AND j.experience_years = 2)
          OR (:experience = 3 AND j.experience_years >= 3)
        )
      )
      AND (
        :status IS NULL 
        OR :status = '' 
        OR (
          (:status = 'active' AND u.enabled = 1 AND (j.is_locked IS NULL OR j.is_locked = 0))
          OR (:status = 'locked' AND (u.enabled = 0 OR j.is_locked = 1))
        )
      )
    """, nativeQuery = true)
    long countSearchJobSeekers(@Param("keyword") String keyword,
                               @Param("experience") Integer experience,
                               @Param("status") String status);


    @Query(value = """
    SELECT COUNT(*)
    FROM JobSeekerProfile j
    JOIN Users u ON j.user_id = u.user_id
    WHERE LOWER(u.role) = 'jobseeker'
    """, nativeQuery = true)
    long countJobSeekers();

    // Phương thức tìm kiếm với pagination
    @Query(value = """
    SELECT j.*
    FROM JobSeekerProfile j
    INNER JOIN Users u ON j.user_id = u.user_id
    WHERE LOWER(u.role) = 'jobseeker'
      AND (
        :keyword IS NULL 
        OR :keyword = '' 
        OR LOWER(ISNULL(j.fullname, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(ISNULL(j.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (
        :experience IS NULL 
        OR (
          (:experience = 0 AND (j.experience_years = 0 OR j.experience_years IS NULL))
          OR (:experience = 1 AND j.experience_years = 1)
          OR (:experience = 2 AND j.experience_years = 2)
          OR (:experience = 3 AND j.experience_years >= 3)
        )
      )
      AND (
        :status IS NULL 
        OR :status = '' 
        OR (
          (:status = 'active' AND u.enabled = 1 AND (j.is_locked IS NULL OR j.is_locked = 0))
          OR (:status = 'locked' AND (u.enabled = 0 OR j.is_locked = 1))
        )
      )
    ORDER BY j.seeker_id ASC
    OFFSET :offset ROWS
    FETCH NEXT :size ROWS ONLY
    """, nativeQuery = true)
    List<JobSeekerProfile> searchJobSeekersPaginated(@Param("keyword") String keyword,
                                                      @Param("experience") Integer experience,
                                                      @Param("status") String status,
                                                      @Param("offset") int offset,
                                                      @Param("size") int size);

}