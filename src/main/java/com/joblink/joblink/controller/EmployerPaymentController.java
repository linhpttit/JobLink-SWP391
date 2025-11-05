package com.joblink.joblink.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment-history")
public class EmployerPaymentController {
    @GetMapping
    public String viewPaymentHistory(Model model){
        return "employer/payment-history";
    }
}
