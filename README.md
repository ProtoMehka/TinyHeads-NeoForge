# TinyHeads (NeoForge)

A standalone mod for NeoForge 1.21.1 adding collectible figurine blocks inspired by various franchises, with a display shelf that grants area buffs once a full boxed collection is assembled.

This is a port of TinyHeads (Forge 1.20.1) to NeoForge 1.21.1 — same mod ID, same content and version, parallel release on a different platform rather than a continuation of the Forge version's own release history.

## What it does

- 30 figurine types across several franchise "collections" (League of Legends, Marvel, Rick and Morty, Minecraft, South Park, DC Comics, Star Wars, Horror, Touhou Project, Date a Live), each with a boxed and an unboxed block variant.
- A display shelf block (multi-part, like a vanilla bed) that renders placed figurines and grants a configurable area buff once an entire collection is assembled on shelves nearby.
- Figurines can turn up in vanilla loot tables via a Global Loot Modifier, with per-rarity weights adjustable live through the mod's config — no rebuild needed to retune drop rates.

## Dependencies

None beyond NeoForge 1.21.1-21.1.259+. No other mod required.

## Building

```
./gradlew build
```

The output jar is in `build/libs/`.

## License

All Rights Reserved — free to use in any modpack, fork/modify freely, but no reupload/redistribution elsewhere without explicit permission. See [LICENSE.txt](LICENSE.txt).
