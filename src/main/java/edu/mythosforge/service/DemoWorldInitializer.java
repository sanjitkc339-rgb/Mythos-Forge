package edu.mythosforge.service;

import edu.mythosforge.api.Requests;
import edu.mythosforge.repository.WorldRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoWorldInitializer implements CommandLineRunner {
    private final WorldRepository worlds;
    private final WorldService service;

    public DemoWorldInitializer(WorldRepository worlds, WorldService service) {
        this.worlds = worlds;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (worlds.count() == 0) {
            var world = service.createWorld(new Requests.CreateWorld(
                    "The Shattered Meridian",
                    "Mythic fantasy",
                    "A moon has fallen into the sea, and its fragments are changing the names of places.",
                    482917L));
            service.generateStarterKit(world.getId());
        }
    }
}
