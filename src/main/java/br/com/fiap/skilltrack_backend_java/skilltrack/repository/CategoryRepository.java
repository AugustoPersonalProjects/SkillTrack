package br.com.fiap.skilltrack_backend_java.skilltrack.respository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByOrderByIdAsc(Pageable pageable);
}
