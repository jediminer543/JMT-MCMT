package org.jmt.mcmt.serdes;

public interface ISerDesHookType {

	public String getName();
	
	public Class<?> getSuperclass();
	
	public default boolean isTargetable(Class<?> clazz) {
		return getSuperclass().isAssignableFrom(clazz);
	}
	
	public default boolean isTargetable(Object obj) {
		return isTargetable(obj.getClass());
	}
	
}
