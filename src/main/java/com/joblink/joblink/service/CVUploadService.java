
package com.joblink.joblink.service;

import com.joblink.joblink.dao.CVUploadDao;
import com.joblink.joblink.model.CVUpload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CVUploadService {
    private final CVUploadDao cvUploadDao;

    public CVUploadService(CVUploadDao cvUploadDao) {
        this.cvUploadDao = cvUploadDao;
    }

    @Transactional
    public int saveCV(CVUpload cv) {
        validateCV(cv);
        return cvUploadDao.create(cv);
    }

    public List<CVUpload> getRecentCVs(int seekerId, int limit) {
        return cvUploadDao.findRecentBySeekerId(seekerId, limit);
    }

    public CVUpload getCVById(int cvId) {
        return cvUploadDao.findById(cvId);
    }

    private void validateCV(CVUpload cv) {
        if (cv.getFullName() == null || cv.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (cv.getEmail() == null || cv.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (cv.getPhoneNumber() == null || cv.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (cv.getCvFileUrl() == null || cv.getCvFileUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("CV file is required");
        }
        if (cv.getYearsOfExperience() != null && (cv.getYearsOfExperience() < 0 || cv.getYearsOfExperience() >= 100)) {
            throw new IllegalArgumentException("Years of experience must be between 0 and 99");
        }
    }
}
