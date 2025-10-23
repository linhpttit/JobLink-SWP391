package com.joblink.joblink.service;

import com.joblink.joblink.dao.JobSeekerProfileDao;
import com.joblink.joblink.dao.SkillDao;
import com.joblink.joblink.model.JobSeekerProfile;
import com.joblink.joblink.model.Skill;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerService {

    private final JobSeekerProfileDao profileDao;
    private final SkillDao skillDao;

    public JobSeekerService(JobSeekerProfileDao profileDao, SkillDao skillDao) {
        this.profileDao = profileDao;
        this.skillDao = skillDao;
    }

    /** Lấy hồ sơ theo seeker_id (khi đã có PK) */
    public JobSeekerProfile getProfileBySeekerId(int seekerId) {
        return profileDao.findBySeekerId(seekerId);
    }

    /** ✅ Dùng ở tất cả nơi có user đang đăng nhập */
    public JobSeekerProfile getProfileByUserId(int userId) {
        return profileDao.findByUserId(userId);
    }

    /** Danh sách seeker có kỹ năng trùng (cho Networking) */
    public List<JobSeekerProfile> findSeekersWithOverlappingSkills(int seekerId) {
        return profileDao.findSeekersWithOverlappingSkills(seekerId);
    }

    /** Danh sách kỹ năng của seeker (type-safe) */
    public List<Skill> getSeekerSkills(int seekerId) {
        return skillDao.findBySeekerId(seekerId);
    }

    /** Cập nhật hồ sơ theo seeker_id */
    public void updateProfile(JobSeekerProfile profile) {
        profileDao.updateProfile(profile);
    }

    /** (Tuỳ chọn) Tự tạo hồ sơ rỗng nếu chưa có */
    public void ensureProfileForUser(int userId) {
        if (profileDao.findByUserId(userId) == null) {
            profileDao.createEmptyProfileByUserId(userId);
        }
    }
}
