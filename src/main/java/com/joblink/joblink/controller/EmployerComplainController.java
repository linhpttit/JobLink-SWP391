package com.joblink.joblink.controller;

import com.joblink.joblink.dto.UserSessionDTO;
import com.joblink.joblink.entity.EmployerComplaint;
import com.joblink.joblink.service.IEmployerComplaintService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employer-response")
public class EmployerComplainController {
    private final IEmployerComplaintService employerComplaintService;

    @GetMapping
    public String viewComplaintPage(Model model, HttpSession session, RedirectAttributes ra){
        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }
        Long currentEmployerId = Long.valueOf(user.getUserId());
        List<EmployerComplaint> complaints = complaintService.getComplaintsByEmployer((Long) currentEmployerId);

        model.addAttribute("complaints", complaints);
        return "employer/employer-response";
    }
    private final IEmployerComplaintService complaintService;

//    @PostMapping("/create")
//    public EmployerComplaint createComplaint(
//            @RequestParam Long jobSeekerId,
//            @RequestParam Long employerId,
//            @RequestParam String subject,
//            @RequestParam String content) {
//        return complaintService.createComplaint(jobSeekerId, employerId, subject, content);
//    }

    @GetMapping("/employer/{employerId}")
    public List<EmployerComplaint> getComplaintsByEmployer(@PathVariable Long employerId) {
        return complaintService.getComplaintsByEmployer(employerId);
    }

    @GetMapping("/jobseeker/{jobSeekerId}")
    public List<EmployerComplaint> getComplaintsByJobSeeker(@PathVariable int jobSeekerId) {
        return complaintService.getComplaintsByJobSeeker(jobSeekerId);
    }

    @PostMapping("/{complaintId}/respond")
    public EmployerComplaint respondToComplaint(@PathVariable Long complaintId,
                                                @RequestParam String response) {
        return complaintService.respondToComplaint(complaintId, response);
    }
    @PostMapping("/update-status/{id}")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        complaintService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filters")
    public String viewComplaintPage(@RequestParam(required = false) String status,
                                    @RequestParam(required = false) String keyword,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes ra) {

        UserSessionDTO user = (UserSessionDTO) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/signin";
        }

        Long currentEmployerId = Long.valueOf(user.getUserId());

        List<EmployerComplaint> complaints =
                complaintService.searchComplaintsByEmployer(currentEmployerId, status, keyword);

        model.addAttribute("complaints", complaints);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        return "employer/employer-response";
    }
    @GetMapping("/respond/{id}")
    public String viewComplaintDetailPage(@PathVariable Long id, Model model) {
        EmployerComplaint complaint = complaintService.getComplaintById(id);
        model.addAttribute("complaint", complaint);
        return "employer/complaint-detail";
    }
    @PostMapping("/respond/{id}")
    public String handleComplaintResponse(@PathVariable Long id,
                                          @RequestParam String response,
                                          @RequestParam String status,
                                          RedirectAttributes ra) {
        complaintService.respondAndUpdateStatus(id, response, status);
        ra.addFlashAttribute("success", "Cập nhật khiếu nại thành công!");
        return "redirect:/employer-response";
    }
}
