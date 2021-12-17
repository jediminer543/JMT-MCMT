package org.jmt.mcmt.modlauncher;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class DevModeEnabler implements ITransformer<ClassNode> {

	
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker M_LOCATOR = MarkerManager.getMarker("LOCATE");
	private boolean isActive = true;
	
	
	@Override
	public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
		for (MethodNode mn : input.methods) {
			if (mn.name.equals("<clinit>")) {
				LOGGER.warn("[JMTSUPERTRANS] Now entering DEBUG MODE!!!");
				InsnList il = new InsnList();
				il.add(new InsnNode(Opcodes.ICONST_1));
				il.add(new FieldInsnNode(Opcodes.PUTSTATIC, "net/minecraft/util/SharedConstants", "developmentMode", "Z"));
				il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
				il.add(new LdcInsnNode("WHO SUMMONED ME!!!!!!!!"));
				il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
				mn.instructions.insertBefore(mn.instructions.getLast(), il);
			}
		}
		//System.out.println("TEST");
		return input;
	}
	@Override
	public TransformerVoteResult castVote(ITransformerVotingContext context) {
		return TransformerVoteResult.YES;
	}
	@Override
	public Set<Target> targets() {
		Set<Target> out = new HashSet<ITransformer.Target>();
		if (!isActive) {
			// Is Dead
			return out;
		}
		out.add(Target.targetClass("net.minecraft.util.SharedConstants"));
		return out;
	}
	
}
