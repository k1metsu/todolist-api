package dev.kimetsu.store.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "task_states")
public class TaskState {

    private static final String SEQ = "task_state_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ)
    @SequenceGenerator(name = SEQ, sequenceName = SEQ, allocationSize = 1)
    Long id;

    @Column(unique = true)
    String stateName;

    @OneToOne
    TaskState leftTaskState;

    @OneToOne
    TaskState rightTaskState;

    @Builder.Default
    Instant created = Instant.now();

    @ManyToOne
    Project project;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "task_state_id")
    List<Task> tasks = new ArrayList<>();

    public Optional<TaskState> getLeftTaskState() {
        return Optional.ofNullable(leftTaskState);
    }

    public Optional<TaskState> getRightTaskState() {
        return Optional.ofNullable(rightTaskState);
    }



}
