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
				var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
				var FrameNode = Java.type("org.objectweb.asm.tree.FrameNode");

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
					"isOnExecutionThread", "()Z", false
				));
				instructions.add(new VarInsnNode(opcodes.ALOAD, 0));
				instructions.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "serverExecutionThreadPatch",
            				"(Lnet/minecraft/server/MinecraftServer;)Z" ,false))
				instructions.add(new InsnNode(opcodes.IOR));
				instructions.add(new InsnNode(opcodes.IRETURN));
				instructions.add(fn_end);
				methods.add(targetNode);
				            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ServerExecutionThread Transformer Complete");
            	
                return classNode;
			}
    	}
	}
}