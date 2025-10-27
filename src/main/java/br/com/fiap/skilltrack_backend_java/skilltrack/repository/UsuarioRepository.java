package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findAllByOrderByIdAsc(Pageable pageable);

    // ✅ Use Optional<Usuario> aqui
    Optional<Usuario> findByEmail(String email);
}
