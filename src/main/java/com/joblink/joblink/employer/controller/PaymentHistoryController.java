package com.joblink.joblink.employer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.joblink.joblink.employer.application.service.IEmployerService;
import com.joblink.joblink.employer.application.service.IPaymentHistoryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employer")
public class PaymentHistoryController {
	private final IPaymentHistoryService service;
    private final IEmployerService employerService; 

    @GetMapping("/payment-history")
    public String payments(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {

        Long userId = employerService.getCurrentEmployerId(); 
        var history = service.getHistory(userId, page, size);

        model.addAttribute("rows", history.getContent());
        model.addAttribute("page", history);
        return "employer/payment-history";
    }
}
