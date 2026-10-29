package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Page<Progress> findAllByOrderByIdAsc(Pageable pageable);
    Page<Progress> findBySkillUserIdOrderByIdAsc(Long userId, Pageable pageable);
    List<Progress> findBySkillIdOrderByDateAsc(Long skillId);

    // registro mais recente de cada skill do usuário: o de maior data e, no empate, o de maior id
    @Query("SELECT p FROM Progress p WHERE p.skill.user.id = :userId AND NOT EXISTS (" +
            "SELECT 1 FROM Progress p2 WHERE p2.skill = p.skill " +
            "AND (p2.date > p.date OR (p2.date = p.date AND p2.id > p.id)))")
    List<Progress> findLatestBySkillUserId(@Param("userId") Long userId);

    List<Progress> findTop5BySkillUserIdOrderByDateDescIdDesc(Long userId);

}