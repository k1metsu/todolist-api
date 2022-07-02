package dev.kimetsu.store.repository;

import dev.kimetsu.store.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByTitle(String title);
    Stream<Project> streamAllBy();
    Stream<Project> streamAllByTitleStartingWithIgnoreCase(String prefix_name);

}
