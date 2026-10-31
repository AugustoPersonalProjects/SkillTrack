package br.com.fiap.skilltrack_backend_java.skilltrack.model;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.CorAvatar;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Perfil;
import br.com.fiap.skilltrack_backend_java.skilltrack.model.enums.Tema;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "st_usuarios", uniqueConstraints = {@UniqueConstraint(name = "st_usuarios_email_uq", columnNames = "email")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @JsonProperty("id_usuario")
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(name = "data_criacao", updatable = false)
    private Instant dataCriacao;

    // vai dentro do token: ao mudar, os tokens emitidos antes deixam de valer
    @Column(name = "versao_token", nullable = false)
    private int versaoToken;

    @Column(length = 280)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "cor_avatar", nullable = false, length = 20)
    private CorAvatar corAvatar = CorAvatar.ESMERALDA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tema tema = Tema.SISTEMA;

    @PrePersist
    protected void onCreate() {
        this.dataCriacao = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
