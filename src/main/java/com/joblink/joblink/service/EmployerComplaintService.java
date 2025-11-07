package com.joblink.joblink.service;

import com.joblink.joblink.Repository.EmployerComplaintRepository;
import com.joblink.joblink.Repository.EmployerRepository;
import com.joblink.joblink.Repository.JobSeekerProfileRepository;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.EmployerComplaint;
import com.joblink.joblink.entity.JobSeekerProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerComplaintService implements IEmployerComplaintService {
    private final EmployerComplaintRepository complaintRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerProfileRepository jobSeekerRepository;

//    @Override
//    public EmployerComplaint createComplaint(int jobSeekerId, Long employerId, String subject, String content) {
//        JobSeekerProfile jobSeeker = jobSeekerRepository.findById(jobSeekerId)
//                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));
//        Employer employer = employerRepository.findById(employerId)
//                .orElseThrow(() -> new RuntimeException("Employer not found"));
//
//        EmployerComplaint complaint = EmployerComplaint.builder()
//                .jobSeeker(jobSeeker)
//                .employer(employer)
//                .subject(subject)
//                .content(content)
//                .build();
//
//        return complaintRepository.save(complaint);
//    }

    @Override
    public List<EmployerComplaint> getComplaintsByEmployer(Long employerId) {
        return complaintRepository.findByEmployerId(employerId);
    }

    @Override
    public List<EmployerComplaint> getComplaintsByJobSeeker(int jobSeekerId) {
        return complaintRepository.findByJobSeekerSeekerId(jobSeekerId);
    }

    @Override
    public EmployerComplaint respondToComplaint(Long complaintId, String response) {
        EmployerComplaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setResponse(response);
        complaint.setStatus("RESOLVED");
        return complaintRepository.save(complaint);
    }
    @Override
    public void updateStatus(Long id, String status) {
        EmployerComplaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setStatus(status);
        complaintRepository.save(complaint);
    }
    @Override
    public List<EmployerComplaint> searchComplaintsByEmployer(Long employerId, String status, String keyword) {
        if (status != null && status.isBlank()) status = null;
        if (keyword != null && keyword.isBlank()) keyword = null;
        return complaintRepository.searchByEmployer(employerId, status, keyword);
    }

}
