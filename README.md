# Fertilia Public Farming Java Plugin

This replaces the prototype Skript approach with a Java Paper/Spigot plugin.

## Architecture

- Public crop types are registered from real world blocks.
- The exact Bukkit `BlockData#getAsString()` value is the crop identity key.
- Runtime harvest lookup is a direct map lookup from clicked block state to crop type.
- WorldGuard regions are not part of the harvest path.
- Item registry entries are saved as Bukkit-serialized `ItemStack`s in `items.yml`.
- Crop types are saved in `crops.yml`.

## Commands

```text
/fitem save <key>
/fitem give <key> [player]
/fitem delete <key>
/fitem menu

/ffarm register <id>
/ffarm replant <id>
/ffarm reward <id> <item-key|held> [min] [max]
/ffarm toggle <id>
/ffarm list
```

## Workflow

### Build With GitHub Actions

1. Upload this source folder to a GitHub repository.
2. Open the repository's **Actions** tab.
3. Run **Build Fertilia Public Farming**, or push to `main`/`master`.
4. Download the `FertiliaPublicFarming` artifact from the completed workflow run.
5. Put `FertiliaPublicFarming.jar` in the server `plugins` folder.

### Configure In Game

1. Save custom reward items with `/fitem save <key>`.
2. Look at a mature crop block and run `/ffarm register <id>`.
3. Look at the desired post-harvest block state and run `/ffarm replant <id>`.
4. Attach a reward with `/ffarm reward <id> <item-key> <min> <max>`.

### Local Build

If Maven is installed locally, you can also build with:

```text
mvn package
```

The jar will be created at `target/FertiliaPublicFarming.jar`.

## Migration Notes

The requested old zip/scripts/SQL export were not present in this workspace, so this is a clean Java implementation of the requested design rather than a line-by-line port. When those files are available, migrate useful old registry keys into `items.yml` through `/fitem save`, or write a small importer that creates Bukkit `ItemStack`s and calls `ItemRegistry#put`.

Useful old concepts retained:

- Stable item keys and an admin item menu.
- Crop registration from in-world examples.
- Fast keyed lookup instead of broad region/search logic.

Intentionally dropped:

- WorldGuard region dependence for public crop identity.
- Escrow-style broad item search logic in the harvest path.
- Skript variable-driven runtime behavior.
