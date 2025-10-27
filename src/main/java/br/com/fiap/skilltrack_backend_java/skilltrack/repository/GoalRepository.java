package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findAllByOrderByIdAsc(Pageable pageable);
    List<Goal> findBySkillIdOrderByDeadlineAsc(Long skillId);
    List<Goal> findTop5ByOrderByDeadlineAsc();

}
