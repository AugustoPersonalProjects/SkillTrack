package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.CorAvatar;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // registro mais recente de cada skill: o de maior data e, no empate, o de maior id
    String ULTIMO_REGISTRO_DA_SKILL = "NOT EXISTS (" +
            "SELECT 1 FROM Progress p2 WHERE p2.skill = p.skill " +
            "AND (p2.date > p.date OR (p2.date = p.date AND p2.id > p.id)))";

    Page<Progress> findAllByOrderByIdAsc(Pageable pageable);
    Page<Progress> findBySkillUserIdOrderByIdAsc(Long userId, Pageable pageable);
    List<Progress> findBySkillIdOrderByDateAsc(Long skillId);

    @Query("SELECT p FROM Progress p WHERE p.skill.user.id = :userId AND " + ULTIMO_REGISTRO_DA_SKILL)
    List<Progress> findLatestBySkillUserId(@Param("userId") Long userId);

    List<Progress> findTop5BySkillUserIdOrderByDateDescIdDesc(Long userId);

    @Query("SELECT COALESCE(SUM(p.percentage), 0) FROM Progress p WHERE " + ULTIMO_REGISTRO_DA_SKILL)
    long sumLatestPercentages();

    @Query("SELECT p.date FROM Progress p WHERE p.date BETWEEN :inicio AND :fim")
    List<LocalDate> findDatasEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(DISTINCT p.skill.user.id) FROM Progress p WHERE p.date BETWEEN :inicio AND :fim")
    long countUsuariosComProgressoEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    interface UsuarioAtivo {
        Long getIdUsuario();
        String getNome();
        CorAvatar getCorAvatar();
        long getRegistros();
    }

    @Query("SELECT u.id AS idUsuario, u.nome AS nome, u.corAvatar AS corAvatar, COUNT(p) AS registros " +
            "FROM Progress p JOIN p.skill s JOIN s.user u WHERE p.date BETWEEN :inicio AND :fim " +
            "GROUP BY u.id, u.nome, u.corAvatar ORDER BY COUNT(p) DESC, u.nome ASC")
    List<UsuarioAtivo> findUsuariosMaisAtivos(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, Limit limite);
}
