function initializeCoreMod() {
    return {
    	'ServerWorldTickChunk': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.level.ServerChunkCache'
            },
            "transformer": function(classNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldTickChunk Transformer Called");
            	
            	var methods = classNode.methods;
            	
            	var tgtdesc = "(JZLnet/minecraft/world/level/NaturalSpawner$SpawnState;ZILnet/minecraft/server/level/ChunkHolder;)V"
            	
        		for (var i in methods) {
            		var method = methods[i];
            		
            		if (!method.desc.equals(tgtdesc)) {
            			continue;
            		}
            		
            		asmapi.log("DEBUG", "[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var instructions = method.instructions;
            		
            		var callMethod = asmapi.mapMethod("m_8714_");
            		var callClass = "net/minecraft/server/level/ServerLevel";
            		var callDesc = "(Lnet/minecraft/world/level/chunk/LevelChunk;I)V";
            	
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
            				callClass, callMethod, callDesc, 0);
            		
            		if (callTarget == null) {
            			asmapi.log("ERROR", "[JMTSUPERTRANS] MISSING TARGET INSN");
            			return;
            		}
            		
            		// Jump instruction
            		var skipTarget = new LabelNode();
            		            		
            		var il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0)); // Is lambda so `this` isn't local var 0
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "callTickEnvironment",
            				"(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;ILnet/minecraft/server/level/ServerChunkCache;)V" ,false));
            		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
            		
            		instructions.insertBefore(callTarget, il);
            		instructions.insert(callTarget, skipTarget);
            		
            		break;
            	
        		}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldTickChunk Transformer Complete");
            	
            	return classNode;
            }
    	}
	}
    
    
}