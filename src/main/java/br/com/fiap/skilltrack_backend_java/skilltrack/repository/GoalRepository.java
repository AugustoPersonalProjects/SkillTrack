package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findAllByOrderByIdAsc(Pageable pageable);
    Page<Goal> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);
    List<Goal> findBySkillIdOrderByDeadlineAsc(Long skillId);
    List<Goal> findTop5ByUserIdOrderByDeadlineAsc(Long userId);
    long countByUserId(Long userId);
    @Query("SELECT g FROM Goal g WHERE g.user.id = :userId")
    List<Goal> findByUserId(@Param("userId") Long userId);

}
