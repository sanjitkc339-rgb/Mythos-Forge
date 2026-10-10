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
@Table(name = "worlds")
public class World {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 70)
    private String genre;

    @Column(nullable = false, length = 320)
    private String premise;

    @Column(nullable = false)
    private long seed;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected World() {}

    public World(String name, String genre, String premise, long seed) {
        this.name = name;
        this.genre = genre;
        this.premise = premise;
        this.seed = seed;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getGenre() { return genre; }
    public String getPremise() { return premise; }
    public long getSeed() { return seed; }
    public Instant getCreatedAt() { return createdAt; }
}
