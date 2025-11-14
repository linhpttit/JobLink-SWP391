package com.joblink.joblink.controller;

// domain User import removed; session holds UserSessionDTO now
import com.joblink.joblink.dto.PaymentFilterDTO;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.PaymentTransaction;
import com.joblink.joblink.repository.EmployerRepository;
import com.joblink.joblink.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmployerRepository employerRepository;

    private boolean ensureAdmin(HttpSession session) {
        com.joblink.joblink.dto.UserSessionDTO u = (com.joblink.joblink.dto.UserSessionDTO) session.getAttribute("user");
        return u != null && "admin".equalsIgnoreCase(u.getRole());
    }

    private void putUser(Model model, HttpSession session) {
        model.addAttribute("user", session.getAttribute("user"));
    }

    @GetMapping({"", "/"})
    public String adminShell(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "admin";
    }

  
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "dashboard";
    }

    @GetMapping("/jobseeker")
    public String jobseeker(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "jobseeker";
    }

    @GetMapping("/recruitment")
    public String recruitment(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "recruiment"; // template file is recruiment.html in templates
    }

    @GetMapping("/employer")
    public String employer(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "employer";
    }

    @GetMapping("/applications")
    public String applications(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "applications";
    }

    @GetMapping("/companies")
    public String companies(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "companies";
    }

    @GetMapping("/blog")
    public String blog(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "blog";
    }

    @GetMapping("/premium")
    public String premium(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "premium";
    }

    @GetMapping("/payments")
    public String payments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) Integer tierLevel,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, HttpSession session, HttpServletRequest request) {
        
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        
        // Create filter DTO from URL parameters
        PaymentFilterDTO filter = new PaymentFilterDTO();
        filter.setSearch(search);
        filter.setPaymentStatus(paymentStatus);
        filter.setTierLevel(tierLevel);
        filter.setPaymentMethod(paymentMethod);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy("createdAt");
        filter.setSortDirection("desc");
        
        // Get filtered data
        Page<PaymentTransaction> transactionPage = paymentService.getPaymentTransactionsForAdmin(filter);
        List<PaymentTransaction> transactions = transactionPage.getContent();
        
        // Get employer information for each transaction
        Map<Integer, Employer> employerMap = new HashMap<>();
        for (PaymentTransaction transaction : transactions) {
            if (transaction.getUser() != null && "employer".equalsIgnoreCase(transaction.getUser().getRole())) {
                Integer userId = transaction.getUser().getUserId();
                if (!employerMap.containsKey(userId)) {
                    Optional<Employer> employerOpt = employerRepository.findByUserId(userId);
                    employerOpt.ifPresent(employer -> employerMap.put(userId, employer));
                }
            }
        }
        
        // Get statistics with filter
        Map<String, Object> stats = paymentService.getPaymentStatisticsForAdmin(filter);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("employerMap", employerMap);
        model.addAttribute("currentPage", transactionPage.getNumber());
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("totalElements", transactionPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("successCount", stats.get("successCount"));
        model.addAttribute("failedCount", stats.get("failedCount"));
        model.addAttribute("totalRevenue", stats.get("totalRevenue"));
        
        // Add filter values back to form
        model.addAttribute("searchValue", search);
        model.addAttribute("paymentStatusValue", paymentStatus);
        model.addAttribute("tierLevelValue", tierLevel);
        model.addAttribute("paymentMethodValue", paymentMethod);
        
        // Check if this is an AJAX request (from admin.js loadContent function)
        String requestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        
        // If it's an AJAX request or fetch request, return the fragment
        if ("XMLHttpRequest".equals(requestedWith) || 
            (acceptHeader != null && acceptHeader.contains("text/html") && 
             request.getHeader("User-Agent") != null && 
             !request.getHeader("User-Agent").contains("Mozilla"))) {
            return "admin-payment-history-content";
        }
        
        // Otherwise return the full page
        return "admin-payment-history";
    }

    @PostMapping("/payments/filter")
    @ResponseBody
    public Map<String, Object> filterPayments(@RequestBody PaymentFilterDTO filter, HttpSession session) {
        if (!ensureAdmin(session)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized");
            return errorResponse;
        }

        // Lấy payment transactions với pagination và filtering
        Page<PaymentTransaction> transactionPage = paymentService.getPaymentTransactionsForAdmin(filter);
        List<PaymentTransaction> transactions = transactionPage.getContent();

        // Lấy thông tin employer cho mỗi transaction
        Map<Integer, Employer> employerMap = new HashMap<>();
        for (PaymentTransaction transaction : transactions) {
            if (transaction.getUser() != null && "employer".equalsIgnoreCase(transaction.getUser().getRole())) {
                Integer userId = transaction.getUser().getUserId();
                if (!employerMap.containsKey(userId)) {
                    Optional<Employer> employerOpt = employerRepository.findByUserId(userId);
                    employerOpt.ifPresent(employer -> employerMap.put(userId, employer));
                }
            }
        }

        // Lấy thống kê với filter
        Map<String, Object> stats = paymentService.getPaymentStatisticsForAdmin(filter);

        // Tạo response object
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("employerMap", employerMap);
        response.put("currentPage", filter.getPage());
        response.put("totalPages", transactionPage.getTotalPages());
        response.put("totalElements", transactionPage.getTotalElements());
        response.put("size", filter.getSize());
        response.put("successCount", stats.get("successCount"));
        response.put("failedCount", stats.get("failedCount"));
        response.put("totalRevenue", stats.get("totalRevenue"));

        return response;
    }

    @GetMapping("/feedbacks")
    public String feedbacks(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "feedbacks";
    }

    @GetMapping("/statistic")
    public String statistic(Model model, HttpSession session) {
        if (!ensureAdmin(session)) return "redirect:/signin";
        putUser(model, session);
        return "statistic";
    }
}
