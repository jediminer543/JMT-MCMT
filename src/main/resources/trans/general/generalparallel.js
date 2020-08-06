function synchronizeMethod(debugLine) {
	return function(methodNode) {
		print("[JMTSUPERTRANS] " + debugLine + " Transformer Called");
		
		var opcodes = Java.type('org.objectweb.asm.Opcodes');
		
		methodNode.access += opcodes.ACC_SYNCHRONIZED;
		
		print("[JMTSUPERTRANS] " + debugLine + " Transformer Complete");
		
		return methodNode;
	}
}

function printInsnNode(printTgt) {
	print(printTgt+"|"+printTgt.opcode
			+"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"])
}

function toParallelHashSets(methodNode) {
	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	var MethodType = asmapi.MethodType;
	var instructions = methodNode.instructions;
	
	var callMethod = "newHashSet";
	var callClass = "com/google/common/collect/Sets";
	var callDesc = "()Ljava/util/HashSet;";
	
	var tgtMethod = "newHashSet";
	var tgtClass = "org/jmt/mcmt/asmdest/ConcurrentCollections";
	var tgtDesc = "()Ljava/util/Set;";
	
	var invoke = asmapi.findFirstMethodCallAfter(methodNode, MethodType.STATIC, callClass, callMethod, callDesc, 0);
	if (invoke != null) {
		do {
			invoke.owner = tgtClass;
			invoke.name = tgtMethod;
			invoke.desc = tgtDesc;
		} while ((invoke = asmapi.findFirstMethodCallAfter(methodNode, 
				MethodType.STATIC, callClass, callMethod, callDesc, instructions.indexOf(invoke))) != null)
	}
}

function initializeCoreMod() {
    return {
    	'serverChunkProviderTick': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerChunkProvider',
                "methodName": "func_217207_a",
        		"methodDesc": "()V"
            },
            "transformer": synchronizeMethod("SCPTick")
    	},
    	'LevelBasedGraphProcessUpdates': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.lighting.LevelBasedGraph',
                "methodName": "func_215483_b",
        		"methodDesc": "(I)I"
            },
            "transformer": synchronizeMethod("LevelBasedGraphProcessUpdates")
    	},
    	'ServerTickListTick': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerTickList',
                "methodName": "func_205365_a",
        		"methodDesc": "()V"
            },
            "transformer": synchronizeMethod("ServerTickListTick")
    	},
    	'ServerTickListAddEntry': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerTickList',
                "methodName": "func_219504_a",
        		"methodDesc": "(Lnet/minecraft/world/NextTickListEntry;)V"
            },
            "transformer": synchronizeMethod("ServerTickListAddEntry")
    	},
    	'ServerTickListGetPending': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerTickList',
                "methodName": "func_223187_a",
        		"methodDesc": "(Ljava/util/List;Ljava/util/Collection;Lnet/minecraft/util/math/MutableBoundingBox;Z)Ljava/util/List;"
            },
            "transformer": synchronizeMethod("ServerTickListGetPending")
    	},
    	'ServerWorldCollections': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                "methodName": "<init>",
        		"methodDesc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] ServerWorldCollections Transformer Called");
            	
            	toParallelHashSets(methodNode);
            	
            	print("[JMTSUPERTRANS] ServerWorldCollections Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'ServerWorldCollections116': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                "methodName": "<init>",
        		"methodDesc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] ServerWorldCollections116 Transformer Called");
            	
            	toParallelHashSets(methodNode);
            	
            	print("[JMTSUPERTRANS] ServerWorldCollections116 Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'ServerWorldFastUtkill': {
    		'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerWorld'
            },
            "transformer": function(classNode) {
            	print("[JMTSUPERTRANS] ServerWorldFastUtkill Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var tgt = "it/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet";
            	var replace = "java/util/Deque";
            	
            	var fields = classNode.fields;
            	
            	var tgtField = asmapi.mapField("field_147490_S");
            	
            	for (var i in fields) {
            		var fieldNode = fields[i];
            		
            		if (fieldNode.name != tgtField) {
            			continue;
            		}
            		
            		fieldNode.signature = fieldNode.signature.replace(tgt, replace);
            		fieldNode.desc = fieldNode.desc.replace(tgt, replace);
            		print(fieldNode.name + "|" + fieldNode.desc + "|" + fieldNode.signature);
            	}
            	
            	var methods = classNode.methods;
            	
            	var targetMethods = {
        			"<init>": {
        				"desc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V",
        				"fallbackdesc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",
        				"update": function(methodNode) {
        					var instructions = methodNode.instructions;
        					
        					var initTgt = asmapi.findFirstMethodCall(methodNode, MethodType.SPECIAL, tgt, "<init>", "()V");
        					//var newTgt = asmapi.findFirstInstructionBefore(methodNode, opcodes.NEW, instructions.indexOf(initTgt))
        					var putTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.PUTFIELD, instructions.indexOf(initTgt))
        					
        					var newTgt = initTgt;
        					
        					while (newTgt.getOpcode() != opcodes.NEW) {
        						newTgt = newTgt.getPrevious();
        					}
        					
        					if (initTgt == null || newTgt == null || putTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - INIT");
        						return false;
        					}
        					
        					newTgt.desc = "java/util/concurrent/ConcurrentLinkedDeque";
        					initTgt.owner = "java/util/concurrent/ConcurrentLinkedDeque";
    						putTgt.desc = putTgt.desc.replace(tgt, replace);
    						
        					/*
    						var printTgt = newTgt.getPrevious().getPrevious();
    						for (var i = 0; i < 30; i++) {
    							printInsnNode(printTgt);
    							printTgt = printTgt.getNext();
    						}
    						*/
    						
    						return true;
        				}
        			},
            	}
            	
            	targetMethods[asmapi.mapMethod("func_175641_c")] = {
            			"desc": "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;II)V",
            			"update": function(methodNode) {
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				var addTgt = asmapi.findFirstMethodCall(methodNode, MethodType.VIRTUAL, tgt, "add", "(Ljava/lang/Object;)Z");
            				
            				if (addTgt == null || getTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - addBlockEvent");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				addTgt.owner = replace;
            				addTgt.setOpcode(opcodes.INVOKEINTERFACE);
            				addTgt.itf = true;
            				
            				return true;
            			}
            	}
            	
            	targetMethods[asmapi.mapMethod("func_229854_a_")] = {
            			"desc": "(Lnet/minecraft/util/math/MutableBoundingBox;)V",
            			"update": function(methodNode) {
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				var addTgt = asmapi.findFirstMethodCall(methodNode, MethodType.VIRTUAL, tgt, "removeIf", "(Ljava/util/function/Predicate;)Z");
            				
            				if (addTgt == null || getTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - addBlockEvent");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				addTgt.owner = replace;
            				addTgt.setOpcode(opcodes.INVOKEINTERFACE);
            				addTgt.itf = true;
            				
            				return true;
            			}
            	}
            	
            	targetMethods[asmapi.mapMethod("func_147488_Z")] = {
            			"desc": "()V",
            			"update": function(methodNode) {
            				var instructions = methodNode.instructions;
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				
            				var call = asmapi.buildMethodCall("org/jmt/mcmt/asmdest/ASMHookTerminator", 
            						"sendQueuedBlockEvents", "(Ljava/util/Deque;Lnet/minecraft/world/server/ServerWorld;)V",  MethodType.STATIC)
            						
            				if (getTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - addBlockEvent");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            						
            				var il = new InsnList();
            				il.add(new VarInsnNode(opcodes.ALOAD, 0));
            				il.add(call);
            				il.add(new InsnNode(opcodes.RETURN));
            				
            				instructions.insert(getTgt, il);
            				
            				return true;
            			}
            	}
            	
            	for (var i in methods) {
            		var methodNode = methods[i];
            		
            		var op = targetMethods[methodNode.name];
            		if (op != undefined && (op.desc == methodNode.desc || op.fallbackdesc == methodNode.desc)) {
            			if (op.fallbackdesc == methodNode.desc) {
            				print("[JMTSUPERTRANS] 1.16 WARNING")
            			}
            			print("[JMTSUPERTRANS] updating method: " + methodNode.name + methodNode.desc)
            			var result = op.update(methodNode)
            			op.result = result;
            		}
            	}
            	
            	for (var o in targetMethods) {
            		if (targetMethods[o].result != true) {
            			print("[JMTSUPERTRANS] failed to update method: " + o + targetMethods[o].desc)
            		}
            	}
            	
            	
            	
            	return classNode;
            }
    	},
    	'ServerWorldParaProvider': {
    		'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerWorld'
            },
            "transformer": function(classNode) {
            	print("[JMTSUPERTRANS] ServerWorldParaProvider Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var MethodType = asmapi.MethodType;
            	
            	var tgtdesc = "(Lnet/minecraft/world/storage/SaveHandler;Ljava/util/concurrent/Executor;Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/World;Lnet/minecraft/world/dimension/Dimension;)Lnet/minecraft/world/chunk/AbstractChunkProvider;"
            		
        		var methods = classNode.methods;
            		
        		for (var i in methods) {
            		var method = methods[i];
            		
            		if (!method.desc.equals(tgtdesc)) {
            			continue;
            		}
            		
            		print("[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var callMethod = "<init>";
            		var callClass = "net/minecraft/world/server/ServerChunkProvider";
            		var callDesc = "(Lnet/minecraft/world/server/ServerWorld;Ljava/io/File;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/gen/feature/template/TemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/ChunkGenerator;ILnet/minecraft/world/chunk/listener/IChunkStatusListener;Ljava/util/function/Supplier;)V";
            	
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.SPECIAL, 
            				callClass, callMethod, callDesc, 0);
            		
            		if (callTarget == null) {
            			print("[JMTSUPERTRANS] MISSING TARGET INSN");
            			return;
            		}
            		
            		var tgtMethod = callMethod;
            		var tgtClass = "org/jmt/mcmt/paralelised/ParaServerChunkProvider";
            		var tgtDesc = callDesc;
            		
            		var newTgt = callTarget;
					
					while (newTgt.getOpcode() != opcodes.NEW) {
						newTgt = newTgt.getPrevious();
					}
            		
            		callTarget.owner = tgtClass;
            		callTarget.name = tgtMethod;
            		callTarget.desc = tgtDesc;
            		
            		newTgt.desc = tgtClass;
        		}
            		
        		print("[JMTSUPERTRANS] ServerWorldParaProvider Transformer Complete");
            	
            	return classNode;
            }
    	},
    	'ServerWorldParaProvider116': {
    		'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                'methodName': "<init>",
                'methodDesc': "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V"
            },
            "transformer": function(method) {
            	print("[JMTSUPERTRANS] ServerWorldParaProvider116 Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var MethodType = asmapi.MethodType;
        		
        		print("[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
        		
        		var callMethod = "<init>";
        		var callClass = "net/minecraft/world/server/ServerChunkProvider";
        		var callDesc = "(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/gen/feature/template/TemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/ChunkGenerator;IZLnet/minecraft/world/chunk/listener/IChunkStatusListener;Ljava/util/function/Supplier;)V"
        	
        		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.SPECIAL, 
        				callClass, callMethod, callDesc, 0);
        		
        		if (callTarget == null) {
        			print("[JMTSUPERTRANS] MISSING TARGET INSN");
        			return;
        		}
        		
        		var tgtMethod = callMethod;
        		var tgtClass = "org/jmt/mcmt/paralelised/ParaServerChunkProvider";
        		var tgtDesc = callDesc;
        		
        		var newTgt = callTarget;
				
				while (newTgt.getOpcode() != opcodes.NEW) {
					newTgt = newTgt.getPrevious();
				}
        		
        		callTarget.owner = tgtClass;
        		callTarget.name = tgtMethod;
        		callTarget.desc = tgtDesc;
        		
        		newTgt.desc = tgtClass;
            		
        		print("[JMTSUPERTRANS] ServerWorldParaProvider Transformer Complete");
            	
            	return method;
            }
    	},
    	'PlayerEntityRemoveQueueSync': {
    		'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.player.ServerPlayerEntity',
                'methodName': "<init>",
                'methodDesc': "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/server/ServerWorld;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/management/PlayerInteractionManager;)V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] PlayerEntityRemoveQueueSync Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var instructions = methodNode.instructions;
            	            	
            	var callMethod = "newLinkedList";
        		var callClass = "com/google/common/collect/Lists";
        		var callDesc = "()Ljava/util/LinkedList;";
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(methodNode, MethodType.STATIC, 
        				callClass, callMethod, callDesc, 0);
        		
        		var newClass = "org/jmt/mcmt/paralelised/ConcurrentDoublyLinkedList"
        		
        		var il = new InsnList();
        		il.add(new InsnNode(opcodes.POP));
            	il.add(new TypeInsnNode(opcodes.NEW, newClass));
            	il.add(new InsnNode(opcodes.DUP));
            	il.add(new MethodInsnNode(opcodes.INVOKESPECIAL, 
            			newClass, "<init>", "()V", false));
            	
            	instructions.insert(callTarget, il);
            	            	
            	print("[JMTSUPERTRANS] PlayerEntityRemoveQueueSync Transformer Complete");
            	
            	return methodNode;
            }
    	}
    }
}