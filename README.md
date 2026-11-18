# Pharao Client

Fabric client mod project for Minecraft Java 1.21.11.

## Build environment
- Java 21
- Minecraft 1.21.11
- Fabric Loader 0.18.4
- Fabric API 0.141.1+1.21.11
- Yarn mappings 1.21.11+build.5
- Fabric Loom 1.14

## Build
Run `gradle build` from this directory or import the folder as a Gradle project in IntelliJ IDEA. The remapped jar should be produced in `build/libs/`.

## Functional in this milestone
- Right Shift opens the Pharao menu.
- Tools, Appearance, Keybinds, and Settings screens.
- Persistent client configuration.
- Toggleable chest, ender chest, barrel, amethyst growth-stage, underground light-source, entity and ore indicators.
- Nearby findings are shown in a compact HUD list with relative direction arrows and distance.
- Entity labels include players, item frames, glow item frames, armor stands, paintings, dropped items and arrows.
- Ore inspection only reads block states already available in loaded client chunks.
- Scanning is throttled to once per second and limited to a 12-block horizontal and vertical radius.

## Limitations
- This is not a compiled or runtime-tested jar. Build and test it in a clean Fabric 1.21.11 instance.
- The current freecam entry is a placeholder; safe camera detachment is not yet implemented.
- "Excavation" analysis is not yet implemented. Natural caves and player-dug spaces cannot be reliably distinguished from a single client snapshot.
- Amethyst growth state shows the current bud/cluster stage; it does not reveal historical chunk-load counts.
- Lighting checks identify light-emitting blocks underground, but cannot prove whether a source was naturally generated or player-placed.
- The client cannot inspect unloaded chunks or recover block data hidden by server anti-xray. No hidden data is requested or exposed.
- Client-only scanning cannot prove a rule violation. Findings are clues for a human moderator to review.

## Carrot Client
This targets standard Fabric 1.21.11. Compatibility with Carrot Client has not been tested; Carrot must support ordinary Fabric mods.
