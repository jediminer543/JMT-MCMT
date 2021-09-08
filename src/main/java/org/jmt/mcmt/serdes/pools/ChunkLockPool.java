package org.jmt.mcmt.serdes.pools;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jmt.mcmt.paralelised.ChunkLock;
import org.jmt.mcmt.serdes.filter.AutoFilter;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkLockPool implements ISerDesPool {

	public class CLPOptions implements ISerDesOptions {
		int range;

		public int getRange() { return range; };
	}

	ChunkLock cl = new ChunkLock();

	public ChunkLockPool() {

	}

	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, World w, 
			Consumer<Runnable> executeMultithreaded, @Nullable ISerDesOptions options) {
		executeMultithreaded.accept(() -> {
			int range = 1;
			if (options instanceof CLPOptions) {
				range = ((CLPOptions) options).getRange();
			}
			long[] locks = cl.lock(bp, range);
			try {
				task.run();
			} catch (Exception e) {
				if (o instanceof Entity) AutoFilter.singleton().addClassToBlacklist(o.getClass());
			} finally {
				cl.unlock(locks);
			}
		});
	}
}
