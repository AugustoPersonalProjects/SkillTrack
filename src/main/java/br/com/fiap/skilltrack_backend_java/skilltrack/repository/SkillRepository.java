package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Page<Skill> findAllByOrderByIdAsc(Pageable pageable);
    Page<Skill> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    List<Skill> findByUserIdOrderByNameAsc(Long userId);
}