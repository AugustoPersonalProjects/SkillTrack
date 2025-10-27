package br.com.fiap.skilltrack_backend_java.skilltrack.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "ST_GOALS")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "goal_seq")
    @SequenceGenerator(name = "goal_seq", sequenceName = "SEQ_ST_GOALS", allocationSize = 1)
    @Column(name = "id_goal")
    @JsonProperty("id_goal")
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "completed")
    private boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Usuario user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @PrePersist
    protected void onCreate() {
        if (this.title == null && this.description != null) {
            this.title = this.description.length() > 150 ?
                    this.description.substring(0, 147) + "..." : this.description;
        }
    }
}