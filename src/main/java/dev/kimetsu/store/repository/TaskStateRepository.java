package dev.kimetsu.store.repository;

import dev.kimetsu.store.entity.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStateRepository extends JpaRepository<TaskState, Long> {
    Optional<TaskState> findTaskStateByProjectIdAndStateNameContainsIgnoreCase(Long projectId, String stateName);
}
