package com.joblink.joblink.repository;

import com.joblink.joblink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    double getTotalRevenue();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'seeker'")
    long countJobSeekers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'employer'")
    long countEmployers();

    long countByRole(String role);

    @Query("SELECT u FROM User u WHERE u.role = 'seeker'")
    List<User> findAllJobSeekers();

    // Đếm job seekers đang hoạt động: isLocked = false/null và receiveInvitations = true
    @Query(value = """
        SELECT COUNT(j.seeker_id)
        FROM JobSeekerProfile j
        INNER JOIN Users u ON j.user_id = u.user_id
        WHERE LOWER(u.role) = 'seeker'
          AND (j.is_locked IS NULL OR j.is_locked = 0)
          AND (j.receive_invitations IS NULL OR j.receive_invitations = 1)
        """, nativeQuery = true)
    long countActiveJobSeekers();

    // Đếm job seekers đã khóa: isLocked = true hoặc receiveInvitations = false
    @Query(value = """
        SELECT COUNT(j.seeker_id)
        FROM JobSeekerProfile j
        INNER JOIN Users u ON j.user_id = u.user_id
        WHERE LOWER(u.role) = 'seeker'
          AND (j.is_locked = 1 OR j.receive_invitations = 0)
        """, nativeQuery = true)
    long countLockedJobSeekers();

    // Đếm employers đã được chấp nhận: approved = true
    @Query(value = """
        SELECT COUNT(e.employer_id)
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
          AND e.approved = 1
        """, nativeQuery = true)
    long countActiveEmployers();

    // Đếm employers bị từ chối: approved = false
    @Query(value = """
        SELECT COUNT(e.employer_id)
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
          AND e.approved = 0
        """, nativeQuery = true)
    long countInactiveEmployers();

    // Đếm employers chờ xét duyệt: approved IS NULL
    @Query(value = """
        SELECT COUNT(e.employer_id)
        FROM EmployerProfile e
        INNER JOIN Users u ON e.user_id = u.user_id
        WHERE LOWER(u.role) = 'employer'
          AND e.approved IS NULL
        """, nativeQuery = true)
    long countPendingEmployers();

}
