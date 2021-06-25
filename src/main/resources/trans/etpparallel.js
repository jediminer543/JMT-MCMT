function initializeCoreMod() {
    return {
    	'ServerExecutionThread': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.MinecraftServer'
            },
            "transformer": function(classNode) {
				var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				var List = Java.type("java.util.ArrayList");
				var LocalVariableNode  = Java.type("org.objectweb.asm.tree.LocalVariableNode");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
				var InsnList = Java.type("org.objectweb.asm.tree.InsnList");

            	asmapi.log("INFO", "[JMTSUPERTRANS] ServerExecutionThread Transformer Called");
            	
            	var methods = classNode.methods;

				var targetNode = asmapi.getMethodNode();
				targetNode.name = asmapi.mapMethod("func_213162_bc");
				targetNode.desc = "()Z";
				targetNode.access = 0x1;
				
				var fn_start = new LabelNode();
				var fn_end = new LabelNode();
				
				targetNode.localVariables = new List();
				targetNode.localVariables.add(new LocalVariableNode(
					"this", "Lnet/minecraft/server/MinecraftServer;", null,
					fn_start, fn_end, 0
				));
				
				instructions = targetNode.instructions;
				instructions.add(fn_start)
				instructions.add(new VarInsnNode(opcodes.ALOAD, 0));
				instructions.add(new MethodInsnNode(opcodes.INVOKESPECIAL, 
					"net/minecraft/util/concurrent/RecursiveEventLoop", 
					targetNode.name, "()Z", false
				));
				instructions.add(new InsnNode(opcodes.IRETURN));
				instructions.add(fn_end);

				var hit = false;
				for (var mn in methods) {
					if (mn.name == targetNode.name && mn.desc == targetNode.desc) {
						hit = true;
						targetNode = mn;
						break;
					}
				}
				
				if (!hit) {
					methods.add(targetNode);
				}
				
				instructions = targetNode.instructions;
				
				var il = new InsnList();
				il.add(new VarInsnNode(opcodes.ALOAD, 0));
				il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "serverExecutionThreadPatch",
            				"(Lnet/minecraft/server/MinecraftServer;)Z" ,false))
				il.add(new InsnNode(opcodes.IOR));
				
				insn = targetNode.instructions.getFirst();
				
				while (insn != null) {
					if (insn.opcode == opcodes.IRETURN) {
						targetNode.instructions.insertBefore(insn, il);
						il = new InsnList();
						il.add(new VarInsnNode(opcodes.ALOAD, 0));
						il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
		            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "serverExecutionThreadPatch",
		            				"(Lnet/minecraft/server/MinecraftServer;)Z" ,false))
						il.add(new InsnNode(opcodes.IOR));
					}
					insn = insn.getNext();
				}
				            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ServerExecutionThread Transformer Complete");
            	
                return classNode;
			}
    	}
	}
}