package org.jmt.mcmt.modlauncher;

import java.lang.module.Configuration;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import cpw.mods.cl.ModuleClassLoader;

public class CheatyModuleClassLoader extends ModuleClassLoader {

	FastUtilTransformerService ts = new FastUtilTransformerService();
	
	public CheatyModuleClassLoader(String name, Configuration configuration, List<ModuleLayer> parentLayers) {
		super(name, configuration, parentLayers);
	}
	
	@Override
	protected byte[] maybeTransformClassBytes(byte[] bytes, String name, String context) {
		//if (bypass != null) {
		//	return bypass.
		//}
		if (!ts.targets().stream().anyMatch(t->t.getClassName().equals(name))) {
			return bytes;
		}
		ClassReader cr = new ClassReader(bytes);
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		ts.transform(cn, null);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

}
