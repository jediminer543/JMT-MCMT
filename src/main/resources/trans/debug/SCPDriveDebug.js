function printInsnNode(printTgt) {
	print(printTgt+"|"+printTgt.opcode
			+"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"])
}

function initializeCoreMod() {
    return {
        'SCPDriveDebug': {
            'target': {
            	'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                "methodName": "func_212849_a_",
        		"methodDesc": "(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/IChunk;"
            },
            'transformer': function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;

				asmapi.log("INFO", "[JMTSUPERTRANS] SCPDriveDebug Transformer Called");

            	var targetMethodOwner = "net/minecraft/world/server/ServerChunkProvider$ChunkExecutor";
            	var targetMethodName = asmapi.mapMethod("func_213161_c"); 
            	var targetMethodDesc = "(Ljava/util/function/BooleanSupplier;)V";
            	
            	var method = methodNode
            	        		
        		var instructions = methodNode.instructions;
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
        				targetMethodOwner, targetMethodName, targetMethodDesc, 0);
        		
        		if (callTarget != null) {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] FOUND TARGET INSNS");
        		} else {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] MISSING TARGET INSNS:");
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] HAVE CALL:" + (callTarget != null));
        			return;
        		}
        		
        		//Call Hook
        		var skipTarget = new LabelNode();

				var cfl = 8;
				
				var targetVar = null;
				for (var idx in method.localVariables) {
            		var lv = method.localVariables[idx];
					if (lv == null) continue; // Should never be hit but I don't trust that'
					asmapi.log("WARN", "[JMTSUPERTRANS] " + lv.desc + ":" + lv.index);
					if (lv.index == cfl && lv.desc.endsWith("CompletableFuture;")) {
						asmapi.log("WARN", "[JMTSUPERTRANS] found: " + lv.desc + ":" + lv.index);
						targetVar = lv;
					}
				}
				
				
				if (targetVar == null) {
					//asmapi.log("WARN", "[JMTSUPERTRANS] );
					cfl = 9;
					asmapi.log("WARN", "[JMTSUPERTRANS] You are using new forge!");
				}
        		
        		var il = new InsnList();
        		il.add(new VarInsnNode(opcodes.ALOAD, 0));
        		il.add(new VarInsnNode(opcodes.ALOAD, cfl));
        		il.add(new VarInsnNode(opcodes.LLOAD, 6));
        		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
        				"org/jmt/mcmt/asmdest/ChunkRepairHookTerminator", "chunkLoadDrive",
        				"(Lnet/minecraft/world/server/ServerChunkProvider$ChunkExecutor;Ljava/util/function/BooleanSupplier;Lnet/minecraft/world/server/ServerChunkProvider;Ljava/util/concurrent/CompletableFuture;J)V"        				,false));
        		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
        		
        		instructions.insertBefore(callTarget, il);
        		instructions.insert(callTarget, skipTarget);

				asmapi.log("INFO", "[JMTSUPERTRANS] SCPDriveDebug Transformer Complete");
          
                return methodNode;
            }
        },
        'InitialChunkCountBypass': {
            'target': {
            	'type': 'METHOD',
                'class': 'net.minecraft.server.MinecraftServer',
                "methodName": "func_213186_a",
        		"methodDesc": "(Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V"
            },
            'transformer': function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;

				asmapi.log("INFO", "[JMTSUPERTRANS] InitialChunkCountBypass Transformer Called");

            	//mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/server/ServerChunkProvider", "getLoadedChunksCount", "()I", false);
            	var targetMethodOwner = "net/minecraft/world/server/ServerChunkProvider";
            	var targetMethodName = asmapi.mapMethod("func_217229_b"); 
            	var targetMethodDesc = "()I";
            	
            	var method = methodNode
            	        		
        		var instructions = methodNode.instructions;
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
        				targetMethodOwner, targetMethodName, targetMethodDesc, 0);
        		
        		if (callTarget != null) {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] FOUND TARGET INSNS");
        		} else {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] MISSING TARGET INSNS:");
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] HAVE CALL:" + (callTarget != null));
        			return;
        		}
        		
        		var ultraTgt = callTarget.getNext().getNext();
        		var labelTgt = ultraTgt.label;
        		
        		
        		//Call Hook
        		
        		var il = new InsnList();
        		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
        				"org/jmt/mcmt/asmdest/ChunkRepairHookTerminator", "isBypassLoadTarget",
        				"()Z"
        				,false));
        		il.add(new JumpInsnNode(opcodes.IFNE, labelTgt));
        		
        		instructions.insert(ultraTgt, il);

				asmapi.log("INFO", "[JMTSUPERTRANS] InitialChunkCountBypass Transformer Complete");
          
                return methodNode;
            }
        },
/*
		'ChunkHolderNullCheck': {
            'target': {
            	'type': 'METHOD',
                'class': 'net.minecraft.world.server.ChunkHolder',
                "methodName": "func_219276_a",
        		"methodDesc": "(Lnet/minecraft/world/chunk/ChunkStatus;Lnet/minecraft/world/server/ChunkManager;)Ljava/util/concurrent/CompletableFuture;"
            },
            'transformer': function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;

				asmapi.log("INFO", "[JMTSUPERTRANS] ChunkHolderNullCheck Transformer Called");

				//INVOKEVIRTUAL java/util/concurrent/CompletableFuture.getNow(Ljava/lang/Object;)Ljava/lang/Object;
            	//mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/server/ServerChunkProvider", "getLoadedChunksCount", "()I", false);
            	var targetMethodOwner = "java/util/concurrent/CompletableFuture";
            	var targetMethodName = "getNow";//asmapi.mapMethod("func_217229_b"); 
            	var targetMethodDesc = "(Ljava/lang/Object;)Ljava/lang/Object;";
            	
            	var method = methodNode
            	        		
        		var instructions = methodNode.instructions;
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
        				targetMethodOwner, targetMethodName, targetMethodDesc, 0);
        		
        		if (callTarget != null) {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] FOUND TARGET INSNS");
        		} else {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] MISSING TARGET INSNS:");
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] HAVE CALL:" + (callTarget != null));
        			return;
        		}
        		
        		
        		//Call Hook
        		
        		var il = new InsnList();
				//il.add(new InsnNode(opcodes.DUP));
        		//il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
        		//		"org/jmt/mcmt/asmdest/ChunkRepairHookTerminator", "checkNull",
        		//		"(Ljava/lang/Object;)V", 
				//		false));
        		
        		instructions.insert(callTarget, il);

				asmapi.log("INFO", "[JMTSUPERTRANS] ChunkHolderNullCheck Transformer Complete");
          
                return methodNode;
            }
        },
*/
    }
}
