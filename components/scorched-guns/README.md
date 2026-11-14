# Scorched Guns 2 - NeoForge 26.2 Unofficial Port

This repository contains an **unofficial NeoForge port of Scorched Guns 2 for Minecraft 26.2**.

Scorched Guns 2 was originally created by **ribs498 / ribs** for Minecraft 1.20.1 Forge. This project is not a rewrite or a reimagining of the mod. The goal of the port is to keep the original gameplay, content, data formats and general behavior as close to **Scorched Guns 2 0.5.5** as reasonably possible while adapting the code to the modern Minecraft and NeoForge APIs.

This port is not affiliated with or maintained by the original author.

## Original project

- **Original mod:** https://modrinth.com/mod/scorched-guns-2 or https://www.curseforge.com/minecraft/mc-mods/scorched-guns
- **Original source:** https://github.com/ribs498/Scorched-Guns-1.20.1


## About the port

Scorched Guns 2 0.5.5 was written for Minecraft 1.20.1 Forge. Porting it to Minecraft 26.2 required changes in several systems that no longer work the same way in modern Minecraft.

The work was done with a parity-first approach: when a system had to be replaced, the new implementation was built around the behavior of the original rather than changing how the mod plays.

Some of the larger changes made during the port include:

- Migrating the project from **Forge to NeoForge**.
- Updating the codebase from **Minecraft 1.20.1 to Minecraft 26.2**.
- Updating the development environment to **Java 25**.
- Porting animated weapons from **GeckoLib 4 to GeckoLib 5**.
- Rebuilding first-person weapon rendering around the modern rendering pipeline.
- Restoring animated first-person player arms and their original weapon poses.
- Adapting third-person weapon poses to Minecraft's modern **render-state** system.
- Updating held-item rendering to work with the current `ItemStackRenderState` / submission pipeline.
- Restoring physical attachment rendering in first and third person.
- Restoring the original ADS behavior and the different magnification levels of scopes.
- Migrating weapon data from the old mutable ItemStack NBT model to modern **Data Components / CustomData**.
- Adapting item and energy capability code to the current NeoForge transfer APIs.
- Updating resource reload listeners while keeping the original gun JSON system usable.
- Updating JEI integration for the current Minecraft version.
- Migrating enchantment support to the modern data-driven enchantment and tag systems.
- Fixing performance regressions that were introduced by the port itself, while intentionally leaving original gameplay algorithms unchanged.

A large part of the porting process involved comparing the 26.2 implementation against the original 0.5.5 behavior so that compatibility fixes did not accidentally become balance changes or redesigns.

## Addon support

Scorched Guns has always been useful as a base for addon development, so keeping that flexibility was an important part of this port.

The main extension points are still present, including support for:

- Custom `GunItem` and `AnimatedGunItem` implementations.
- Gun data under addon namespaces.
- Custom weapon models, textures and GeckoLib animations.
- Attachments and attachment modifiers.
- Custom `IGunModifier` implementations.
- Gun-compatible enchantments and enchantment tags.
- Datapack-driven gun data and related resources.

Addons made for older Scorched Guns versions will still need to be ported to the Minecraft 26.2, NeoForge and GeckoLib 5 APIs. The intention here is to preserve the original mod's addon-friendly structure, not to guarantee source-level compatibility with 1.20.1 addons without changes.


## Dependencies

### Required dependencies

Scorched Guns 2 requires the following for Minecraft 26.2:

- **NeoForge**
- **Framework** `0.13.26` or a compatible Minecraft 26.2 release
- **Curios API** `16.0.0+26.2` or a compatible Minecraft 26.2 release
- **GeckoLib 5** `5.5.4` or newer compatible Minecraft 26.2 release

Refer to `build.gradle` and `gradle.properties` for the exact versions used by the repository.

### Optional integrations

None of the following are required to run Scorched Guns. When installed, the port keeps or adds compatibility with them where applicable:

- **Just Enough Items (JEI)** — recipe and workstation integration.
- **Create** — preserves the original conditional Create integration and recipes (for 26.2 u can try with Create Adapted).
- **Guard Villagers** — guards can equip and use Scorched Guns weapons, and newly generated guards can receive thematic gunner loadouts. No Guard Villagers resources are bundled with this port.
- **Punchy!** — when detected, Scorched Guns automatically merges the exact IDs of its registered gun items into Punchy's `itemBlacklist`. This compatibility does not replace or mix into Punchy's renderer. On the first launch after installing both mods, one restart may be required if Punchy loaded its config before the blacklist was updated.
- **Fresh Animations: Player Extensions + Entity Model Features (EMF 3.3+)** — Fresh Animations keeps control of player locomotion while Scorched Guns preserves its weapon-specific pose. Held guns and Scorched Guns GeckoLib armor, including the ExoSuit, follow the final EMF player pose so weapons and armor stay attached to the animated body. EMF/ETF and the resource pack are optional and are not bundled with this mod.

## Reporting issues

Please report problems that are specific to this **Minecraft 26.2 unofficial port** in this repository.

Do not report port-specific crashes, rendering problems or compatibility regressions to the original Scorched Guns 2 author unless the same issue can also be reproduced in the original mod.

## Credits

**Scorched Guns 2**
- Original creator: **ribs498 / ribs**
- Original design, gameplay systems, codebase, models, textures, sounds and other assets belong to their respective original authors and contributors.

**Minecraft 26.2 NeoForge port**
- Port maintained by **Tepox00**

Additional credit remains with the contributors acknowledged by the original Scorched Guns 2 project.

## License

The original Scorched Guns 2 project is currently distributed under the **GNU General Public License v3.0 or later**.

This port is therefore distributed under the same terms:

**SPDX-License-Identifier: GPL-3.0-or-later**

See the [`LICENSE`](LICENSE) file for the complete GNU GPL v3 license text.

This repository contains a modified version of Scorched Guns 2. The Minecraft 26.2 porting changes were made in 2026 and are clearly presented as an unofficial port so that issues introduced by this version are not attributed to the original author.

## Disclaimer

This is an unofficial community port of Scorched Guns 2.

Minecraft is a trademark of Microsoft / Mojang. This project is not affiliated with, endorsed by, or associated with Mojang, Microsoft, NeoForge, or the original Scorched Guns 2 author.
