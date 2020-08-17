function initializeCoreMod() {
    return {
    	'executorListingHack': {
    		//
    		'target': {
    			'type': 'CLASS',
                'name': 'net.minecraft.util.Util'
    		},
    		"transformer": function(classNode) {
    			print("[JMTSUPERTRANS] executorListingHack Transformer Called");
    			
    			var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var MethodType = asmapi.MethodType;
    			
    			var desc = "(Ljava/lang/String;Ljava/util/concurrent/ForkJoinPool;)Ljava/util/concurrent/ForkJoinWorkerThread;" 
    			
    			for (methodid in classNode.methods) {
    				var methodNode = classNode.methods[methodid];
    				
    				if (methodNode.desc != desc) {
    					continue;
    				}
    				
    				print("FOUND PICKUP TARGET")
    				
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
    			
    			print("[JMTSUPERTRANS] executorListingHack Transformer Called");
    			
    			return classNode
    		}
    	},
    	'serverChunkProviderGetChunkPatch': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                "methodName": "func_212849_a_",
        		"methodDesc": "(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/IChunk;"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] GetChunkPatch Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	
            	
            	var instructions = methodNode.instructions;
            	
            	print("[JMTSUPERTRANS] Patching thread check");
            	            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	/* Not needed; and broken; built as default cache WILL die under concurrency
            	print("[JMTSUPERTRANS] Patching thread caching"); 
            	
            	//mv.visitFieldInsn(GETSTATIC, "org/jmt/mcmt/config/GeneralConfig", "disabled", "Z");
            	var cacheTarget = instructions.getFirst();
            	
            	while (!(cacheTarget instanceof VarInsnNode) 
            			&& cacheTarget.getOpcode() != opcodes.ILOAD
            			&& cacheTarget["var"] != 8) {
            		cacheTarget = cacheTarget.getNext()
            	}
            	cacheTarget = cacheTarget.getNext()
            	cacheTarget = cacheTarget.getNext()
            	
            	var cacheEndLbl = cacheTarget.label;
            	
            	il = new InsnList();
            	il.add(new FieldInsnNode(opcodes.GETSTATIC, "org/jmt/mcmt/config/GeneralConfig", "disabled", "Z"));
            	il.add(new JumpInsnNode(opcodes.IFNE, cacheEndLbl));
            	
            	instructions.insert(cacheTarget, il);
            	*/
            	
            	print("[JMTSUPERTRANS] GetChunkPatch Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'serverChunkProviderGetChunkNowPatch': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                "methodName": "func_225313_a",
        		"methodDesc": "(II)Lnet/minecraft/world/chunk/Chunk;"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] GetChunkNowPatch Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	
            	
            	var instructions = methodNode.instructions;
            	
            	methodNode.access += opcodes.ACC_SYNCHRONIZED;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	
            	print("[JMTSUPERTRANS] GetChunkNowPatch Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'classInheritanceMultiMapList': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.util.ClassInheritanceMultiMap',
                "methodName": "<init>",
        		"methodDesc": "(Ljava/lang/Class;)V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] MultiMapList Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var instructions = methodNode.instructions;
            	            	
            	var callMethod = "newArrayList";
        		var callClass = "com/google/common/collect/Lists";
        		var callDesc = "()Ljava/util/ArrayList;";
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(methodNode, MethodType.STATIC, 
        				callClass, callMethod, callDesc, 0);
        		
        		methodNode.maxStack += 1;
            	
            	var il = new InsnList();
            	// Purge old arrayList
            	il.add(new InsnNode(opcodes.POP));
            	/*
            	mv.visitTypeInsn(NEW, "java/util/concurrent/CopyOnWriteArrayList");
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/CopyOnWriteArrayList", "<init>", "()V", false);
            	 */
            	il.add(new TypeInsnNode(opcodes.NEW, "java/util/concurrent/CopyOnWriteArrayList"));
            	il.add(new InsnNode(opcodes.DUP));
            	il.add(new MethodInsnNode(opcodes.INVOKESPECIAL, 
            			"java/util/concurrent/CopyOnWriteArrayList", "<init>", "()V", false));
            	instructions.insert(callTarget, il);
            	
            	print("[JMTSUPERTRANS] MultiMapList Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	"ServerTickListSelfRepair": {
    		'target': {
    			'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerTickList',
                "methodName": "func_205365_a",
        		"methodDesc": "()V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] ServerTickListSR Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var instructions = methodNode.instructions;
            	
            	var targetPre = asmapi.findFirstInstruction(methodNode, opcodes.NEW);
            	var targetPost = asmapi.findFirstInstruction(methodNode, opcodes.ATHROW);
            	
            	var skipTarget = new LabelNode();
            	
            	var il = new InsnList();
            	
            	il.add(new VarInsnNode(opcodes.ALOAD, 0));
        		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
        				"org/jmt/mcmt/asmdest/ASMHookTerminator", "fixSTL",
        				"(Lnet/minecraft/world/server/ServerTickList;)V" ,false));
        		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
            	
        		instructions.insertBefore(targetPre, il);
        		instructions.insert(targetPost, skipTarget);
            	
        		print("[JMTSUPERTRANS] ServerTickListSR Transformer Complete");
        		
        		return methodNode
            }
    	},
    	'PalettedContainerLock': {
    		'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.util.palette.PalettedContainer',
                "methodName": "func_210459_b",
        		"methodDesc": "()V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] PalettedContainerLock Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	
            	var instructions = methodNode.instructions;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.IFNE);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new JumpInsnNode(opcodes.GOTO, target.label));
            	
            	instructions.insertBefore(target, il);
            	
            	print("[JMTSUPERTRANS] PalettedContainerLock Transformer Complete");
            	
            	return methodNode
            }
    	},
    	'WorldGetTE': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.World',
                "methodName": "func_175625_s",
        		"methodDesc": "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] WorldGetTE Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	
            	
            	var instructions = methodNode.instructions;
            	
            	methodNode.access += opcodes.ACC_SYNCHRONIZED;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	var target = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, instructions.indexOf(target)+1);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	
            	print("[JMTSUPERTRANS] WorldGetTE Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'ClassInheritanceMultiMapGBC': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.util.ClassInheritanceMultiMap'
            },
            "transformer": function(classNode) {
            	print("[JMTSUPERTRANS] ClassInheritanceMultiMapGBC Transformer Called");
            	
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var MethodType = asmapi.MethodType;
            	
            	var tgtdesc = "(Ljava/lang/Class;)Ljava/util/List;"
            	
        		var methods = classNode.methods;
            	
            	for (var i in methods) {
            		var method = methods[i];
            		
            		if (!method.desc.equals(tgtdesc)) {
            			continue;
            		}
            		
            		print("[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		            		
            		var callMethod = "toList";
            		var callClass = "java/util/stream/Collectors";
            		var callDesc = "()Ljava/util/stream/Collector;";
            	
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.STATIC, 
            				callClass, callMethod, callDesc, 0);
            		
            		if (callTarget == null) {
            			print("[JMTSUPERTRANS] MISSING TARGET INSN");
            			return;
            		}
            		
            		var tgtMethod = "toList";
            		var tgtClass = "org/jmt/mcmt/asmdest/ConcurrentCollections";
            		var tgtDesc = "()Ljava/util/stream/Collector;";
            		
            		callTarget.owner = tgtClass;
            		callTarget.name = tgtMethod;
            		callTarget.desc = tgtDesc;
            		
            		break;
            	
        		}
            		
            	print("[JMTSUPERTRANS] ClassInheritanceMultiMapGBC Transformer Complete");
            	
            	return classNode;
            }
    	},
    }
}