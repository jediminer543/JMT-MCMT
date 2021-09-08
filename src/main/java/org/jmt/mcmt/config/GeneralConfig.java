package org.jmt.mcmt.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jmt.mcmt.Constants;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 
 * We have 2 configs; startup config and running config
 * 
 * GeneralConfigTemplate contains the startup config; GeneralConfig the running
 * 
 * {@link #bakeConfig()} performs start->running and is executed on startup or reset via command
 * 
 * {@link #saveConfig()} performs running->start and is executed by command save
 * 
 * All settings are runtime configurable
 * 
 * @author jediminer543
 *
 */
@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class GeneralConfig {

	// Actual config stuff
	//////////////////////
	
	// General
	public static boolean disabled;
	
	// Parallelism
	public static int paraMax;
	public static ParaMaxMode paraMaxMode;

	// World
	public static boolean disableWorld;
	public static boolean disableWorldPostTick;
	
	//WorldChunk
	public static boolean disableMultiChunk;

	// Entity
	public static boolean disableEntity;

	// TE
	public static boolean disableTileEntity;
	public static boolean chunkLockModded;
	public static Set<Class<?>> teWhiteList;
	public static Set<Class<?>> teBlackList;
	
	// Any TE class strings that aren't avaliable in the current environment
	// We use classes for the main operation as class-class comparisons are memhash based
	// So (should) be MUCH faster than string-string comparisons
	public static List<String> teUnfoundWhiteList;
	public static List<String> teUnfoundBlackList;

	// Misc
	public static boolean disableEnvironment;
	public static boolean disableChunkProvider;
	
	//Debug
	public static boolean enableChunkTimeout;
	public static boolean enableTimeoutRegen;
	public static boolean enableBlankReturn;
	public static int timeoutCount;
	
	// More Debug
	public static boolean opsTracing;
	public static int logcap;
	
	public static boolean continueAfterStuckTick;
	
	//Forge stuff
	public static final GeneralConfigTemplate GENERAL;
	public static final ForgeConfigSpec GENERAL_SPEC;
	
	public static enum ParaMaxMode {
		Standard, 
		Override,
		Reduction
	}

	static {
		final Pair<GeneralConfigTemplate, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(GeneralConfigTemplate::new);
		GENERAL_SPEC = specPair.getRight();
		GENERAL = specPair.getLeft();
	}
	
	// Functions intended for usage
	///////////////////////////////
	
	public static int getParallelism() {
		switch (GeneralConfig.paraMaxMode) {
		case Standard:
			return GeneralConfig.paraMax <= 1 ? 
					Runtime.getRuntime().availableProcessors() : 
					Math.max(2, Math.min(Runtime.getRuntime().availableProcessors(), GeneralConfig.paraMax));
		case Override:
			return GeneralConfig.paraMax <= 1 ? 
					Runtime.getRuntime().availableProcessors() : 
					Math.max(2, GeneralConfig.paraMax);
		case Reduction:
			return Math.max(
					Runtime.getRuntime().availableProcessors() - Math.max(0, GeneralConfig.paraMax),
					2);
		}
		// Unsure quite how this is "Reachable code" but ok I guess
		return Runtime.getRuntime().availableProcessors();
	}
	
	// Config management stuff
	//////////////////////////

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == GeneralConfig.GENERAL_SPEC) {
			bakeConfig();
		}
	}

	/**
	 * 
	 */
	public static void bakeConfig() {
		disabled = GENERAL.disabled.get();
		
		paraMax = GENERAL.paraMax.get();
		paraMaxMode = GENERAL.paraMaxMode.get();
		
		disableWorld = GENERAL.disableWorld.get();
		disableWorldPostTick = GENERAL.disableWorldPostTick.get();
		
		disableMultiChunk = GENERAL.disableMultiChunk.get();
		
		disableEntity = GENERAL.disableEntity.get();
		disableTileEntity = GENERAL.disableTileEntity.get();
		disableEnvironment = GENERAL.disableEnvironment.get();
		disableChunkProvider = GENERAL.disableChunkProvider.get();
		chunkLockModded = GENERAL.chunkLockModded.get();
		
		enableChunkTimeout = GENERAL.enableChunkTimeout.get();
		enableTimeoutRegen = GENERAL.enableTimeoutRegen.get();
		enableBlankReturn = GENERAL.enableBlankReturn.get();
		timeoutCount = GENERAL.timeoutCount.get();
		
		opsTracing = GENERAL.opsTracing.get();
		logcap = GENERAL.logcap.get();
		
		continueAfterStuckTick = GENERAL.continueAfterStuckTick.get();
		
		teWhiteList = ConcurrentHashMap.newKeySet();//new HashSet<Class<?>>();
		teUnfoundWhiteList = new ArrayList<String>();
		GENERAL.teWhiteList.get().forEach(str -> {
			Class<?> c = null;
			try {
				c = Class.forName(str);
				teWhiteList.add(c);
			} catch (ClassNotFoundException cnfe) {
				teUnfoundWhiteList.add(str);
			}
		});
		
		teBlackList = ConcurrentHashMap.newKeySet();//new HashSet<Class<?>>();
		teUnfoundBlackList = new ArrayList<String>();
		GENERAL.teBlackList.get().forEach(str -> {
			Class<?> c = null;
			try {
				c = Class.forName(str);
				teBlackList.add(c);
			} catch (ClassNotFoundException cnfe) {
				teUnfoundBlackList.add(str);
			}
		});
	}
	
	public static void saveConfig() {
		GENERAL.disabled.set(disabled);
		
		GENERAL.paraMax.set(paraMax);
		GENERAL.paraMaxMode.set(paraMaxMode);
		
		GENERAL.disableWorld.set(disableWorld);
		GENERAL.disableWorldPostTick.set(disableWorldPostTick);
		
		GENERAL.disableMultiChunk.set(disableMultiChunk);
		
		GENERAL.disableEntity.set(disableEntity);
		GENERAL.disableTileEntity.set(disableTileEntity);
		GENERAL.disableEnvironment.set(disableEnvironment);
		GENERAL.disableChunkProvider.set(disableChunkProvider);
		GENERAL.chunkLockModded.set(chunkLockModded); 
		
		GENERAL.enableChunkTimeout.set(enableChunkTimeout);
		GENERAL.enableTimeoutRegen.set(enableTimeoutRegen);
		GENERAL.enableBlankReturn.set(enableBlankReturn);
		GENERAL.timeoutCount.set(timeoutCount);
		
		GENERAL.opsTracing.set(opsTracing);
		GENERAL.logcap.set(logcap);
		
		GENERAL.continueAfterStuckTick.set(continueAfterStuckTick);
		
		GENERAL.teWhiteList.get().clear();
		GENERAL.teWhiteList.get().addAll(teUnfoundWhiteList);
		GENERAL.teWhiteList.get().addAll(teWhiteList.stream().map(clz -> clz.getName()).collect(Collectors.toList()));

		GENERAL.teBlackList.get().clear();
		GENERAL.teBlackList.get().addAll(teUnfoundBlackList);
		GENERAL.teBlackList.get().addAll(teBlackList.stream().map(clz -> clz.getName()).collect(Collectors.toList()));
		
		GENERAL_SPEC.save();
	}

	public static class GeneralConfigTemplate {

		public final BooleanValue disabled;
		
		public final IntValue paraMax;
		public final ConfigValue<ParaMaxMode> paraMaxMode;
		
		public final BooleanValue disableWorld;
		public final BooleanValue disableWorldPostTick;
		
		public final BooleanValue disableMultiChunk;
		
		public final BooleanValue disableEntity;
		
		public final BooleanValue disableTileEntity;
		public final BooleanValue chunkLockModded;
		public final ConfigValue<List<String>> teWhiteList;
		public final ConfigValue<List<String>> teBlackList;
		
		public final BooleanValue disableEnvironment;
		
		public final BooleanValue disableChunkProvider;
		public final BooleanValue enableChunkTimeout;
		public final BooleanValue enableTimeoutRegen;
		public final BooleanValue enableBlankReturn;
		public final IntValue timeoutCount;
		
		public final BooleanValue opsTracing;
		public final IntValue logcap;
		
		public final BooleanValue continueAfterStuckTick;
		
		public GeneralConfigTemplate(ForgeConfigSpec.Builder builder) {
			builder.push("general");
			disabled = builder
					.comment("Globally disable all toggleable functionality")
					.define("disabled", false);
			builder.push("parallelism");
			paraMax = builder
					.comment("Thread count config; In standard mode: will never create more threads\n"
							+ "than there are CPU threads (as that causeses Context switch churning)\n"
							+ "Values <=1 are treated as 'all cores'")
					.defineInRange("paraMax", -1, -1, Integer.MAX_VALUE);
			paraMaxMode = builder
					.comment("Other modes for paraMax\n"
							+"Override: Standard but without the CoreCount Ceiling (So you can have 64k threads if you want)\n"
							+"Reduction: Parallelism becomes Math.max(CoreCount-paramax, 2), if paramax is set to be -1, it's treated as 0\n"
							+"Todo: add more"
							)
					.defineEnum("paraMaxMode", ParaMaxMode.Standard);
			builder.pop();
			builder.pop();
			builder.push("world");
			disableWorld = builder
					.comment("Disable world parallelisation")
					.define("disableWorld", false);
			disableWorldPostTick = builder
					.comment("Disable world post tick parallelisation")
					.define("disableWorldPostTick", false);
			builder.push("chunk");
			disableMultiChunk = builder
					.comment("Disable world prallel chunk loading")
					.define("disableMultiChunk", true);
			builder.pop();
			builder.pop();
			builder.push("entity");
			disableEntity = builder
					.comment("Disable entity parallelisation")
					.define("disableEntity", false);
			builder.pop();
			builder.push("te");
			disableTileEntity = builder
					.comment("Disable tile entity parallelisation")
					.define("disableTileEntity", false);
			chunkLockModded = builder
					.comment("Use chunklocks for any unknown (i.e. modded) tile entities\n"
							+ "Chunklocking means we prevent multiple tile entities a 1 chunk radius of eachother being ticked to limit concurrency impacts")
					.define("chunkLockModded", true);
			teWhiteList = builder
					.comment("List of tile entity classes that will always be fully parallelised\n"
							+ "This will occur even when chunkLockModded is set to true\n"
							+ "Adding pistons to this will not parallelise them")
					.define("teWhiteList", (List<String>)new ArrayList<String>());
			teBlackList = builder
					.comment("List of tile entity classes that will always be chunklocked\n"
							+ "This will occur even when chunkLockModded is set to false")
					.define("teBlackList", (List<String>)new ArrayList<String>());
			builder.pop();
			builder.push("misc");
			disableEnvironment = builder
					.comment("Disable environment (plant ticks, etc.) parallelisation")
					.define("disableEnvironment", false);
			disableChunkProvider = builder
					.comment("Disable parallelised chunk caching; doing this will result in much lower performance with little to no gain")
					.define("disableChunkProvider", false);
			builder.pop();
			builder.push("debug");
			builder.comment("Here is where all the wierd diagnostic and hack options go to live");
			builder.push("load-forcing");
			builder.comment("This option allows for overuling chunk loading faliures. \n"
					+ "This was needed due to an early bug in chunk loading parallelism, but remains for posterity\n"
					+ "May break unexpectedly.");
			enableChunkTimeout = builder
					.comment("Enable chunk loading timeouts; this will forcably kill any chunks that fail to load in sufficient time\n"
							+"may allow for loading of damaged/corrupted worlds")
					.define("enableChunkTimeout", false);
			enableTimeoutRegen = builder
					.comment("Attempts to re-load timed out chunks; Seems to work")
					.define("enableTimeoutReload", false);
			enableBlankReturn = builder
					.comment("Simply returns a new empty chunk instead of a re-generating fully")
					.define("enableBlankReturn", false);
			timeoutCount = builder
					.comment("Amount of workless iterations to wait before declaring a chunk load attempt as timed out\n"
							+"This is in ~100us itterations (plus minus yield time) so timeout >= timeoutCount*100us")
					.defineInRange("timeoutCount", 5000, 500, 500000);
			builder.pop();
			builder.push("ops-tracing");
			builder.comment("This allows for tracing the operations invoked, to diagnose lockups/etc.");
			opsTracing = builder
					.comment("Enable ops tracing; this will probably have a performance impact, but allows for better debugging")
					.define("opsTracing", false);
			builder.pop();
			builder.push("logcap");
			builder.comment("This allows for tracing the operations invoked, to diagnose lockups/etc.");
			logcap = builder
					.comment("Maximum time between MCMT presence alerts in 10ms steps")
					.defineInRange("logcap", 720000, 15000, Integer.MAX_VALUE);
			builder.pop();
			builder.push("continueAfterStuckTick");
			builder.comment("Allows continuation after a stuck tick. "
					+ "This is HIGHLY unstable, so don't enable it unless you know what you're doing and have backups.");
			continueAfterStuckTick = builder.define("continueAfterStuckTick", false);
			builder.pop();
		}

	}

}
