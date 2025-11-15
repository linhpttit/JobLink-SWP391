// File: com/joblink/joblink/repository/JobPostingRepository.java (ĐÃ SỬA LỖI HOÀN CHỈNH)
package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // Tìm các tin đăng theo ID của nhà tuyển dụng
    List<JobPosting> findByEmployerId(Long employerId);

    // SỬA LỖI: Đổi tên phương thức để khớp với đường dẫn thuộc tính "category.categoryId"
    // Đồng thời đổi Integer thành Long cho nhất quán
    List<JobPosting> findTop3ByCategoryCategoryIdAndJobIdNotOrderByPostedAtDesc(int categoryId, Long excludeJobId);
    @Query("SELECT j FROM JobPosting j " +
            "WHERE j.skill.skillId = :skillId " +
            "AND j.jobId <> :excludeJobId " +
            "AND j.status = 'ACTIVE' " +
            "ORDER BY j.postedAt DESC")
    List<JobPosting> findBySkillSkillIdAndJobIdNot(@Param("skillId") Long skillId,
                                                   @Param("excludeJobId") Long excludeJobId);

    long count();
    List<JobPosting> findTop10ByStatusOrderByPostedAtDesc(String status);

    @Query("SELECT DISTINCT j FROM JobPosting j " +
            "LEFT JOIN FETCH j.employer e " +
            "LEFT JOIN FETCH j.province p " +
            "LEFT JOIN FETCH j.district d " +
            "LEFT JOIN FETCH j.skill s " +
            "WHERE (LOWER(p.provinceName) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "   OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND j.status = 'ACTIVE' " +
            "ORDER BY j.postedAt DESC")
    List<JobPosting> findByLocationContaining(@Param("location") String location);

    // Tìm job theo skill name (vì JobPosting có trường skill trực tiếp)
    @Query("SELECT DISTINCT j FROM JobPosting j " +
            "LEFT JOIN FETCH j.employer e " +
            "LEFT JOIN FETCH j.province p " +
            "LEFT JOIN FETCH j.skill s " +
            "WHERE LOWER(s.name) IN :skillNames " +
            "AND j.status = 'ACTIVE' " +
            "ORDER BY j.postedAt DESC")
    List<JobPosting> findBySkillNames(@Param("skillNames") List<String> skillNames);

    // Tìm job theo skill ID
    @Query("SELECT j FROM JobPosting j " +
            "LEFT JOIN FETCH j.employer e " +
            "LEFT JOIN FETCH j.province p " +
            "WHERE j.skill.skillId = :skillId " +
            "AND j.status = 'ACTIVE' " +
            "ORDER BY j.postedAt DESC")
    List<JobPosting> findBySkillId(@Param("skillId") Long skillId);

    // Tìm job theo mức lương tối thiểu
    @Query("SELECT j FROM JobPosting j " +
            "LEFT JOIN FETCH j.employer e " +
            "LEFT JOIN FETCH j.province p " +
            "WHERE j.status = 'ACTIVE' " +
            "AND j.salaryMin >= :minSalary " +
            "ORDER BY j.salaryMin DESC")
    List<JobPosting> findByMinSalaryGreaterThanEqual(@Param("minSalary") java.math.BigDecimal minSalary);
    long countByStatus(String status);
    
}