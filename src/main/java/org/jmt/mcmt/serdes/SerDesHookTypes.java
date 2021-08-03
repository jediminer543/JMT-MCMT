package org.jmt.mcmt.serdes;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.entity.Entity;

public enum SerDesHookTypes implements ISerDesHookType {
	EntityTick(Entity.class),
	TETick(ITickableTileEntity.class);
	
	Class<?> clazz;
	
	SerDesHookTypes(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public Class<?> getSuperclass() {
		return clazz;
	}

	
}
