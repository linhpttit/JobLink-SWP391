package com.joblink.joblink.dao;

import com.joblink.joblink.auth.model.BlockedEmployer; // Sửa lại package model nếu cần
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BlockedEmployerDao {

    private final JdbcTemplate jdbcTemplate;

    // --- ROW MAPPER ĐÃ BỔ SUNG ---
    private final RowMapper<BlockedEmployer> blockedEmployerRowMapper = (rs, rowNum) -> {
        BlockedEmployer block = new BlockedEmployer();
        block.setBlockId(rs.getInt("block_id"));
        block.setSeekerId(rs.getInt("seeker_id"));
        block.setEmployerId(rs.getInt("employer_id"));
        if (rs.getTimestamp("created_at") != null) {
            block.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return block;
    };

    public void block(int seekerId, int employerId) {
        String sql = "INSERT INTO BlockedEmployers (seeker_id, employer_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, seekerId, employerId);
    }

    public void unblock(int seekerId, int employerId) {
        String sql = "DELETE FROM BlockedEmployers WHERE seeker_id = ? AND employer_id = ?";
        jdbcTemplate.update(sql, seekerId, employerId);
    }

    // --- PHƯƠNG THỨC ĐÃ HOÀN THIỆN ---
    public List<BlockedEmployer> findBySeekerId(int seekerId) {
        String sql = "SELECT * FROM BlockedEmployers WHERE seeker_id = ?";
        return jdbcTemplate.query(sql, blockedEmployerRowMapper, seekerId);
    }
}