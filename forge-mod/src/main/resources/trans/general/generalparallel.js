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

function synchronizeClass(debugLine) {
	return function(classNode) {
		var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
		var opcodes = Java.type('org.objectweb.asm.Opcodes');
		
		
		var posfilter = opcodes.ACC_PUBLIC;
		var negfilter = opcodes.ACC_STATIC | opcodes.ACC_SYNTHETIC | opcodes.ACC_NATIVE | opcodes.ACC_ABSTRACT
			| opcodes.ACC_ABSTRACT | opcodes.ACC_BRIDGE;
		
		asmapi.log("INFO", "[JMTSUPERTRANS] " + debugLine + " Transformer Called");
		
		for (var i in classNode.methods) {
			var methodNode = classNode.methods[i];
			if ((methodNode.access & posfilter) == posfilter && (methodNode.access & negfilter) == 0 && !methodNode.name.equals("<init>")) {
				asmapi.log("INFO", "[JMTSUPERTRANS] " + debugLine + " Transformer Hit " + methodNode.name);
				methodNode.access += opcodes.ACC_SYNCHRONIZED;
			}
		}
		
		asmapi.log("INFO", "[JMTSUPERTRANS] " + debugLine + " Transformer Complete");
		
		return classNode;
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
			asmapi.log("INFO", "[JMTSUPERTRANS] toParallelHashSets Transforming");
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
			asmapi.log("INFO", "[JMTSUPERTRANS] toParallelHashMaps Transforming");
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
			asmapi.log("INFO", "[JMTSUPERTRANS] toParallelHashSets Transforming");
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
                'class': 'net.minecraft.server.level.ServerChunkCache',
                "methodName": "m_142483_",
        		"methodDesc": "(Ljava/util/function/BooleanSupplier;)V"
            },
            "transformer": synchronizeMethod("SCPTick")
    	},
    	'ServerTickListTick': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.ServerTickList',
                "methodName": "m_47253_",
        		"methodDesc": "()V"
            },
            "transformer": synchronizeMethod("ServerTickListTick")
    	},
    	'ServerTickListAddEntry': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.ServerTickList',
                "methodName": "m_47227_",
        		"methodDesc": "(Lnet/minecraft/world/level/TickNextTickData;)V"
            },
            "transformer": synchronizeMethod("ServerTickListAddEntry")
    	},
    	'ServerWorldCollections': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.level.ServerLevel',
            },
            "transformer": function(classNode) {
				var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldCollections Transformer Called");            	


            	for (var i in classNode.methods) {
					
            		var methodNode = classNode.methods[i];

					if (!methodNode.name.equals("<init>")) {
            			continue;
            		}

					asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldCollections Transformer Ran");

            		toParallelHashSets(methodNode);
					toParallelDeque(methodNode);
				
				}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldCollections Transformer Complete");
            	
            	return classNode;
            }
    	},
    	//onBlockStateChange
    	'ServerWorldOnBlockStateChange': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.level.ServerLevel',
                "methodName": "m_6559_ ",
        		"methodDesc": "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/BlockState;Lnet/minecraft/world/level/block/BlockState;)V"
            },
            "transformer": synchronizeMethod("ServerWorldOnBlockStateChange")
    	},
		'TemplateManagerHashMap': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager',
            },
            "transformer": function(classNode) {
				var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	asmapi.log("INFO", "[JMTSUPERTRANS] TemplateManagerHashMap Transformer Called");
            	
				for (var i in classNode.methods) {
					
            		var methodNode = classNode.methods[i];

					if (!methodNode.name.equals("<init>")) {
            			continue;
            		}

					asmapi.log("INFO", "[JMTSUPERTRANS] TemplateManagerHashMap Transformer Ran");
					
					toParallelHashMaps(methodNode);
            		toParallelHashSets(methodNode);
					toParallelDeque(methodNode);
				
				}
            	
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] TemplateManagerHashMap Transformer Complete");
            	
            	return classNode;
            }
    	},
		'TicketManagerCollections': {
			'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.level.DistanceManager'
            },
            "transformer": function(classNode) {
	            var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	asmapi.log("INFO", "[JMTSUPERTRANS] TicketManagerCollections Transformer Called");
            	
				for (methodid in classNode.methods) {
					var methodNode = classNode.methods[methodid];
					if (methodNode.name != "<init>") {
    					continue;
    				}
					asmapi.log("INFO", "[JMTSUPERTRANS] TicketManagerCollections Hit Init");
					/*
					var insn = methodNode.instructions.getFirst();
					while (insn != null) {
						printInsnNode(insn);
						insn = insn.getNext();
					}
					*/
					toParallelHashMaps(methodNode);
					toParallelDeque(methodNode);
					toParallelHashSets(methodNode);
				}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] TicketManagerCollections Transformer Complete");
            	
            	return classNode;
            }
		},
		'ThreadTaskExecutorCollections': {
			'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.util.concurrent.ThreadTaskExecutor'
            },
            "transformer": function(classNode) {
	            var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	asmapi.log("INFO", "[JMTSUPERTRANS] ThreadTaskExecutorCollections Transformer Called");
            	
				for (methodid in classNode.methods) {
					var methodNode = classNode.methods[methodid];
					if (methodNode.name != "<init>") {
    					continue;
    				}
					asmapi.log("INFO", "[JMTSUPERTRANS] ThreadTaskExecutorCollections Hit Init");
					/*
					var insn = methodNode.instructions.getFirst();
					while (insn != null) {
						printInsnNode(insn);
						insn = insn.getNext();
					}
					*/
					toParallelHashMaps(methodNode);
					toParallelDeque(methodNode);
					toParallelHashSets(methodNode);
				}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ThreadTaskExecutorCollections Transformer Complete");
            	
            	return classNode;
            }
		},
		//Conversion limit
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
		'ServerTickListGetPending': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint',
                "methodName": "m_75588_",
        		"methodDesc": "(I)I"
            },
            "transformer": synchronizeMethod("DynamicGraphMinFixedPointRunUpdates")
    	},
		'EntityTickList': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.entity.EntityTickList',
            },
            "transformer": synchronizeClass("EntityTickList")
    	},
    }
}