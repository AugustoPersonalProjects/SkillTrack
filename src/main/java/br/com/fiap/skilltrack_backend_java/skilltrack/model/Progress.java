package br.com.fiap.skilltrack_backend_java.skilltrack.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "st_progress")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progress")
    @JsonProperty("id_progress")
    private Long id;

    @Column(name = "date_progress")
    private LocalDate date;

    @Column(nullable = false)
    private Integer percentage;

    @Column(length = 255)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private Goal goal;

    @PrePersist
    protected void onCreate() {
        if (this.date == null) this.date = LocalDate.now();
    }
}
