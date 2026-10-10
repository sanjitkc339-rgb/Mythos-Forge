package edu.mythosforge.api;

import edu.mythosforge.model.World;
import edu.mythosforge.model.WorldItem;
import edu.mythosforge.model.WorldLink;
import edu.mythosforge.repository.WorldItemRepository;
import edu.mythosforge.repository.WorldLinkRepository;
import edu.mythosforge.repository.WorldRepository;
import edu.mythosforge.service.WorldService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class WorldApiController {
    private final WorldRepository worlds;
    private final WorldItemRepository items;
    private final WorldLinkRepository links;
    private final WorldService service;

    public WorldApiController(WorldRepository worlds, WorldItemRepository items, WorldLinkRepository links, WorldService service) {
        this.worlds = worlds;
        this.items = items;
        this.links = links;
        this.service = service;
    }

    @GetMapping("/worlds")
    public List<World> allWorlds() { return worlds.findAllByOrderByCreatedAtDesc(); }

    @PostMapping("/worlds")
    @ResponseStatus(HttpStatus.CREATED)
    public World createWorld(@Valid @RequestBody Requests.CreateWorld request) { return service.createWorld(request); }

    @GetMapping("/worlds/{worldId}/workspace")
    public WorkspaceResponse workspace(@PathVariable long worldId) { return service.workspace(worldId); }

    @DeleteMapping("/worlds/{worldId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorld(@PathVariable long worldId) { service.deleteWorld(worldId); }

    @PostMapping("/worlds/{worldId}/generate")
    public WorkspaceResponse generate(@PathVariable long worldId) { return service.generateStarterKit(worldId); }

    @PostMapping("/worlds/{worldId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WorldItem createItem(@PathVariable long worldId, @Valid @RequestBody Requests.WorldItemInput request) {
        return service.addItem(worldId, request);
    }

    @PutMapping("/worlds/{worldId}/items/{itemId}")
    public WorldItem updateItem(@PathVariable long worldId, @PathVariable long itemId, @Valid @RequestBody Requests.WorldItemInput request) {
        return service.updateItem(worldId, itemId, request);
    }

    @DeleteMapping("/worlds/{worldId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable long worldId, @PathVariable long itemId) { service.deleteItem(worldId, itemId); }

    @PostMapping("/worlds/{worldId}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public WorldLink createLink(@PathVariable long worldId, @Valid @RequestBody Requests.CreateLink request) {
        return service.addLink(worldId, request);
    }

    @DeleteMapping("/worlds/{worldId}/links/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLink(@PathVariable long worldId, @PathVariable long linkId) { service.deleteLink(worldId, linkId); }
}
