package com.joblink.joblink.Repository;

import com.joblink.joblink.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findBySkillId(Integer id);
}
