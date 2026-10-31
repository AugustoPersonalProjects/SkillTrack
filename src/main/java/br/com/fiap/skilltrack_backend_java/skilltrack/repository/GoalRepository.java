package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    Page<Goal> findAllByOrderByIdAsc(Pageable pageable);
    Page<Goal> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);
    List<Goal> findBySkillIdOrderByDeadlineAsc(Long skillId);
    List<Goal> findTop5ByUserIdAndCompletedFalseOrderByDeadlineAsc(Long userId);
    long countByUserId(Long userId);
    @Query("SELECT g FROM Goal g WHERE g.user.id = :userId")
    List<Goal> findByUserId(@Param("userId") Long userId);

    long countByCompletedTrue();

    interface TotalPorUsuario {
        Long getIdUsuario();
        long getTotal();
        long getConcluidas();
    }

    @Query("SELECT g.user.id AS idUsuario, COUNT(g) AS total, SUM(CASE WHEN g.completed = true THEN 1 ELSE 0 END) AS concluidas " +
            "FROM Goal g WHERE g.user IS NOT NULL GROUP BY g.user.id")
    List<TotalPorUsuario> countGroupedByUser();

    long countByCompletedFalseAndDeadlineBefore(LocalDate data);

    @Query("SELECT g.dataCriacao FROM Goal g WHERE g.dataCriacao >= :inicio")
    List<Instant> findDatasDeCriacaoDesde(@Param("inicio") Instant inicio);

    @Query("SELECT g.dataConclusao FROM Goal g WHERE g.dataConclusao >= :inicio")
    List<Instant> findDatasDeConclusaoDesde(@Param("inicio") Instant inicio);

    long countByDataCriacaoIsNull();

}
