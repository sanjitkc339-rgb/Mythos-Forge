package edu.mythosforge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_links")
public class WorldLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long worldId;

    @Column(nullable = false)
    private Long sourceId;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 70)
    private String label;

    protected WorldLink() {}

    public WorldLink(Long worldId, Long sourceId, Long targetId, String label) {
        this.worldId = worldId;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.label = label;
    }

    public Long getId() { return id; }
    public Long getWorldId() { return worldId; }
    public Long getSourceId() { return sourceId; }
    public Long getTargetId() { return targetId; }
    public String getLabel() { return label; }
}
