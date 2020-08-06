# MCMT - Minecraft Multi-Threading Mod

This is a mod for Minecraft (forge) versions 1.15.2 and 1.16.1 that enables multithreading

Currently it supports multithreading worlds, entities, tile entities, and environment ticks; 
under testing it has appeared to work with mods.

Under single player testing, there is a nominal drop of 20-50% in tps enabled vs disabled and little measurable difference between disabled and not installed. Note: this is a processing side (i.e. either internal or dedicated server) mod and is unlikely cause any increase in framerates. 

Also note that this WILL allow vanilla clients to connect to a server with just this mod and forge installed.

## IMPORTANT NOTICE

This fecks around with a LOT of minecraft's internal processing loops. If you are using this with any other mods; it may break. If you do something in vanilla, it might still break. It might break simply by you looking at it. It is almost impossible to test every possible interaction that CAN occur; all I can say is that it's worked in every case I've tested.

DO NOT, under any circumstances, report errors under this mod back to the mod author unless they have EXPLICITLY stated they are attempting to support it. Should mod devs wish to support this in any way, please yell so I can add registration apis.

TO MODDERS: This mod logs at warn level to the log saying it is installed every ~3 minutes and that the logs are invalid. It should be really obvious.

## Installing

Simply install the jar like you normally would any other mod; it's that simple. 

There are different jars for 1.15.2 and 1.16.1 as while exactly 1 method thing changed to break transferability, there is no way for me to deal with it in a single jar. So make sure you get the right one.

## How it works

The normal minecraft server loop is:

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

To make this parallelisation work, there are a lot of patches to the minecraft core code to avoid concurrent access to non-concurrent objects (curse ye fastutil) or to replace them with a working concurrent alternative.

Further, this mod adds it's own chunk caching into the mix, in order to provide paralellised access to chunks; this does mean it may consume some more memory though.

I'll attempt to add more documentation as time goes by on how this works on the gritty internal level.

## Compiling

TODO: write properly

In summary:

Get a download of the repo:

`git clone [the url of the page you are on now]`

(Or download a zip and unzip it; it's up to you)

Open the directory containing it in a command line and run:

`./gradlew build`

The resulting jar will be present in `./build/libs/`

## TODO LIST

- Add config for specifying classes/packages that can/cant multithread
- Parallelise more stuff
- Hack the planet
- Document everything so it's reproducable
