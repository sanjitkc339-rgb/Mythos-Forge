# Mythos Forge — User Guide

## 1. Open the studio

Start the Java application, then visit `http://localhost:8080`. A sample fantasy world is ready on first launch. The left sidebar lists saved worlds.

## 2. Create a world

Choose **Forge a new world**. Enter a name, genre, and premise. A seed is optional: entering one makes generated starter names repeatable. Leave it empty to receive a randomly selected seed.

## 3. Generate starter lore

Select **Generate starter lore** to add sample realms, characters, creatures, quests, relics, and relationships. Generation only fills an empty world; it does not overwrite existing entries.

## 4. Add and edit entries

Choose **Add to world** and select one of five types. Add a name, subtitle, description, and traits. Select an entry card to read its full description. Use the edit control on a card or in its detail view to update it. Deleting an entry also removes its connections.

## 5. Connect entries

Choose **Connect entries**, select two different entries, and describe their relationship. Switch from the grid to the map view to see the connections. Select a node to open its entry.

## 6. Find and export

Use the type tabs to filter the codex and the search field to find matching names, descriptions, or traits. Choose **Export world** to download a JSON snapshot containing the world, entries, and connections.

## 7. Database view

While the app is running, open `http://localhost:8080/h2-console`. Use the JDBC URL `jdbc:h2:file:./data/mythosforge`, user `sa`, and an empty password. The console is enabled for local academic demonstration; do not expose it on a public server.
