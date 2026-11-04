# AreaBreaker 1.2.0

A Fabric client mod for Minecraft Java 1.21.11 (Java 21) focused on configurable area mining and miner progression.

## Mining
- Area mining with configurable square sizes: 1, 2, 3, 4, 5 and 10.
- Vein mining with a level- and upgrade-scaled cap, up to 128 connected blocks.
- Shapes: square, wall, strip and tunnel.
- Tunnel depth grows with miner level and the Tunneler upgrade.
- Reach, vein cap, magnet radius and durability reserve can be improved through the upgrade tree.
- Safe mode avoids protected blocks (bedrock, barriers, command blocks, portals, structure blocks) and block entities during area/vein mining.
- Durability protection stops mass mining before the tool reaches its configured reserve.

## Progression and statistics
- +1 XP for ordinary blocks, +3 XP for ores.
- Miner level increases every 250 XP.
- Earn one upgrade point per five miner levels.
- Six upgrades: Vein Capacity, Magnet Range, Reinforced Tool, Geologist, Tunneler and Mining Reach.
- Lifetime stats: blocks, ores, collected items and largest vein.
- Session stats are shown in the HUD.
- Settings, upgrades and lifetime stats persist in `config/areabreaker.properties`.

## Convenience
- Magnetic pickup can collect nearby drops into the inventory.
- Optional auto-smelting converts raw iron, gold and copper into ingots.
- Block filters: OFF, BLACKLIST and WHITELIST. Aim at a block and press K to add/remove its ID.
- HUD shows mode, shape, target block, miner level and XP progress.
- Main menu includes settings, safety toggle and a shortcut to the upgrade tree.

## Controls
- V: open settings.
- U: open upgrade tree.
- B: enable/disable AreaBreaker.
- G: switch area/vein mode.
- N: cycle mining shape.
- H: toggle auto-smelting.
- K: add/remove the targeted block in the filter list.
- M: toggle protected-block safety.
- J: toggle HUD.

Controls can be remapped in Minecraft's Controls menu.

## Miner books
Three shaped crafting recipes are included: Miner I, Miner II and Miner III. Their recipe JSON uses the Minecraft 1.21.11 string-ID ingredient format. CI validates resource JSON and the recipe schema before building.

## Important limitations
- The current mining implementation is designed for integrated single-player. A multiplayer server must run compatible server-side logic; a client mod cannot force a remote server to break blocks.
- The magnet picks up item entities within its radius, not only drops created by the current mining action.
- Auto-smelting currently handles raw iron, raw gold and raw copper only.
- Safe mode is a conservative block-ID and block-entity guard, not a complete claim/protection-mod integration.
- The build pipeline verifies compilation and resource formats. Always test gameplay in a disposable world before using it in a main save.
