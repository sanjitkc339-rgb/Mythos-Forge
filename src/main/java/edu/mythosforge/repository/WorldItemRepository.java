package edu.mythosforge.repository;

import edu.mythosforge.model.WorldItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldItemRepository extends JpaRepository<WorldItem, Long> {
    List<WorldItem> findAllByWorldIdOrderByIdDesc(Long worldId);
    long countByWorldId(Long worldId);
    void deleteAllByWorldId(Long worldId);
    void deleteByWorldIdAndId(Long worldId, Long id);
}
