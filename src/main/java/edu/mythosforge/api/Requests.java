package edu.mythosforge.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class Requests {
    private Requests() {}

    public record CreateWorld(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Size(max = 70) String genre,
            @NotBlank @Size(max = 320) String premise,
            Long seed) {}

    public record WorldItemInput(
            @NotBlank @Size(max = 20) String kind,
            @NotBlank @Size(max = 90) String name,
            @NotBlank @Size(max = 100) String subtitle,
            @NotBlank @Size(max = 800) String description,
            @NotBlank @Size(max = 240) String traits) {}

    public record CreateLink(@NotNull @Positive Long sourceId, @NotNull @Positive Long targetId, @NotBlank @Size(max = 70) String label) {}
}
