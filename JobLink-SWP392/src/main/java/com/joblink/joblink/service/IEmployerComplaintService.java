package com.joblink.joblink.service;

import com.joblink.joblink.entity.EmployerComplaint;

import java.util.List;

public interface IEmployerComplaintService {
//    EmployerComplaint createComplaint(int jobSeekerId, Long employerId, String subject, String content);
    List<EmployerComplaint> getComplaintsByEmployer(Long employerId);
    List<EmployerComplaint> getComplaintsByJobSeeker(int jobSeekerId);
    EmployerComplaint respondToComplaint(Long complaintId, String response);
    void updateStatus(Long id, String status);
    List<EmployerComplaint> searchComplaintsByEmployer(Long employerId, String status, String keyword);
    void respondAndUpdateStatus(Long id, String response, String status);
    EmployerComplaint getComplaintById(Long id);
}
