package dev.kimetsu.store.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tasks")
public class Task {

    private static final String SEQ = "task_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ)
    @SequenceGenerator(name = SEQ, sequenceName = SEQ, allocationSize = 1)
    Long id;

    String taskName;

    String description;

    @OneToOne
    Task upTask;

    @OneToOne
    Task downTask;

    @Builder.Default
    Instant created = Instant.now();

    @ManyToOne
    TaskState taskState;

    public Optional<Task> getUpTask() {
        return Optional.ofNullable(upTask);
    }

    public Optional<Task> getDownTask() {
        return Optional.ofNullable(downTask);
    }

}
