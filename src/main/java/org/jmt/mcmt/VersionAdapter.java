package org.jmt.mcmt;

import java.lang.reflect.Method;
import java.util.function.Function;

import net.minecraft.world.server.ServerWorld;

// allows unification of multiple MC/Forge versions into one jar
public class VersionAdapter {
	
	private static Function<ServerWorld, String> dynamicGetDimensionName;
	
	private static void adaptGetDimensionName() {
		for (Method i : ServerWorld.class.getMethods()) {
			switch (i.getName()) {
			case "func_234923_W_": // 1.16
				dynamicGetDimensionName = (sw) -> { return sw.func_234923_W_().func_240901_a_().toString(); };
				break;

			default:
				break;
			}
		}
		if (dynamicGetDimensionName == null) {
			throw new NoClassDefFoundError("Unknown Minecraft/Forge version.");
		}
	}
	
	public static String getDimensionName(ServerWorld sw) {
		if (dynamicGetDimensionName == null) adaptGetDimensionName();
		return dynamicGetDimensionName.apply(sw);
	}
}
