package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.SkillLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Page<Skill> findAllByOrderByIdAsc(Pageable pageable);
    Page<Skill> findByUserIdOrderByIdAsc(Long userId, Pageable pageable);
    long countByUserId(Long userId);
    List<Skill> findByUserIdOrderByNameAsc(Long userId);

    interface TotalPorUsuario {
        Long getIdUsuario();
        long getTotal();
    }

    @Query("SELECT s.user.id AS idUsuario, COUNT(s) AS total FROM Skill s WHERE s.user IS NOT NULL GROUP BY s.user.id")
    List<TotalPorUsuario> countGroupedByUser();

    interface TotalPorCategoria {
        String getCategoria();
        long getTotal();
    }

    // inclui as categorias sem nenhuma skill
    @Query("SELECT c.name AS categoria, COUNT(s) AS total FROM Category c LEFT JOIN Skill s ON s.category = c " +
            "GROUP BY c.id, c.name ORDER BY COUNT(s) DESC, c.name ASC")
    List<TotalPorCategoria> countGroupedByCategory();

    interface TotalPorNivel {
        SkillLevel getNivel();
        long getTotal();
    }

    @Query("SELECT s.level AS nivel, COUNT(s) AS total FROM Skill s GROUP BY s.level")
    List<TotalPorNivel> countGroupedByLevel();

    @Query("SELECT s.dataCriacao FROM Skill s WHERE s.dataCriacao >= :inicio")
    List<Instant> findDatasDeCriacaoDesde(@Param("inicio") Instant inicio);

    long countByDataCriacaoIsNull();
}