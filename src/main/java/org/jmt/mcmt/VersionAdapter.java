package org.jmt.mcmt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import net.minecraft.world.server.ServerWorld;

// allows unification of multiple MC/Forge versions into one jar
// This uses reflections to execute code that may or may not exist at runtime.
// Try not to go too overboard using these, as reflection operations are VERY expensive.
public class VersionAdapter {
	private static MethodTable table = (new VersionAdapter()).new MethodTable();
	
	private static final String SERVER_WORLD = "net.minecraft.world.server.ServerWorld";
	private static final String REGISTRY_KEY = "net.minecraft.util.RegistryKey";
	

	private static Function<ServerWorld, String> dynamicGetDimensionName;

	public class MethodTable {
		private ConcurrentHashMap<String, ConcurrentHashMap<String, Method>> table = new ConcurrentHashMap<>();

		public void add(String className, String methodName, Class<?>... parameterTypes) {
			try {
				Class<?> clazz = this.getClass().getClassLoader().loadClass(className);

				ConcurrentHashMap<String, Method> m = table.computeIfAbsent(className, 
						(c) -> {return new ConcurrentHashMap<String, Method>();});

				Method method = clazz.getMethod(methodName, parameterTypes);
				method.setAccessible(true);
				m.put(methodName, method);
			} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		public Method get(String className, String methodName) {
			return table.get(className).get(methodName);
		}
	}

	private static void adaptGetDimensionName() {
		for (Method i : ServerWorld.class.getMethods()) {
			switch (i.getName()) {
			case "func_234923_W_": // 1.16
				table.add(SERVER_WORLD, "func_234923_W_");
				table.add(REGISTRY_KEY, "func_240901_a_");
				dynamicGetDimensionName = (sw) -> {
					// return sw.func_234923_W_().func_240901_a_().toString();
					try {
						Object f = table.get(SERVER_WORLD, "func_234923_W_").invoke(sw);
						return table.get(REGISTRY_KEY, "func_240901_a_").invoke(f).toString();
					} catch (IllegalAccessException | IllegalArgumentException | 
							InvocationTargetException | SecurityException e) {
						e.printStackTrace();
						return "ReflectionFailure";
					}
				};
				break;

			case "getDimension": // 1.15
				table.add(SERVER_WORLD, "getDimension");
				table.add("net.minecraft.world.dimension.Dimension", "getType");
				table.add("net.minecraft.world.dimension.DimensionType", "getRegistryName");
				
				dynamicGetDimensionName = (sw) -> {
					// return sw.getDimension().getType().getRegistryName().toString();
					try {
						Object dimension = i.invoke(sw);
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
		if (dynamicGetDimensionName == null)
			throw new NoClassDefFoundError("Unknown Minecraft/Forge version.");
	}

	public static String getDimensionName(ServerWorld sw) {
		if (dynamicGetDimensionName == null) adaptGetDimensionName();
		return dynamicGetDimensionName.apply(sw);
	}
}
