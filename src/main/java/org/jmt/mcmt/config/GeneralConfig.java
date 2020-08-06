package org.jmt.mcmt.config;

import org.apache.commons.lang3.tuple.Pair;
import org.jmt.mcmt.Constants;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class GeneralConfig {

	public static boolean disabled;

	public static boolean disableWorld;

	public static boolean disableEntity;

	public static boolean disableTileEntity;
	public static boolean chunkLockModded;

	public static boolean disableEnvironment;

	public static boolean disableChunkProvider;

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

	public static void bakeConfig() {
		disabled = GENERAL.disabled.get();
		disableWorld = GENERAL.disableWorld.get();
		disableEntity = GENERAL.disableEntity.get();
		disableTileEntity = GENERAL.disableTileEntity.get();
		disableEnvironment = GENERAL.disableEnvironment.get();
		disableChunkProvider = GENERAL.disableChunkProvider.get();
		chunkLockModded = GENERAL.chunkLockModded.get(); 
	}
	
	public static void saveConfig() {
		GENERAL.disabled.set(disabled);
		GENERAL.disableWorld.set(disableWorld);
		GENERAL.disableEntity.set(disableEntity);
		GENERAL.disableTileEntity.set(disableTileEntity);
		GENERAL.disableEnvironment.set(disableEnvironment);
		GENERAL.disableChunkProvider.set(disableChunkProvider);
		GENERAL.chunkLockModded.set(chunkLockModded); 
		GENERAL_SPEC.save();
	}

	public static class GeneralConfigTemplate {

		public final BooleanValue disabled;
		
		public final BooleanValue disableWorld;
		
		public final BooleanValue disableEntity;
		
		public final BooleanValue disableTileEntity;
		public final BooleanValue chunkLockModded;
		
		public final BooleanValue disableEnvironment;
		
		public final BooleanValue disableChunkProvider;
		
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
		}

	}

}
