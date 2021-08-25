package org.jmt.mcmt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.server.ServerWorld;

// allows unification of multiple MC/Forge versions into one jar
// This uses reflections to execute code that may or may not exist at runtime.
// Try not to go too overboard using these, as reflection operations are VERY expensive.
public class VersionAdapter {
	private static MethodTable table = (new VersionAdapter()).new MethodTable();

	private static Function<ServerWorld, String> dynamicGetDimensionName;

	public class MethodTable {
		private ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>> table = new ConcurrentHashMap<>();

		public void add(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
			ConcurrentHashMap<String, Method> m = table.computeIfAbsent(clazz, 
					(c) -> {return new ConcurrentHashMap<String, Method>();});
			try {
				Method method = clazz.getMethod(methodName, parameterTypes);
				method.setAccessible(true);
				m.put(methodName, method);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}

		public Method get(Class<?> clazz, String methodName) {
			return table.get(clazz).get(methodName);
		}
	}

	private static void adaptGetDimensionName() {
		for (Method i : ServerWorld.class.getMethods()) {
			switch (i.getName()) {
			case "func_234923_W_": // 1.16
				table.add(ServerWorld.class, "func_234923_W_");
				table.add(RegistryKey.class, "func_240901_a_");
				dynamicGetDimensionName = (sw) -> {
					// return sw.func_234923_W_().func_240901_a_().toString();
					Object f;
					try {
						f = i.invoke(sw);
						return f.getClass().getMethod("func_240901_a_").invoke(f).toString();
					} catch (IllegalAccessException | IllegalArgumentException | 
							InvocationTargetException | NoSuchMethodException | 
							SecurityException e) {
						e.printStackTrace();
						return "ReflectionFailure";
					}

				};
				break;

			case "getDimension": // 1.15
				dynamicGetDimensionName = (sw) -> {
					// return sw.getDimension().getType().getRegistryName().toString();
					Object dimension;
					try {
						dimension = i.invoke(sw);
						Object dimensionType = dimension.getClass().getMethod("getType").invoke(dimension);
						return dimensionType.getClass().getMethod("getRegistryName").invoke(dimensionType).toString();
					} catch (IllegalAccessException | IllegalArgumentException | 
							InvocationTargetException | NoSuchMethodException | 
							SecurityException e) {
						e.printStackTrace();
						return "ReflectionFailure";
					}
				};
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
