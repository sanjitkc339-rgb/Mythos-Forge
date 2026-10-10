package edu.mythosforge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "world_items")
public class WorldItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long worldId;

    @Column(nullable = false, length = 20)
    private String kind;

    @Column(nullable = false, length = 90)
    private String name;

    @Column(nullable = false, length = 100)
    private String subtitle;

    @Column(nullable = false, length = 800)
    private String description;

    @Column(nullable = false, length = 240)
    private String traits;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected WorldItem() {}

    public WorldItem(Long worldId, String kind, String name, String subtitle, String description, String traits) {
        this.worldId = worldId;
        this.kind = kind;
        this.name = name;
        this.subtitle = subtitle;
        this.description = description;
        this.traits = traits;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public void update(String kind, String name, String subtitle, String description, String traits) {
        this.kind = kind;
        this.name = name;
        this.subtitle = subtitle;
        this.description = description;
        this.traits = traits;
    }

    public Long getId() { return id; }
    public Long getWorldId() { return worldId; }
    public String getKind() { return kind; }
    public String getName() { return name; }
    public String getSubtitle() { return subtitle; }
    public String getDescription() { return description; }
    public String getTraits() { return traits; }
    public Instant getCreatedAt() { return createdAt; }
}
