# Material Chain & Mod Integration

> Last checked: 2026-11-06 · Applies to: 1.21.1 / 1.20.1

This page explains why a modded ore can (or cannot) enter this mod's ore chain, and where the three
customization paths end. Field-level details live in the [datapack page](datapack.md); this page
answers the questions pack authors ask most often.

## 1. What the chain looks like

Every material owns a six-piece set:

| Stage | Block | Piece item |
|---|---|---|
| Gravel | `<id>_ore_gravel` | `<id>_broken` |
| Sand | `<id>_ore_sand` | `<id>_crushed` |
| Dust | `<id>_ore_dust` | `<id>_powdered` |

- Sift gravel → broken; sift sand → crushed; sift dust (`exnihilosatellitium:dust`) → powdered;
- 4 pieces → 1 matching ore block (2×2);
- Hammer ore gravel → crushed (4–6), hammer ore sand → powdered (4–6); the dust block is the end of the hammer chain;
- Smelt any of the three ore blocks → the material's product (ingot, gem, or crystal, decided by the material).

The resource identity belongs to this chain: we register our own gravel/pieces/powder instead of
pasting another mod's item onto the sieve.

## 2. Which materials exist

There are currently 26: 14 using `ingot`/`ingots`, 12 using `gems`. The authoritative list is
[`materials.json`](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/src/main/resources/exnihilosatellitium/materials.json);
the table below only covers products supplied by other mods.

| Source mod | Candidate products | Notes |
|---|---|---|
| Vanilla | iron, gold, copper | reuses vanilla ingots |
| Mekanism | tin, lead, osmium, uranium; fluorite | both versions |
| Thermal series | tin, lead, silver, nickel; apatite, cinnabar, niter, sulfur, ruby, sapphire | currently 1.20.1 only |
| Create | zinc | only when loaded |
| Immersive Engineering | lead, silver, nickel, aluminum, uranium | only when loaded |
| Tinkers' Construct | cobalt | only when loaded |
| Tinkers' Thinking | ardite, chlorophyll | only when loaded |
| Extreme Reactors | `yellorium`, `anglesite`, `benitoite` | `gems` gate |
| Draconic Evolution | `draconium` | single source: 1/8 crushed end stone |

- Candidates are an **ordered list**: when several candidate mods are present, the first one in
  `materials.json` wins;
- **`gems` materials** (fluorite, apatite, `yellorium`, `draconium`, etc.; the schema field is called
  `gems`, the product is not necessarily a gem) are registered **as a whole only when a candidate mod is present**:
  without it, even the gravel/pieces/powder do not exist;
- Materials using `ingot`/`ingots` fall back to our own ingot (`<id>_ingot`) when no candidate is loaded,
  so an unmodded instance still closes the loop; `gems` have no fallback;
- Changing the mod list changes which ingots exist: own ingots already in a world can turn into
  unknown items. Decide the mod list before creating the world.

## 3. Why the three paths cannot add materials

Items and blocks are registered once during the **registration phase** and then frozen;
`materials.json` is read at construction time precisely because that phase decides which items exist.
Datapacks / KubeJS / CraftTweaker can change the seven tables, recipes, and tags, but they **cannot
create an item id for this mod out of nothing**.

Adding a material means: edit `materials.json` inside the jar → run the texture/hammer/lang generator
and `runData` → rebuild and restart.

There is currently **no auto-discovery or config extension layer**: a material outside the table does
not appear just because a mod is installed.

## 4. What pack authors can do

### A. Use existing materials (recommended)

If you only need a flow to use items this mod already has, no material table change is needed:

- add `exnihilosatellitium:<id>_broken` / `_crushed` / `_powdered` to your own sieve input;
- override a smelting recipe with a datapack to change a material's product (a metal may have several
  variants with `mod_loaded` conditions; handle them together);
- add tags to this mod's blocks/items;
- use the KubeJS / CT seven-table API for the same add / remove / replace.

Ready-to-copy example pack:
[examples/datapack](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/tree/main/examples/datapack).

### B. A new mod has the same product

If the new mod only provides another tin ingot, the cheapest route is to **change the smelting result
in the pack**, not to add a material. Item ids, sieve tables, and JEI stay unchanged.

Note: this mod's chain **does not consume other mods' ore blocks**; it consumes its own
gravel/sand/dust and pieces. Mapping another mod's ore block onto our material is not a supported entry.

### C. A genuinely new material

Open an issue / PR with:

- mod id, ore block id, and the `c:ores/<id>` tag (1.20.1 uses `forge:ores/<id>`);
- product id and its `c:ingots/<id>` / `c:gems/<id>` tag;
- color `RRGGBB`, tier, rarity, English and Chinese names;
- whether it is single-source (only from one sieve table or one block).

We add a `materials.json` row + textures/generators + datagen; pack authors only restart.

### D. KubeJS parallel chain

KubeJS can register its own items as a "parallel ore chain", but it is not part of this mod's material
chain: no six-piece set, no `c:ores/<id>` tags, and it does not appear in the sieve table
automatically. You can add it to a sieve table by hand, but that is just a normal drop.

## 5. Common misconceptions

| Misconception | Reality |
|---|---|
| Installing Mekanism puts every Mekanism ore in the sieve | Only materials listed in `materials.json` do |
| A datapack can add an off-table material | No; registration is frozen |
| `gems` materials can be sifted without the mod | No; the whole material is not registered |
| Old ingots turning into unknown items after changing mods is a bug | Classic behavior; decide the mod list before creating the world |
| Missing from JEI means it is not working | On a dedicated server the client cannot read server datapacks, so JEI only shows client-local data; trust `/exnihilo dump` on the server |

## 6. Related pages

- [Datapack: seven table fields](datapack.md)
- [KubeJS: script API](kubejs.md)
- [CraftTweaker: script API](crafttweaker.md)
- [Cookbook](cookbook.md)
- Developer schema: [docs/data-format.md §8](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/data-format.md)
- Design background: [docs/DESIGN.md §6.4](https://github.com/WanConcubine/Ex-Nihilo-Satellitium/blob/main/docs/DESIGN.md)
