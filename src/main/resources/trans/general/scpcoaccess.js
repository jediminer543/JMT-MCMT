// Functions that make parallel stuff able to load/access world stuff
function initializeCoreMod() {
    return {
    	'UtilExecutorListingHack': {
    		//
    		'target': {
    			'type': 'CLASS',
                'name': 'net.minecraft.util.Util'
    		},
    		"transformer": function(classNode) {
    			var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] UtilExecutorListingHack Transformer Called");
    			
    			var desc = "(Ljava/lang/String;Ljava/util/concurrent/ForkJoinPool;)Ljava/util/concurrent/ForkJoinWorkerThread;" 
    			
    			for (methodid in classNode.methods) {
    				var methodNode = classNode.methods[methodid];
    				
    				if (methodNode.desc != desc) {
    					continue;
    				}
    				
    				asmapi.log("DEBUG", "FOUND PICKUP TARGET")
    				
    				var instructions = methodNode.instructions;
    				
    				//    INVOKEVIRTUAL java/util/concurrent/ForkJoinWorkerThread.setName(Ljava/lang/String;)V
    				var target = asmapi.findFirstMethodCallAfter(methodNode, MethodType.VIRTUAL, 
    						"java/util/concurrent/ForkJoinWorkerThread", "setName", "(Ljava/lang/String;)V", 0);
    				
    				var il = new InsnList();
                	il.add(new VarInsnNode(opcodes.ALOAD, 0));
                	il.add(new VarInsnNode(opcodes.ALOAD, 2));
                	il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "regThread",
            				"(Ljava/lang/String;Ljava/lang/Thread;)V" ,false));
                	instructions.insert(target, il);
                	
                	break;
    			}
    			
    			asmapi.log("INFO", "[JMTSUPERTRANS] UtilExecutorListingHack Transformer Complete");
    			
    			return classNode
    		}
    	},
    	'SCPGetChunkPatch': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                "methodName": "func_212849_a_",
        		"methodDesc": "(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/IChunk;"
            },
            "transformer": function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] SCPGetChunkPatch Transformer Called");
            	
            	var instructions = methodNode.instructions;
            	
            	asmapi.log("DEBUG", "[JMTSUPERTRANS] Patching thread check");
            	            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] SCPGetChunkPatch Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'SCPGetChunkNowPatch': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                "methodName": "func_225313_a",
        		"methodDesc": "(II)Lnet/minecraft/world/chunk/Chunk;"
            },
            "transformer": function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] SCPGetChunkNowPatch Transformer Called");
            	
            	var instructions = methodNode.instructions;
            	
            	// We can synchronize this as it would normally return null under threading
            	// So it's concurrent access normally doesn't matter; QED
            	methodNode.access += opcodes.ACC_SYNCHRONIZED;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] SCPGetChunkNowPatch Transformer Complete");
            	
            	return methodNode;
            }
    	},
    }
}