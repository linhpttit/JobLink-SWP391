package com.joblink.joblink.service;

import com.joblink.joblink.dao.*;
import com.joblink.joblink.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

    private final JobSeekerProfileDao profileDao;
    private final EducationDao educationDao;
    private final ExperienceDao experienceDao;
    private final SkillDao skillDao;
    private final LanguageDao languageDao;
    private final CertificateDao certificateDao;

    public ProfileService(JobSeekerProfileDao profileDao,
                          EducationDao educationDao,
                          ExperienceDao experienceDao,
                          SkillDao skillDao,
                          LanguageDao languageDao,
                          CertificateDao certificateDao) {
        this.profileDao = profileDao;
        this.educationDao = educationDao;
        this.experienceDao = experienceDao;
        this.skillDao = skillDao;
        this.languageDao = languageDao;
        this.certificateDao = certificateDao;
    }

    /** Lấy hồ sơ theo userId; nếu chưa có thì tạo mới theo userId (không chèn seeker_id) rồi đọc lại. */
    @Transactional
    public JobSeekerProfile2 getOrCreateProfile(int userId) {
        var p = profileDao.findByUserId(userId);
        if (p == null) {
            profileDao.createEmptyProfileByUserId(userId);
            p = profileDao.findByUserId(userId); // lấy lại seeker_id (identity)
            if (p == null) throw new IllegalStateException("Cannot create profile for user_id=" + userId);
        }
        return p;
    }


    public JobSeekerProfile2 getProfileBySeekerId(int seekerId) {
        return profileDao.findBySeekerId(seekerId);
    }

    @Transactional
    public void updateBasicInfo(JobSeekerProfile2 profile) {
        profileDao.updateProfile(profile);

    }

    @Transactional
    public int updateCompletionPercentage(int seekerId) {
        int percentage = calculateCompletionPercentage(seekerId);
        profileDao.updateCompletionPercentage(seekerId, percentage);
        return percentage;
    }

    // ========= Education =========
    public List<Education> getEducations(int seekerId) { return educationDao.findBySeekerId(seekerId); }
    @Transactional public int  addEducation(Education e) { return educationDao.create(e); }
    @Transactional public void updateEducation(Education e) { educationDao.update(e); }
    @Transactional public void deleteEducation(int id) { educationDao.delete(id); }

    // ========= Experience =========
    public List<Experience> getExperiences(int seekerId) { return experienceDao.findBySeekerId(seekerId); }
    @Transactional public int  addExperience(Experience e) { return experienceDao.create(e); }
    @Transactional public void updateExperience(Experience e) { experienceDao.update(e); }
    @Transactional public void deleteExperience(int id) { experienceDao.delete(id); }

    // ========= Skill =========
    public List<Skill2> getSkills(int seekerId) { return skillDao.findBySeekerId(seekerId); }
    @Transactional public int  addSkill(Skill2 s) { return skillDao.create(s); }
    @Transactional public void updateSkill(Skill2 s) { skillDao.update(s); }
    @Transactional public void deleteSkill(int id) { skillDao.delete(id); }

    // ========= Language =========
    public List<Language> getLanguages(int seekerId) { return languageDao.findBySeekerId(seekerId); }
    @Transactional public int  addLanguage(Language l) { return languageDao.create(l); }
    @Transactional public void updateLanguage(Language l) { languageDao.update(l); }
    @Transactional public void deleteLanguage(int id) { languageDao.delete(id); }

    // ========= Certificate =========
    public List<Certificate> getCertificates(int seekerId) { return certificateDao.findBySeekerId(seekerId); }
    @Transactional public int  addCertificate(Certificate c) { return certificateDao.create(c); }
    @Transactional public void updateCertificate(Certificate c) { certificateDao.update(c); }
    @Transactional public void deleteCertificate(int id) { certificateDao.delete(id); }

    /** Tính % hoàn thành (đơn giản) */
    /** Tính % hoàn thành (đã bao gồm Skills và Certificates) */
    public int calculateCompletionPercentage(int seekerId) {
        JobSeekerProfile2 p = profileDao.findBySeekerId(seekerId);
        if (p == null) return 0;

        // 1. Tăng tổng số mục từ 7 lên 9
        int total = 9, done = 0;

        if (nonEmpty(p.getFullname())) done++;
        if (nonEmpty(p.getGender())) done++;
        if (nonEmpty(p.getLocation())) done++;
        if (nonEmpty(p.getHeadline())) done++;
        if (nonEmpty(p.getAbout())) done++;
        if (!educationDao.findBySeekerId(seekerId).isEmpty()) done++;
        if (!experienceDao.findBySeekerId(seekerId).isEmpty()) done++;

        // 2. Thêm kiểm tra cho Skills
        if (!skillDao.findBySeekerId(seekerId).isEmpty()) done++;

        // 3. Thêm kiểm tra cho Certificates
        if (!certificateDao.findBySeekerId(seekerId).isEmpty()) done++;

        return (done * 100) / total;
    }
    public JobSeekerProfile2 getProfileByUserId(int userId) {
        return profileDao.findByUserId(userId);
    }
    public List<String> getMissingSections(int seekerId) {
        JobSeekerProfile2 p = profileDao.findBySeekerId(seekerId);
        List<String> miss = new ArrayList<>();
        if (p == null) { miss.add("Profile not found"); return miss; }

        // Các mục cơ bản
        if (!nonEmpty(p.getFullname())) miss.add("Full Name");
        if (!nonEmpty(p.getGender()))   miss.add("Gender");
        if (!nonEmpty(p.getLocation())) miss.add("Location");
        if (!nonEmpty(p.getHeadline())) miss.add("Headline");
        if (!nonEmpty(p.getAbout()))    miss.add("About");

        // Các mục danh sách
        if (educationDao.findBySeekerId(seekerId).isEmpty())  miss.add("Education");
        if (experienceDao.findBySeekerId(seekerId).isEmpty()) miss.add("Experience");

        // === BỔ SUNG 2 DÒNG CÒN THIẾU ===
        if (skillDao.findBySeekerId(seekerId).isEmpty())      miss.add("Skills");
        if (certificateDao.findBySeekerId(seekerId).isEmpty()) miss.add("Certificates");

        return miss;
    }

    private boolean nonEmpty(String s) { return s != null && !s.trim().isEmpty(); }
}
