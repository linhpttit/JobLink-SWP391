package com.joblink.joblink.service;



import com.joblink.joblink.Repository.UserRepository;
import com.joblink.joblink.dao.JobSeekerProfileDao;
import com.joblink.joblink.dao.SkillDao;
import com.joblink.joblink.entity.JobSeekerProfile;
import com.joblink.joblink.model.JobSeekerProfile2;
import com.joblink.joblink.Repository.JobSeekerProfileRepository;
import com.joblink.joblink.entity.User;
import com.joblink.joblink.model.Skill2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class JobSeekerService implements IJobSeekerService {
    private final JobSeekerProfileDao profileDao;
    private final SkillDao skillDao;
    public JobSeekerService(JobSeekerProfileDao profileDao, SkillDao skillDao) {
        this.profileDao = profileDao;
        this.skillDao = skillDao;
    }
    @Autowired
    private JobSeekerProfileRepository jobSeekerRepository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public JobSeekerProfile getByUserId(Integer userId) {
        return jobSeekerRepository.findByUserId(userId)
                .orElse(null); // Controller có check null
    }

    public List<JobSeekerProfile> search(String keyword, Integer experience, String status) {
        // Chuẩn hóa các tham số
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String normalizedStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // Xử lý experience: nếu là 3+ năm thì search >= 3
        Integer normalizedExperience = experience;
        if (experience != null && experience == 3) {
            // Trường hợp này sẽ cần xử lý riêng nếu muốn tìm >= 3 năm
            // Tạm thời giữ nguyên logic cũ
        }

        // Lấy tất cả kết quả không có pagination
        return jobSeekerRepository.searchJobSeekers(normalizedKeyword, normalizedExperience, normalizedStatus);
    }

    // Phương thức tìm kiếm với pagination
    public List<JobSeekerProfile> searchPaginated(String keyword, Integer experience, String status, int page, int size) {
        // Chuẩn hóa các tham số
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String normalizedStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        Integer normalizedExperience = experience;

        // Tính offset: page bắt đầu từ 1, nhưng offset bắt đầu từ 0
        int offset = (page - 1) * size;

        // Lấy kết quả với pagination
        return jobSeekerRepository.searchJobSeekersPaginated(normalizedKeyword, normalizedExperience, normalizedStatus, offset, size);
    }

    public long countSearch(String keyword, Integer experience, String status) {
        // Chuẩn hóa các tham số
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String normalizedStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        Integer normalizedExperience = experience;
        return jobSeekerRepository.countSearchJobSeekers(normalizedKeyword, normalizedExperience, normalizedStatus);
    }


    // Thống kê
    public long countAll() {
        return jobSeekerRepository.count();
    }

    public long countJobSeeker() {
        return jobSeekerRepository.countJobSeekers();
    }
    public long countActive() {
        return userRepository.countActiveJobSeekers();
    }

    public long countLocked() {
        return userRepository.countLockedJobSeekers();
    }

    public long countCV() {
        // Giả lập — sau này bạn có thể thay bằng truy vấn bảng CV thực tế
        return (long) (jobSeekerRepository.count() * 0.8);
    }

    // Xóa mềm - chuyển từ đang hoạt động sang đã khóa
    public boolean softDelete(Integer seekerId) {
        try {
            JobSeekerProfile profile = jobSeekerRepository.findById(seekerId)
                    .orElse(null);
            
            if (profile == null) {
                return false;
            }
            
            // Chuyển sang trạng thái đã khóa
            profile.setIsLocked(true);
            profile.setReceiveInvitations(false);
            profile.setUpdatedAt(LocalDateTime.now());
            
            jobSeekerRepository.save(profile);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xóa mềm job seeker: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public JobSeekerProfile2 getProfileBySeekerId(int seekerId) {
        return profileDao.findBySeekerId(seekerId);
    }

    /** ✅ Dùng ở tất cả nơi có user đang đăng nhập */
    public JobSeekerProfile2 getProfileByUserId(int userId) {
        return profileDao.findByUserId(userId);
    }

    /** Danh sách seeker có kỹ năng trùng (cho Networking) */
    public List<JobSeekerProfile2> findSeekersWithOverlappingSkills(int seekerId) {
        return profileDao.findSeekersWithOverlappingSkills(seekerId);
    }

    /** Danh sách kỹ năng của seeker (type-safe) */
    public List<Skill2> getSeekerSkills(int seekerId) {
        return skillDao.findBySeekerId(seekerId);
    }

    /** Cập nhật hồ sơ theo seeker_id */
    public void updateProfile(JobSeekerProfile2 profile) {
        profileDao.updateProfile(profile);
    }

    /** (Tuỳ chọn) Tự tạo hồ sơ rỗng nếu chưa có */
    public void ensureProfileForUser(int userId) {
        if (profileDao.findByUserId(userId) == null) {
            profileDao.createEmptyProfileByUserId(userId);
        }
    }
}