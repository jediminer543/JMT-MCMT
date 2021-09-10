[![Join our discord](https://img.shields.io/discord/749993549004800081?style=for-the-badge)](https://discord.gg/ZuYgPFURkf)
[![Issues](https://img.shields.io/github/issues/jediminer543/JMT-MCMT?style=for-the-badge)](https://github.com/jediminer543/JMT-MCMT/issues)
[![Build Statis](https://img.shields.io/github/workflow/status/jediminer543/JMT-MCMT/Java%20CI%20with%20Gradle?style=for-the-badge)](https://github.com/jediminer543/JMT-MCMT/actions/workflows/build-dev.yml)
[![Curseforge Downloads](https://img.shields.io/badge/dynamic/json?label=Curse%20Downloads&query=downloads.total&url=https%3A%2F%2Fapi.cfwidget.com%2Fminecraft%2Fmc-mods%2Fmcmt-multithreading&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/mcmt-multithreading)
[![Support Me](https://forthebadge.com/images/badges/built-with-love.svg)](https://ko-fi.com/jediminer543)
[![Shenanagins](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://www.youtube.com/watch?v=djV11Xbc914)

# MCMT - Minecraft Multi-Threading Mod

This is a mod for Minecraft (Forge) versions 1.15.2 and 1.16.x that enables multithreading

Currently it supports multithreading worlds, entities, tile entities, and environment ticks; 
under testing it has appeared to work with mods.

Under single player testing, there is a nominal drop of 20-50% in tps enabled vs disabled and little measurable difference between disabled and not installed. Note: this is a processing side (i.e. either internal or dedicated server) mod and is unlikely cause any increase in framerates. 

Also note that this WILL allow vanilla clients to connect to a server with just this mod and Forge installed.

## IMPORTANT NOTICE

This fecks around with a LOT of Minecraft's internal processing loops. If you are using this with any other mods; it may break. If you do something in vanilla, it might still break. It might break simply by you looking at it. It is almost impossible to test every possible interaction that CAN occur; all I can say is that it's worked in every case I've tested.

DO NOT, under any circumstances, report errors under this mod back to the mod author unless they have EXPLICITLY stated they are attempting to support it. Should mod devs wish to support this in any way, please yell so I can add registration APIs.

TO MODDERS: This mod logs at warn level to the log saying it is installed every ~3 minutes and that the logs are invalid. It should be really obvious.

## Installing

Simply install the JAR like you normally would any other mod; it's that simple. 

There are different JARs for 1.15.2 and 1.16.1 as while exactly 1 method thing changed to break transferability, there is no way for me to deal with it in a single JAR. So make sure you get the right one.

## Getting involved

If you want to get involved with the project there are several things you can do:

- There is a currently seperate version of this mod that is [working on Fabric support](https://github.com/himekifee/MCMTFabric)
- We have a Discord (link at top of the README) where I'm going to try and organize stuff
- Create an issue with as much detail as possible (or comment on ones that are already there with more info)
- Contribute code

## How it works

The normal Minecraft server loop is:

```py
def tick() :
	for world in worlds:
		# random stuff
		for chunk in world.loadedchunks:
			chunk.tickEnvironment()
		# random stuff
		for entity in world.entities:
			entity.tick()
		for tileEntity in world.tileEntities:
			tileEntity.tick()
```

All this mod does is parallelise each of these loops these loops. The vast majority of the performance (at least in my testing) was gained from parallelising entites.

Each one of the above loops is paralelised, and each one is toggleable.

To make this parallelisation work, there are a lot of patches to the Mnecraft core code to avoid concurrent access to non-concurrent objects (curse ye fastutil) or to replace them with a working concurrent alternative.

Further, this mod adds it's own chunk caching into the mix, in order to provide paralellised access to chunks; this does mean it may consume some more memory though.

I'll attempt to add more documentation as time goes by on how this works on the gritty internal level.

## Compiling

TODO: write properly

In summary:

Get a download of the repo:

`git clone [the URL of the page you are on now]`

(Or download a ZIP and unzip it, it's up to you)

Open the directory containing it in a command line and run:

`./gradlew build`

The resulting JAR will be present in `./build/libs/`

## TODO LIST

- Parallelise more stuff
- Hack the planet
- Document everything so it's reproducable
