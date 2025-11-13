package com.joblink.joblink.service;

import com.joblink.joblink.auth.model.EmployerProfile;
//import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.dao.EmployerProfileDao;
import com.joblink.joblink.dto.EmployerProfileDto;
import com.joblink.joblink.entity.Employer;
import com.joblink.joblink.entity.User; // ← SỬ DỤNG Entity User
import com.joblink.joblink.repository.EmployerRepository;
import com.joblink.joblink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployerService implements IEmployerService {
    private final EmployerRepository employerReposistory;
    private final PasswordEncoder passwordEncoder;
    private final EmployerProfileDao employerProfileDao;
    private final UserRepository userRepository;

    @Override
    public boolean changePassword(Integer userId, String curPass, String newPass, String confirmPass) {
        System.out.println("=== CHANGE PASSWORD ===");
        System.out.println("User ID: " + userId);

        // Tìm hoặc tạo employer profile
        Employer employer = getOrCreateEmployerProfile(userId);

        System.out.println("✅ Tìm thấy employer ID: " + employer.getId());
        System.out.println("   Company: " + employer.getCompanyName());

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(curPass, employer.getUser().getPasswordHash())) {
            System.err.println("❌ Mật khẩu hiện tại không đúng");
            return false;
        }

        // Kiểm tra mật khẩu mới khớp với xác nhận
        if (!newPass.equals(confirmPass)) {
            System.err.println("❌ Mật khẩu mới và xác nhận không khớp");
            return false;
        }

        // Cập nhật mật khẩu mới
        employer.getUser().setPasswordHash(passwordEncoder.encode(newPass));
        employerReposistory.save(employer);
        System.out.println("✅ Đổi mật khẩu thành công");
        return true;
    }

    @Override
    public void editProfile(Integer userId, EmployerProfileDto employerProfileDto) {
        System.out.println("=== EDIT PROFILE ===");
        System.out.println("User ID: " + userId);
        System.out.println("New Company Name: " + employerProfileDto.getCompanyName());

        // Tìm hoặc tạo employer profile
        Employer employer = getOrCreateEmployerProfile(userId);

        System.out.println("✅ Tìm thấy employer ID: " + employer.getId());

        // Kiểm tra email trùng (nếu có thay đổi)
        if (employerProfileDto.getEmail() != null &&
                !employerProfileDto.getEmail().equals(employer.getUser().getEmail())) {
            if (employerReposistory.existsByUserEmailAndIdNot(employerProfileDto.getEmail(), employer.getId())) {
                System.err.println("❌ Email đã tồn tại: " + employerProfileDto.getEmail());
                throw new IllegalArgumentException("Email đã tồn tại. Vui lòng nhập email khác.");
            }
        }

        // Kiểm tra phone trùng (nếu có thay đổi)
        if (employerProfileDto.getPhoneNumber() != null &&
                !employerProfileDto.getPhoneNumber().equals(employer.getPhoneNumber())) {
            if (employerReposistory.existsByPhoneNumberAndIdNot(employerProfileDto.getPhoneNumber(), employer.getId())) {
                System.err.println("❌ Số điện thoại đã tồn tại: " + employerProfileDto.getPhoneNumber());
                throw new IllegalArgumentException("Số điện thoại đã tồn tại. Vui lòng nhập số điện thoại khác.");
            }
        }

        // Cập nhật thông tin
        if (employerProfileDto.getCompanyName() != null) {
            employer.setCompanyName(employerProfileDto.getCompanyName());
        }
        if (employerProfileDto.getAddress() != null) {
            employer.setLocation(employerProfileDto.getAddress());
        }
        if (employerProfileDto.getPhoneNumber() != null) {
            employer.setPhoneNumber(employerProfileDto.getPhoneNumber());
        }
        if (employerProfileDto.getEmail() != null) {
            employer.getUser().setEmail(employerProfileDto.getEmail());
        }
        if (employerProfileDto.getDescription() != null) {
            employer.setDescription(employerProfileDto.getDescription());
        }
        if (employerProfileDto.getUrlAvt() != null && !employerProfileDto.getUrlAvt().isEmpty()) {
            employer.getUser().setUrlAvt(employerProfileDto.getUrlAvt());
        }

        employerReposistory.save(employer);
        System.out.println("✅ Cập nhật profile thành công");
    }

    @Override
    public EmployerProfileDto getActiveEmployerProfile(Integer userId) {
        System.out.println("=== GET PROFILE ===");
        System.out.println("User ID: " + userId);

        // Tìm hoặc tạo employer profile
        Employer employer = getOrCreateEmployerProfile(userId);

        System.out.println("✅ Tìm thấy employer: " + employer.getCompanyName());

        // Chuyển đổi sang DTO
        EmployerProfileDto dto = new EmployerProfileDto();
        dto.setCompanyName(employer.getCompanyName());
        dto.setAddress(employer.getLocation());
        dto.setPhoneNumber(employer.getPhoneNumber());
        dto.setEmail(employer.getUser().getEmail());
        dto.setDescription(employer.getDescription());
        dto.setUrlAvt(employer.getUser().getUrlAvt());

        // Format ngày tạo
        if (employer.getUser().getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dto.setCreatedAt(employer.getUser().getCreatedAt().toLocalDate().format(formatter));
        }

        return dto;
    }

    /**
     * ✅ TỰ ĐỘNG TẠO EMPLOYER PROFILE NẾU CHƯA CÓ
     * Giống logic của JobSeeker
     */
    private Employer getOrCreateEmployerProfile(Integer userId) {
        // Tìm employer đã tồn tại
        Optional<Employer> existing = employerReposistory.findByUserId(userId);
        if (existing.isPresent()) {
            System.out.println("✅ Employer profile đã tồn tại");
            return existing.get();
        }

        System.out.println("⚠️ Chưa có employer profile, đang tạo mới...");

        // Lấy thông tin User entity (không phải auth.model.User)
        User user = getUserById(userId);

        // Tạo Employer mới
        Employer newEmployer = new Employer();
        newEmployer.setUser(user);

        // Tên công ty mặc định từ email
        String defaultCompanyName = user.getEmail().split("@")[0];
        newEmployer.setCompanyName(defaultCompanyName);
        newEmployer.setLocation("Chưa cập nhật");
        newEmployer.setPhoneNumber("");
        newEmployer.setDescription("");
        newEmployer.setIndustry("Chưa cập nhật");

        Employer saved = employerReposistory.save(newEmployer);
        System.out.println("✅ Đã tạo employer profile mới với ID: " + saved.getId());
        System.out.println("   Company name: " + saved.getCompanyName());

        return saved;
    }

    /**
     * Helper method để lấy User Entity
     */
    private User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại với ID: " + userId));
    }

    // Methods cũ - giữ lại
    public EmployerProfile getProfileByEmployerId(int employerId) {
        return employerProfileDao.findById(employerId);
    }

    public List<EmployerProfile> searchOpenEmployers(String keyword, String location, String industry, String sortBy) {
        return List.of();
    }
}