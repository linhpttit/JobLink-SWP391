package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository để truy xuất bảng Users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Tìm user theo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm user theo username
     */
    Optional<User> findByUsername(String username);

    /**
     * Tìm user theo Google ID
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    boolean existsByUsername(String username);
}