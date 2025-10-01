package com.joblink.joblink.service;

import com.joblink.joblink.dto.EmployerProfileDto;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.repository.EmployerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class EmployerService implements IEmployerService{
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean changePassword(String curPass, String newPass, String confirmPass) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Employer employer = employerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if(passwordEncoder.matches(curPass,employer.getUser().getPasswordHash()) && newPass.equals(confirmPass)){
            employer.getUser().setPasswordHash(passwordEncoder.encode(newPass));
            employerRepository.save(employer);
            return true;
        }
        return false;
    }

    @Override
    public void editProfile(EmployerProfileDto employerProfileDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Employer employer = employerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (employerRepository.existsByUserEmailAndIdNot(employerProfileDto.getEmail(), employer.getId())) {
            throw new IllegalArgumentException("Email đã tồn tại.Vui lòng nhập số email khác ");
        } else if (employerRepository.existsByPhoneNumberAndIdNot(employerProfileDto.getPhoneNumber(),employer.getId())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại. Vui lòng nhập số điện thoại khác.") ;
        } else {
            employer.setCompanyName(employerProfileDto.getCompanyName());
            employer.setLocation(employerProfileDto.getAddress());
            employer.setPhoneNumber(employerProfileDto.getPhoneNumber());
            employer.getUser().setEmail(employerProfileDto.getEmail());
            employer.setDescription(employerProfileDto.getDescription());
            employer.getUser().setUrlAvt(employerProfileDto.getUrlAvt());
            employerRepository.save(employer);
        }
    }

    @Override
    public EmployerProfileDto getActiveEmployerProfile() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String username = auth.getName();
        String username = "testUser";
        Employer employer = employerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        EmployerProfileDto dto = new EmployerProfileDto();
        dto.setCompanyName(employer.getCompanyName());
        dto.setAddress(employer.getLocation());
        dto.setPhoneNumber(employer.getPhoneNumber());
        dto.setEmail(employer.getUser().getEmail());
        dto.setDescription(employer.getDescription());
        dto.setUrlAvt(employer.getUser().getUrlAvt());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dto.setCreatedAt(employer.getUser().getCreatedAt().toLocalDate().format(formatter));
        return dto;

    }
}
