# Version history

Mostly here for my own sanity

## 0.11.19

- Fixes the postTick event from forge; This was causing issues with SereneSeasons

- A bug has been discovered that apparently breaks some world generation (confirmed against LycanitesMobs)
	- Causes hang on world creation/ingame chunk gen as SCP locks up waiting for completed task to complete (See: SCP.getChunk keyword:isdone)

## 0.11.17

Probably not released

- Changed the entitiesByID retype into a wrapped concurrenthashset
	- This improves mod compatibility, with probably negligable performance impact
	- Lesson to future me: There is no need to be overly aggressive with patching
	- Also foundation of a FastUtil replacement project
- Also changed stats a bit

## 0.10.12

- Fixed bug in entitiesByID patch

## 0.10.11

- Retyped entitiesByID
- Added stats comand

## 0.9.8

First public release