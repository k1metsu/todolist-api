package dev.kimetsu.store.repository;

import dev.kimetsu.store.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findTaskByTaskStateIdAndTaskNameContainingIgnoreCase(Long taskId, String taskName);
}
