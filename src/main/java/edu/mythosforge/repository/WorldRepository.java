package edu.mythosforge.repository;

import edu.mythosforge.model.World;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldRepository extends JpaRepository<World, Long> {
    List<World> findAllByOrderByCreatedAtDesc();
}
