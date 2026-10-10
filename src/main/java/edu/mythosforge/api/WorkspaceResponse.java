package edu.mythosforge.api;

import edu.mythosforge.model.World;
import edu.mythosforge.model.WorldItem;
import edu.mythosforge.model.WorldLink;
import java.util.List;

public record WorkspaceResponse(World world, List<WorldItem> items, List<WorldLink> links) {}
