package br.com.fiap.skilltrack_backend_java.skilltrack.repository;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Usuario;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findAllByOrderByIdAsc(Pageable pageable);

    // ✅ Use Optional<Usuario> aqui
    Optional<Usuario> findByEmail(String email);

    long countByPerfil(Perfil perfil);

    long countByDataCriacaoGreaterThanEqual(Instant inicio);

    @Query("SELECT u.dataCriacao FROM Usuario u WHERE u.dataCriacao >= :inicio")
    List<Instant> findDatasDeCriacaoDesde(@Param("inicio") Instant inicio);
}
