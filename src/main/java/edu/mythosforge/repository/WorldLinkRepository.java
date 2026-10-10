package edu.mythosforge.repository;

import edu.mythosforge.model.WorldLink;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldLinkRepository extends JpaRepository<WorldLink, Long> {
    List<WorldLink> findAllByWorldId(Long worldId);
    void deleteAllByWorldId(Long worldId);
    void deleteAllByWorldIdAndSourceIdOrWorldIdAndTargetId(Long firstWorldId, Long sourceId, Long secondWorldId, Long targetId);
}
