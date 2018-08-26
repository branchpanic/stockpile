# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html). 

## [Unreleased]

### Added
 - Dependency on Riftlin
 - Ability to "lock" barrels
    - By default, barrels are unlocked
    - Shift right-click to toggle lock state
    - Locked barrels continue accepting only one item when empty
    - Unlocked barrels accept any item when empty
    
### Changed
 - Barrel drop behavior in creative mode
    - Barrels no longer drop when broken in creative
    - Copies can be obtained via pick block

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
