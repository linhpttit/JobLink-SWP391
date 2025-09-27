package com.joblink.joblink.service;


import com.joblink.joblink.auth.model.User;
import com.joblink.joblink.auth.util.PasswordPolicy;
import com.joblink.joblink.dao.UserDao;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) { this.userDao = userDao; }

    public void register(String email, String password, String role) {
        // Validate role
        String r = role == null ? "" : role.toLowerCase();
        if (!r.equals("employer") && !r.equals("seeker")) {
            throw new IllegalArgumentException("Role chỉ được employer hoặc seeker");
        }
        // Validate password theo policy (để feedback sớm ở UI)
        if (!PasswordPolicy.isValid(password)) {
            throw new IllegalArgumentException("Mật khẩu phải >=8 ký tự, có ít nhất 1 chữ hoa, 1 số, 1 ký tự đặc biệt");
        }
        // Validate email sớm (DB cũng sẽ chặn)
        if (userDao.emailExists(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        // Gọi SP – SP cũng kiểm tra policy + unique
        try {
            userDao.register(email, password, r);
        } catch (DataAccessException ex) {
            // Nếu DB báo lỗi khác thì ném ra cho controller hiển thị
            throw ex;
        }
    }

    public User authenticate(String email, String password) {
        return userDao.login(email, password);
    }
}
