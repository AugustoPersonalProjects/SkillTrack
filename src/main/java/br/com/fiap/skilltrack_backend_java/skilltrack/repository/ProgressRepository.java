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

    // nome da skill + média de progresso, na mesma ordem (skills sem progresso aparecem com 0)
    @Query("SELECT s.name, COALESCE(AVG(p.percentage), 0) FROM Skill s LEFT JOIN s.progresses p GROUP BY s.id, s.name ORDER BY s.name")
    List<Object[]> findAverageProgressBySkill();

    List<Progress> findTop5ByOrderByDateDesc();

}