package com.joblink.joblink.service;

import com.joblink.joblink.dto.EmployerProfileDto;

public interface IEmployerService {
    boolean changePassword(String curPass, String newPass, String confirmPass);
    void editProfile(EmployerProfileDto employerProfileDto);
    EmployerProfileDto getActiveEmployerProfile();
}
