# Troubleshooting and Logs

> Last checked: 2026-11-03 · Applies to: 1.21.1 / 1.20.1

## 1. Do these 4 steps first

1. `/reload`
2. `/datapack list` → your pack is in the list and enabled
3. `/exnihilo tables` → did the entry count change; `/exnihilo dump <table>` → the full table contents
4. Check the matching `[sieve]` / `[hammer]` / `[barrel]` / `[compost]` / `[crucible]` lines in the server log

If the count in step 3 didn't change, it's almost always **the file wasn't loaded**; if the count changed but the behavior is wrong, it's almost always **a wrong field/chance**.

## 2. Common cases of "not taking effect"

| Symptom | Common cause |
|---|---|
| `/datapack list` doesn't list your pack | Wrong directory level, or no `pack.mcmeta` |
| Pack shows disabled | `/datapack enable "file/my_pack"` |
| Log says `empty or unknown tag` | Tag misspelled; 1.20.1 uses `c:` (should be `forge:`), or the other way around |
| Log says `unknown input/result` | Item id misspelled, or a required mod isn't installed (you can add a `"mod"` gate) |
| Log says `duplicate input` | Two files write the same input; for the sieve the later one overrides, for the hammer/crook/melting point the later one is skipped |
| You only changed `outputs` and all the original drops are gone | Missing `"patch": true`, so it's treated as an override |
| `remove` was written but nothing happens | No `patch: true`, or `remove` isn't a patch field; or the target id is wrong |
| A 1.21.1 pack placed in 1.20.1 | `pack_format` should be `15`; paths differ, e.g. `recipe/` vs `recipes/` |
| Fewer entries the first time after `/reload` | Normal: `waiting for tags` comes first, the full count only appears after `TagsUpdatedEvent` |
| You changed something but don't know what the whole table looks like | `/exnihilo dump <table>`; sorted by line, so you can diff it directly against another table |

## 3. What is `waiting for tags`

Tags are bound after all reload listeners, so every reload prints twice:

```
[sieve] loaded 6 inputs, 90 drops (0 tag files, 2 patches, waiting for tags)
[sieve] loaded 45 inputs, 115 drops (1 tag files, 2 patches)
```

**The second line is authoritative.** The first only covers concrete entries; `#tag` and patches haven't been expanded yet.

## 4. Compost "has a value but can't be composted"

Compost needs both places to be present:

1. `values` in `data/<ns>/compost/*.json`
2. The item tag `#exnihilosatellitium:compostable`

Check the current value with `/exnihilo compost <item>`:

```mcfunction
/exnihilo compost minecraft:bread
```

- Value 0: not written in the JSON, or not loaded
- Value > 0 but can't compost in game: the item isn't in the tag
- Can compost but only gives 1: present in the tag, absent from the JSON (defaults to 1)

## 5. JEI doesn't match the actual behavior

- Singleplayer / LAN: JEI reads the server tables, so they should match.
- **Dedicated server**: the client can't read the server datapacks, so JEI shows the jar's built-in values;
  the server is authoritative. This doesn't mean the datapack isn't working.
- JEI doesn't refresh after `/reload` and changes: reopen the JEI screen or re-enter the world.

## 6. KubeJS-specific

| Symptom | Check |
|---|---|
| JSON in `kubejs/data` doesn't take effect | The path must be `kubejs/data/<ns>/sieve/...`, not `kubejs/sieve/...` |
| A script changed a recipe but the table didn't change | The seven tables have no KJS API; the tables only read JSON |
| Startup script errors | Check `logs/kubejs/startup.log` |
| Server script errors | Check `logs/kubejs/server.log` |
| `kubejs/data` isn't re-read after `/reload` | Quit and re-enter the world / restart the server (depending on the KJS version) |
| Items registered by KJS aren't in the material chain | Normal; it's just KJS content, not this mod's material |

## 7. CraftTweaker-specific

| Symptom | Check |
|---|---|
| You want to change the sieve/barrel/compost | **Not possible**; use a datapack or KJS |
| `<recipetype:exnihilosatellitium:sieve>` errors | This mod registers no CT recipe type |
| Script syntax errors | Check `logs/crafttweaker.log`; ZenScript includes line numbers |
| Can't remove a recipe | Remove by output with `craftingTable.remove(<item:...>)`; make sure your CT version uses `craftingTable`, not the 1.12 `recipes` |
| Conflicts with KJS | Don't change the same recipe on both sides; disable one side before testing |

## 8. Version differences at a glance

| | 1.21.1 | 1.20.1 |
|---|---|---|
| `pack_format` | 48 | 15 |
| Vanilla recipe | `data/<ns>/recipe/` | `data/<ns>/recipes/` |
| Vanilla tag | `tags/item`, `tags/block` | `tags/items`, `tags/blocks` |
| Common tag | `c:` | `forge:` |
| Vanilla `result` | `{"id": ...}` | `{"item": ...}` |
| The seven tables | Same as left | Same as left |

## 9. If it still doesn't work, include this information

- Game version + mod version (1.21.1 / 1.20.1)
- Datapack directory tree (a screenshot or text of `/datapack list`)
- The output of `/exnihilo tables`
- All `[sieve]` / `[hammer]` / `[barrel]` / `[compost]` / `[crucible]` lines in the server log
- The full text of the JSON file that's causing the problem

That makes it obvious at a glance whether it's "not loaded", "wrong field", or "wrong path for the version".
