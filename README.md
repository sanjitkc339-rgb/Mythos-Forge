# Mythos Forge

**Mythos Forge** is a fantasy worldbuilding studio made as a BCA-level Java web application. Create a world, grow its codex with realms, characters, creatures, quests, and relics, then connect those entries in a visual relationship map.

## Features

- Create and switch between persistent fantasy worlds.
- Generate a deterministic starter collection from each world's seed.
- Create, edit, search, filter, and delete lore entries.
- Add relationships between any two entries and explore them on a map.
- View live counts for realms, characters, creatures, and connections.
- Export a world and its entries as a portable JSON file.
- Use a responsive HTML/CSS/JavaScript interface backed by a Java REST API.
- Store data in a local H2 relational database; no separate database server is needed.

## Run locally

### Requirements

- Java Development Kit 17 or later
- Apache Maven 3.6.3 or later
- A modern web browser

The first Maven run needs internet access to download project dependencies. After those dependencies are cached, the app runs locally.

### Start the application

Open a terminal in the `mythos-forge` project folder and run:

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The app creates a sample world the first time it starts and keeps the database in `data/mythosforge.mv.db`.

On Windows, you can also double-click `run.bat`.

## H2 database console

For database coursework demonstrations, open [http://localhost:8080/h2-console](http://localhost:8080/h2-console) while the app is running.

- JDBC URL: `jdbc:h2:file:./data/mythosforge`
- User name: `sa`
- Password: leave blank

## Project structure

```text
mythos-forge/
├── data/                         # Persistent local H2 database
├── docs/
│   ├── PROJECT_REPORT.md         # Editable college project report
│   └── USER_GUIDE.md             # App walkthrough
├── src/main/java/edu/mythosforge/
│   ├── api/                      # REST endpoints and request records
│   ├── model/                    # JPA entities
│   ├── repository/               # Spring Data database access
│   ├── service/                  # Validation and world generation
│   └── MythosForgeApplication.java
└── src/main/resources/
    ├── schema.sql                # Relational schema
    ├── application.properties
    └── static/                   # HTML, CSS, and JavaScript frontend
```

## How generation works

Every world has a numeric seed. The starter generator initializes Java's `Random` class with that seed and selects from fixed name pools to create a repeatable collection of realms, characters, creatures, quests, and relics. Running it again on a populated world leaves its content unchanged. The same seed therefore creates the same starter names, which makes demonstrations and debugging repeatable.

## Database model

The database has three core tables: `worlds`, `world_items`, and `world_links`. A world has many entries. A connection belongs to a world and links two entries in that same world. The service checks ownership before creating or deleting a connection, and the schema includes foreign keys for referential integrity.

See [the project report](docs/PROJECT_REPORT.md) for the project abstract, objectives, ER diagram, modules, requirements, and report sections to personalize before submission.

## Academic note

This is a local single-user academic prototype. Before submitting, add your name, roll number, college, supervisor, and required screenshots to the report. Follow your college's citation and AI-assistance policy, and make sure you can explain each module during your viva.

## Technology

Java 17, Spring Boot, Spring MVC, Spring Data JPA, H2, HTML, CSS, and vanilla JavaScript.
