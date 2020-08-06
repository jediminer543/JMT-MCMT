function initializeCoreMod() {
    return {
    	'ServerWorldTickChunk': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerChunkProvider'
            },
            "transformer": function(classNode) {
            	print("[JMTSUPERTRANS] ServerWorldTickChunk Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var methods = classNode.methods;
            	
            	var tgtdesc = "(JZ[Lnet/minecraft/entity/EntityClassification;ZILit/unimi/dsi/fastutil/objects/Object2IntMap;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/world/server/ChunkHolder;)V"
            	
        		for (var i in methods) {
            		var method = methods[i];
            		
            		if (!method.desc.equals(tgtdesc)) {
            			continue;
            		}
            		
            		print("[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var instructions = method.instructions;
            		
            		var callMethod = asmapi.mapMethod("func_217441_a");
            		var callClass = "net/minecraft/world/server/ServerWorld";
            		var callDesc = "(Lnet/minecraft/world/chunk/Chunk;I)V";
            	
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
            				callClass, callMethod, callDesc, 0);
            		
            		if (callTarget == null) {
            			print("[JMTSUPERTRANS] MISSING TARGET INSN");
            			return;
            		}
            		
            		// Jump instruction
            		var skipTarget = new LabelNode();
            		            		
            		var il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0)); // Is lambda so `this` isn't local var 0
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "callTickEnvironment",
            				"(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/chunk/Chunk;ILnet/minecraft/world/server/ServerChunkProvider;)V" ,false));
            		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
            		
            		instructions.insertBefore(callTarget, il);
            		instructions.insert(callTarget, skipTarget);
            		
            		break;
            	
        		}
            	
            	print("[JMTSUPERTRANS] ServerWorldTickChunk Transformer Complete");
            	
            	return classNode;
            }
    	},
		'ServerWorldTickChunk116': {
	        'target': {
	            'type': 'CLASS',
	            'name': 'net.minecraft.world.server.ServerChunkProvider'
	        },
	        "transformer": function(classNode) {
	        	print("[JMTSUPERTRANS] ServerWorldTickChunk Transformer Called");
	        	
	        	var opcodes = Java.type('org.objectweb.asm.Opcodes');
	        	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	        	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	        	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
	        	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	        	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	        	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	        	var MethodType = asmapi.MethodType;
	        	
	        	var methods = classNode.methods;
	        	
	        	var tgtdesc = "(JZLnet/minecraft/world/spawner/WorldEntitySpawner$EntityDensityManager;ZILnet/minecraft/world/server/ChunkHolder;)V"
	        	
	    		for (var i in methods) {
	        		var method = methods[i];
	        		
	        		if (!method.desc.equals(tgtdesc)) {
	        			continue;
	        		}
	        		
	        		print("[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
	        		
	        		var instructions = method.instructions;
	        		
	        		var callMethod = asmapi.mapMethod("func_217441_a");
	        		var callClass = "net/minecraft/world/server/ServerWorld";
	        		var callDesc = "(Lnet/minecraft/world/chunk/Chunk;I)V";
	        	
	        		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
	        				callClass, callMethod, callDesc, 0);
	        		
	        		if (callTarget == null) {
	        			print("[JMTSUPERTRANS] MISSING TARGET INSN");
	        			return;
	        		}
	        		
	        		// Jump instruction
	        		var skipTarget = new LabelNode();
	        		            		
	        		var il = new InsnList();
	        		il.add(new VarInsnNode(opcodes.ALOAD, 0)); // Is lambda so `this` isn't local var 0
	        		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
	        				"org/jmt/mcmt/asmdest/ASMHookTerminator", "callTickEnvironment",
	        				"(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/chunk/Chunk;ILnet/minecraft/world/server/ServerChunkProvider;)V" ,false));
	        		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
	        		
	        		instructions.insertBefore(callTarget, il);
	        		instructions.insert(callTarget, skipTarget);
	        		
	        		break;
	        	
	    		}
	        	
	        	print("[JMTSUPERTRANS] ServerWorldTickChunk Transformer Complete");
	        	
	        	return classNode;
	        }
		}
	}
    
    
}