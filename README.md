[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://www.youtube.com/watch?v=xvFZjo5PgG0)

# Dibs!

![Mod Icon](common/src/main/resources/assets/dibs/icon.png)

## Installation

* [Modrinth](https://modrinth.com/mod/dibs!)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dibs)

## Support
   
If you encounter bugs or wish to contribute:
* [Report any problems you find.](https://github.com/armaninyow/Dibs/discussions/categories/issues)
* [Share your ideas for new features.](https://github.com/armaninyow/Dibs/discussions/categories/suggestions)

## Changelog
<details>
  <summary></summary>
   
### 3.0.0—1.21.x
* Added multi-version support covering Minecraft 1.21 through 1.21.11
### 2.0.0—1.21.11
* Updated to Minecraft 1.21.11
### 1.1.0—1.21.10
* Added villager-to-bed binding system allowing players to reserve a bed for any villager by right-clicking it with a bed item in hand
* Added tagged item lore showing Bound to: <Profession> #<UUID> on bed items after binding
* Added Locate Owner support for beds. Pressing V while looking at either half of a bound bed applies a Glowing effect to the bound villager
* Prevented unbound villagers from walking toward or claiming workstation blocks and beds already reserved for another villager by filtering bound positions out of POI scans before any brain task sees them
* Fixed bindings not persisting across world saves and reloads by replacing the broken Codec.unit stub with a proper RecordCodecBuilder-based codec
* Cleared bed bindings automatically when either half of the bound bed is broken
* Steered bound villagers toward their reserved bed every 20 ticks if they have lost the memory of it
### 1.0.0—1.21.10
* Initial Release
</details>

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://www.youtube.com/watch?v=xvFZjo5PgG0)

