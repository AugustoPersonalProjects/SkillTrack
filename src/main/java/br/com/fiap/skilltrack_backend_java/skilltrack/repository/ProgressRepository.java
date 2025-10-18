package br.com.fiap.skilltrack_backend_java.skilltrack.respository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Page<Progress> findAllByOrderByIdAsc(Pageable pageable);

    List<Progress> findBySkillIdOrderByDateAsc(Long skillId);

    List<Progress> findByUserId(Long userId);
}
