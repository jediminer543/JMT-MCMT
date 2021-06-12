function synchronizeMethod(debugLine) {
	return function(methodNode) {
		var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
		
		asmapi.log("INFO", "[JMTSUPERTRANS] " + debugLine + " Transformer Called");
		
		var opcodes = Java.type('org.objectweb.asm.Opcodes');
		
		methodNode.access += opcodes.ACC_SYNCHRONIZED;
		
		asmapi.log("INFO", "[JMTSUPERTRANS] " + debugLine + " Transformer Complete");
		
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

function toParallelHashMaps(methodNode) {
	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	var MethodType = asmapi.MethodType;
	var instructions = methodNode.instructions;
	
	var callMethod = "newHashMap";
	var callClass = "com/google/common/collect/Maps";
	var callDesc = "()Ljava/util/HashMap;";
	
	var tgtMethod = "newHashMap";
	var tgtClass = "org/jmt/mcmt/asmdest/ConcurrentCollections";
	var tgtDesc = "()Ljava/util/Map;";
	
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

function toParallelDeque(methodNode) {
	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	var MethodType = asmapi.MethodType;
	var instructions = methodNode.instructions;
	
	//com/google/common/collect/Queues.newArrayDeque()Ljava/util/ArrayDeque;
	var callMethod = "newArrayDeque";
	var callClass = "com/google/common/collect/Queues";
	var callDesc = "()Ljava/util/ArrayDeque;";
	
	var tgtMethod = "newArrayDeque";
	var tgtClass = "org/jmt/mcmt/asmdest/ConcurrentCollections";
	var tgtDesc = "()Ljava/util/Queue;";
	
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
    	/*
    	'LevelBasedGraphProcessUpdates': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.lighting.LevelBasedGraph',
                "methodName": "func_215483_b",
        		"methodDesc": "(I)I"
            },
            "transformer": synchronizeMethod("LevelBasedGraphProcessUpdates")
    	},
    	*/
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
				toParallelDeque(methodNode);
            	
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
				toParallelDeque(methodNode);
            	
            	print("[JMTSUPERTRANS] ServerWorldCollections116 Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'ServerWorldCollections1162': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                "methodName": "<init>",
        		"methodDesc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] ServerWorldCollections116 Transformer Called");
            	
            	toParallelHashSets(methodNode);
				toParallelDeque(methodNode);
            	
            	print("[JMTSUPERTRANS] ServerWorldCollections116 Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	//onBlockStateChange
    	'ServerWorldOnBlockStateChange': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                "methodName": "func_217393_a",
        		"methodDesc": "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"
            },
            "transformer": synchronizeMethod("ServerWorldOnBlockStateChange")
    	},
    	//processUpdates net.minecraft.world.lighting.LevelBasedGraph func_215483_b(I)I
    	'LevelBasedGraph-processUpdates': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.lighting.LevelBasedGraph',
                "methodName": "func_215483_b",
        		"methodDesc": "(I)I"
            },
            "transformer": synchronizeMethod("LevelBasedGraph-processUpdates")
    	},
    	//public net.minecraft.world.lighting.LevelBasedGraph func_215469_a(JJIZ)V # scheduleUpdate
    	'LevelBasedGraph-scheduleUpdate': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.lighting.LevelBasedGraph',
                "methodName": "func_215469_a",
        		"methodDesc": "(JJIZ)V"
            },
            "transformer": synchronizeMethod("LevelBasedGraph-scheduleUpdate")
    	},
    	'POIManager_func_219149_a_': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.village.PointOfInterestManager',
                "methodName": "func_219149_a",
        		"methodDesc": "(Lnet/minecraft/util/math/ChunkPos;Ljava/lang/Integer;)Ljava/util/Optional"
            },
            "transformer": synchronizeMethod("POIManager_func_219149_a_")
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
    	'ServerWorldParaProvider1162': {
    		'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                'methodName': "<init>",
                'methodDesc': "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",
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
    	},
		'TemplateManagerHashMap': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.gen.feature.template.TemplateManager',
                "methodName": "<init>",
        		"methodDesc": "(Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lcom/mojang/datafixers/DataFixer;)V"
            },
            "transformer": function(methodNode) {
            	print("[JMTSUPERTRANS] TemplateManagerHashMap Transformer Called");
            	
            	toParallelHashMaps(methodNode);
            	
            	print("[JMTSUPERTRANS] TemplateManagerHashMap Transformer Complete");
            	
            	return methodNode;
            }
    	},
    }
}