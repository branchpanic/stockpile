# stockpile

[![Build Status](https://travis-ci.org/branchpanic/stockpile.svg?branch=master)](https://travis-ci.org/branchpanic/stockpile)
[![CurseForge](https://img.shields.io/badge/dynamic/json.svg?color=orange&label=curseforge&query=%24%5B-1%3A%5D.fileName&url=https%3A%2F%2Fstaging_cursemeta.dries007.net%2Fapi%2Fv3%2Fdirect%2Faddon%2F299913%2Ffiles)](https://minecraft.curseforge.com/projects/stockpile)

> A Minecraft mod all about storage.

Stockpile is powered by the [Fabric](https://fabricmc.net/) modloader and supports Minecraft 1.14. Versions below 0.4.0
ran on the Rift modloader for 1.13.

It depends on the Fabric API and Fabric's [Scala language module](https://github.com/FabricMC/fabric-language-scala).

## Features

See the [wiki](https://github.com/notjoe7F/stockpile/wiki) for a list of features. Feature requests are welcome through
GitHub issues!

## Configuration & Integrations

### Barrel Upgrades

To add or overwrite the available Barrel upgrades, use the tag `stockpile:barrel_storage_upgrade`. This tag is intended
for upgrades equal to 1 chest in capacity (27 stacks).
