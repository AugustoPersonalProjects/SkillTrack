package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    Page<Progress> findAllByOrderByIdAsc(Pageable pageable);
    List<Progress> findBySkillIdOrderByDateAsc(Long skillId);
    @Query("SELECT AVG(p.percentage) FROM Progress p")
    Double findAverageProgress();

    @Query("SELECT s.name FROM Skill s")
    List<String> findSkillNames();

    @Query("SELECT AVG(p.percentage) FROM Progress p GROUP BY p.skill.name")
    List<Double> findProgressBySkill();

    List<Progress> findTop5ByOrderByDateDesc();

}