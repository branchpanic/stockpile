# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).  

## [0.4.4] - 2019-01-26

### Fixed
 - Bug where an entire stack of barrels could be upgraded for the cost of one  

## [0.4.3] - 2019-01-25

### Changed
 - Cereal version to 3.0.0-beta
 - Dependencies to Minecraft 19w04a versions

## [0.4.2] - 2019-01-18

### Changed
 - Dependencies to Minecraft 19w03a/b/c versions

## [0.4.1] - 2019-01-12

### Added
 - Comparator output for barrels, since it wasn't implemented in the previous version
 
### Fixed
 - Version being reported as 1.0.0 

## [0.4.0] - 2019-01-12

### Added
 - Dependency on Cereal
 - Dependency on Fabric's Scala language adapter
 - Tag support to Barrel upgrade system
    - Instead of only chests, any item tagged `stockpile:barrel_storage_upgrade` will upgrade 

### Changed
 - Modding platform from Rift to Fabric
 - Barrel recipe
    - Rotated to make more sense with the texture
    - Uses tag `stockpile:barrel_storage_upgrade` to allow any chest-like block to be used in the recipe
    - Now yields 2 barrels to offset iron requirement
 - Barrel upgrades to take a little bit less experience
 - Barrel texture to fit in a bit more new textures
 
### Removed
 - Dependency on Riftlin

## [0.3.0] - 2018-08-30

### Added
 - Dependency on Riftlin
 - Ability to "lock" barrels
    - By default, barrels are unlocked
    - Shift right-click to toggle lock state
    - Locked barrels continue accepting only one item when empty
    - Unlocked barrels accept any item when empty
 - French translation (thanks to Yanis48)
    
### Changed
 - Rift version to 1.0.4-66
 - Barrel drop behavior in creative mode
    - Barrels no longer drop when broken in creative
    - Copies can be obtained via pick block
    
### Fixed
 - Indicator bar not properly filling up properly

## [0.2.0] - 2018-08-16

### Added
 - Indicator bar showing how full a barrel is

### Fixed
 - Issue where tile entities weren't removed after blocks were
 - Empty barrels showing their item as Air when right-clicked
 - Date on version 0.1.1 in the changelog (oops)

## [0.1.1] - 2018-08-14

### Changed
 - Rift version from 1.0.2-33 to 1.0.3-45

## [0.1.0] - 2018-08-08

### Added
 - Barrel: holds many stacks of a single item
    - Right-click to insert the held stack (if possible)
    - Double-right-click to insert as many stacks as possible from inventory
    - Left-click to extract a single item
    - Crouch-left-click to extract an entire stack
  - Trash Can: deletes items
    - Right-clicking opens/closes the lid
    - When open, items thrown on top are deleted
    - Items hoppered in are always deleted
