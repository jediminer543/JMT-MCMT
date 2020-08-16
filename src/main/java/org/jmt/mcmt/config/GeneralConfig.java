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

	public static boolean disabled;

	public static boolean disableWorld;

	public static boolean disableEntity;

	public static boolean disableTileEntity;
	public static boolean chunkLockModded;
	public static Set<Class<?>> teWhiteList;
	public static Set<Class<?>> teBlackList;
	
	// Any TE class strings that aren't avaliable in the current environment
	// We use classes for the main operation as class-class comparisons are memhash based
	// So (should) be MUCH faster than string-string comparisons
	public static List<String> teUnfoundWhiteList;
	public static List<String> teUnfoundBlackList;

	public static boolean disableEnvironment;

	public static boolean disableChunkProvider;
	public static boolean enableChunkTimeout;
	public static boolean enableTimeoutRegen;
	public static int timeoutCount;

	public static final GeneralConfigTemplate GENERAL;
	public static final ForgeConfigSpec GENERAL_SPEC;

	static {
		final Pair<GeneralConfigTemplate, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(GeneralConfigTemplate::new);
		GENERAL_SPEC = specPair.getRight();
		GENERAL = specPair.getLeft();
	}

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
		disableWorld = GENERAL.disableWorld.get();
		disableEntity = GENERAL.disableEntity.get();
		disableTileEntity = GENERAL.disableTileEntity.get();
		disableEnvironment = GENERAL.disableEnvironment.get();
		disableChunkProvider = GENERAL.disableChunkProvider.get();
		chunkLockModded = GENERAL.chunkLockModded.get();
		
		enableChunkTimeout = GENERAL.enableChunkTimeout.get();
		enableTimeoutRegen = GENERAL.enableTimeoutRegen.get();
		timeoutCount = GENERAL.timeoutCount.get();
		
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
		GENERAL.disableWorld.set(disableWorld);
		GENERAL.disableEntity.set(disableEntity);
		GENERAL.disableTileEntity.set(disableTileEntity);
		GENERAL.disableEnvironment.set(disableEnvironment);
		GENERAL.disableChunkProvider.set(disableChunkProvider);
		GENERAL.chunkLockModded.set(chunkLockModded); 
		
		GENERAL.enableChunkTimeout.set(enableChunkTimeout);
		GENERAL.enableTimeoutRegen.set(enableTimeoutRegen);
		GENERAL.timeoutCount.set(timeoutCount);
		
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
		
		public final BooleanValue disableWorld;
		
		public final BooleanValue disableEntity;
		
		public final BooleanValue disableTileEntity;
		public final BooleanValue chunkLockModded;
		public final ConfigValue<List<String>> teWhiteList;
		public final ConfigValue<List<String>> teBlackList;
		
		public final BooleanValue disableEnvironment;
		
		public final BooleanValue disableChunkProvider;
		public final BooleanValue enableChunkTimeout;
		public final BooleanValue enableTimeoutRegen;
		public final IntValue timeoutCount;
		
		public GeneralConfigTemplate(ForgeConfigSpec.Builder builder) {
			disabled = builder
					.comment("Globally disable all toggleable functionality")
					.define("disabled", false);
			builder.push("world");
			disableWorld = builder
					.comment("Disable world parallelisation")
					.define("disableWorld", false);
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
			builder.push("environment");
			disableEnvironment = builder
					.comment("Disable environment (plant ticks, etc.) parallelisation")
					.define("disableEnvironment", false);
			builder.pop();
			builder.push("misc");
			disableChunkProvider = builder
					.comment("Disable parallelised chunk caching; doing this will result in much lower performance with little to no gain")
					.define("disableChunkProvider", false);
			enableChunkTimeout = builder
					.comment("Enable chunk loading timeouts; this will forcably kill any chunks that fail to load in sufficient time\n"
							+"may allow for loading of damaged/corrupted worlds")
					.define("enableChunkTimeout", false);
			enableTimeoutRegen = builder
					.comment("Attempts to re-load timed out chunks; Seems to work")
					.define("enableTimeoutReload", false);
			timeoutCount = builder
					.comment("Ammount of workless iterations to wait before declaring a chunk load attempt as timed out\n"
							+"This is in ~100us itterations (plus minus yield time) so timeout >= timeoutCount*100us")
					.defineInRange("timeoutCount", 5000, 500, 500000);
		}

	}

}
