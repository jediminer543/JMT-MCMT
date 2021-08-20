# Adding stuff to the filter

The mod should automatically identify troublesome entities and automatically create a new filter config file, but in the event that it doesn't...

Create a new `.toml` file in `config/mcmt-serdes/`. It can be named anything, but keep it simple for sanity's sake.

Paste the contents of this example config:

```toml
[[filters]]
	name = "custom"
	priority = 10

	[filters.pools]

		[filters.pools.primary]
			name = "LEGACY"
			params = {}

	[filters.targets]
		blacklist = ["com.example.Entity"]
		whitelist = []

```

Change/Add the erroring entities in `filters.targets.blacklist`. This uses Java class notation, not Minecraft IDs. You can also change the name of the config to better identify open configs.

*IF AND ONLY IF YOU'RE USING THE NEW SERIALIZATION PATCH:*

You can choose a pool in `filters.pools.primary.name` between `LEGACY` (multithreaded with a chunk look to prevent race conditions nearby) or `SINGLE` (runs single-threaded before all other multithreaded entites)
