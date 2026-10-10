package edu.mythosforge.service;

import edu.mythosforge.api.Requests;
import edu.mythosforge.api.WorkspaceResponse;
import edu.mythosforge.model.World;
import edu.mythosforge.model.WorldItem;
import edu.mythosforge.model.WorldLink;
import edu.mythosforge.repository.WorldItemRepository;
import edu.mythosforge.repository.WorldLinkRepository;
import edu.mythosforge.repository.WorldRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorldService {
    private static final List<String> KINDS = List.of("realm", "character", "creature", "quest", "relic");
    private final WorldRepository worlds;
    private final WorldItemRepository items;
    private final WorldLinkRepository links;

    public WorldService(WorldRepository worlds, WorldItemRepository items, WorldLinkRepository links) {
        this.worlds = worlds;
        this.items = items;
        this.links = links;
    }

    public World requireWorld(long id) {
        return worlds.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "World not found."));
    }

    public WorldItem requireItem(long id) {
        return items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "World entry not found."));
    }

    public WorkspaceResponse workspace(long worldId) {
        World world = requireWorld(worldId);
        return new WorkspaceResponse(world, items.findAllByWorldIdOrderByIdDesc(worldId), links.findAllByWorldId(worldId));
    }

    public World createWorld(Requests.CreateWorld request) {
        long seed = request.seed() == null ? new Random().nextLong() : request.seed();
        return worlds.save(new World(request.name().trim(), request.genre().trim(), request.premise().trim(), seed));
    }

    @Transactional
    public void deleteWorld(long worldId) {
        requireWorld(worldId);
        links.deleteAllByWorldId(worldId);
        items.deleteAllByWorldId(worldId);
        worlds.deleteById(worldId);
    }

    public WorldItem addItem(long worldId, Requests.WorldItemInput input) {
        requireWorld(worldId);
        validateKind(input.kind());
        return items.save(new WorldItem(worldId, input.kind().toLowerCase(), input.name().trim(), input.subtitle().trim(), input.description().trim(), input.traits().trim()));
    }

    public WorldItem updateItem(long worldId, long itemId, Requests.WorldItemInput input) {
        validateKind(input.kind());
        WorldItem item = requireOwnedItem(worldId, itemId);
        item.update(input.kind().toLowerCase(), input.name().trim(), input.subtitle().trim(), input.description().trim(), input.traits().trim());
        return items.save(item);
    }

    @Transactional
    public void deleteItem(long worldId, long itemId) {
        requireOwnedItem(worldId, itemId);
        links.deleteAllByWorldIdAndSourceIdOrWorldIdAndTargetId(worldId, itemId, worldId, itemId);
        items.deleteByWorldIdAndId(worldId, itemId);
    }

    public WorldLink addLink(long worldId, Requests.CreateLink request) {
        requireWorld(worldId);
        WorldItem source = requireOwnedItem(worldId, request.sourceId());
        WorldItem target = requireOwnedItem(worldId, request.targetId());
        if (source.getId().equals(target.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose two different entries for a connection.");
        return links.save(new WorldLink(worldId, source.getId(), target.getId(), request.label().trim()));
    }

    public void deleteLink(long worldId, long linkId) {
        requireWorld(worldId);
        WorldLink link = links.findById(linkId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found."));
        if (!link.getWorldId().equals(worldId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found in this world.");
        links.delete(link);
    }

    private WorldItem requireOwnedItem(long worldId, long itemId) {
        WorldItem item = requireItem(itemId);
        if (!item.getWorldId().equals(worldId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found in this world.");
        return item;
    }

    private void validateKind(String kind) {
        if (kind == null || !KINDS.contains(kind.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid entry type: realm, character, creature, quest, or relic.");
        }
    }

    @Transactional
    public WorkspaceResponse generateStarterKit(long worldId) {
        World world = requireWorld(worldId);
        if (items.countByWorldId(worldId) > 0) return workspace(worldId);

        Random random = new Random(world.getSeed());
        String[] realms = {"The Glass Marches", "Vesperwild", "The Sunken Choir", "Ashen Reach", "Moonwell"};
        String[] names = {"Ilyra Venn", "Kael Orison", "Mira Thorne", "Soren Vale", "Nyx Calder", "Aven Sol", "Talia Wren", "Eris Fen"};
        String[] creatures = {"Lantern Stag", "Cloud Serpent", "Mossback Giant", "Inkwing Raven", "Star-Eater Moth"};
        String[] objects = {"The Unwritten Compass", "Bell of First Rain", "A Map That Hums", "The Orchard Key"};

        List<WorldItem> created = new ArrayList<>();
        created.add(create(worldId, "realm", realms[random.nextInt(realms.length)], "A place between maps", "A borderland where old roads bend toward impossible horizons.", "ancient · mistbound · disputed"));
        created.add(create(worldId, "realm", realms[random.nextInt(realms.length)], "A country of unfinished songs", "Its towns are built around bells that ring only when someone forgets a name.", "musical · coastal · secretive"));
        created.add(create(worldId, "realm", realms[random.nextInt(realms.length)], "The last bright valley", "A small refuge lit by a sun that appears only at midnight.", "warm · hidden · fragile"));
        created.add(create(worldId, "character", names[random.nextInt(names.length)], "Cartographer of lost roads", "Keeps a pocket atlas of places that have not been discovered yet.", "curious · stubborn · mapmaker"));
        created.add(create(worldId, "character", names[random.nextInt(names.length)], "Keeper of the quiet bell", "Can hear a promise echo long after the person who made it has gone.", "patient · watchful · bellkeeper"));
        created.add(create(worldId, "character", names[random.nextInt(names.length)], "A runaway royal astronomer", "Claims the constellations are rearranging themselves to spell a warning.", "bold · sleepless · stargazer"));
        created.add(create(worldId, "character", names[random.nextInt(names.length)], "The stranger at the ferry", "Arrives every seventh dawn with a different shadow.", "enigmatic · courteous · traveler"));
        created.add(create(worldId, "creature", creatures[random.nextInt(creatures.length)], "A guide with borrowed antlers", "Appears at crossroads when the traveler has already chosen the wrong path.", "gentle · luminous · nocturnal"));
        created.add(create(worldId, "creature", creatures[random.nextInt(creatures.length)], "A keeper of forgotten names", "Collects names in a nest of silver thread and returns them as lullabies.", "ancient · shy · uncanny"));
        created.add(create(worldId, "relic", objects[random.nextInt(objects.length)], "Points toward what you meant to find", "Its needle turns when its holder tells the truth about why they are traveling.", "brass · temperamental · compass"));
        created.add(create(worldId, "relic", objects[random.nextInt(objects.length)], "Rings once for every hidden door", "The bell is silent in rooms where everyone is welcome.", "silver · resonant · heirloom"));
        created.add(create(worldId, "quest", "The Road That Remembers", "Find the road erased from every map", "Three witnesses remember a different path. Their stories overlap only at moonrise.", "exploration · mystery · 3 clues"));
        created.add(create(worldId, "quest", "A Name for the Night", "Return the missing name before dawn", "The keeper will trade a name for a memory that the seeker is willing to lose.", "choice · folklore · 2 endings"));

        links.save(new WorldLink(worldId, created.get(3).getId(), created.get(0).getId(), "charts the roads of"));
        links.save(new WorldLink(worldId, created.get(4).getId(), created.get(10).getId(), "guards the secret of"));
        links.save(new WorldLink(worldId, created.get(5).getId(), created.get(11).getId(), "is drawn into"));
        links.save(new WorldLink(worldId, created.get(7).getId(), created.get(3).getId(), "leads home"));
        links.save(new WorldLink(worldId, created.get(12).getId(), created.get(8).getId(), "requires a meeting with"));
        links.save(new WorldLink(worldId, created.get(6).getId(), created.get(1).getId(), "arrives from"));
        return workspace(worldId);
    }

    private WorldItem create(Long worldId, String kind, String name, String subtitle, String description, String traits) {
        return items.save(new WorldItem(worldId, kind, name, subtitle, description, traits));
    }
}
