//TODO
/*
function toParallelHashSets(classNode, fastOwner, concurrentOwner, methodMap, ignore=[]) {
	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	
	var MethodType = asmapi.MethodType;
	
	var methods = classNode.methods;
	
	for (var i in methods) {
		var methodNode = methods[i];
		if (ignore.includes(methodNode.name)) {
			continue;
		}
		var instructions = methodNode.instructions;
		
		
	}
	
}
*/

function printInsnNode(printTgt) {
	print(printTgt+"|"+printTgt.opcode
			+"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"])
}

function getFieldAccessAfter(methodNode, opcode, owner, name, descriptor, after) {
	var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	var node = methodNode.instructions.get(after);
	while (node != null) {
		if (node instanceof FieldInsnNode) {
			if (node.getOpcode() == opcode &&
				node.name.equals(name) && 
				node.desc.equals(descriptor) &&
				node.owner.equals(owner)) {
				return node
			}
		}
		node = node.getNext();
	}
	return null;
	
}

function synchronizeOn(methodNode, before, loadInsns) {
	var opcodes = Java.type('org.objectweb.asm.Opcodes');
	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	
	var instructions = methodNode.instructions;
	
	var startInsn = instructions.get(before);
	
	var il = new InsnList();
	for (var insn in loadInsns) {
		il.add(loadInsns[insn].clone(null));
	}
	il.add(new InsnNode(opcodes.MONITORENTER));
	
	instructions.insertBefore(startInsn, il);
	
	var tgtInsn = startInsn.getNext();
	while (tgtInsn != null) {
		if (tgtInsn.getOpcode() == opcodes.IRETURN ||
				tgtInsn.getOpcode() == opcodes.LRETURN ||
				tgtInsn.getOpcode() == opcodes.FRETURN ||
				tgtInsn.getOpcode() == opcodes.DRETURN ||
				tgtInsn.getOpcode() == opcodes.ARETURN ||
				tgtInsn.getOpcode() == opcodes.RETURN) {
			il = new InsnList();
			for (var insn in loadInsns) {
				il.add(loadInsns[insn].clone(null));
			}
			il.add(new InsnNode(opcodes.MONITOREXIT));
			instructions.insert(tgtInsn, il);
		}
		tgtInsn = tgtInsn.getNext();
	}
	
}

function synchronizeMethod(methodNode, debugLine) {
	print("[JMTSUPERTRANS] " + debugLine + " Transformer Called");
	
	var opcodes = Java.type('org.objectweb.asm.Opcodes');
	
	methodNode.access += opcodes.ACC_SYNCHRONIZED;
	
	print("[JMTSUPERTRANS] " + debugLine + " Transformer Complete");
	
	return methodNode;
}

function initializeCoreMod() {
    return {
    	'ServerWorldBlockPosFastUtkill': {
    		'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerWorld'
            },
            "transformer": function(classNode) {
            	print("[JMTSUPERTRANS] ServerWorldBlockPosFastUtkill Transformer Called");
            	
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
        				"fallbackdesc2": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",
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
            			if (op.fallbackdesc == methodNode.desc || op.fallbackdesc2 == methodNode.desc) {
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
    	'ServerWorldEntitiesByIdFastUtkill': {
    		'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerWorld'
            },
            "transformer": function(classNode) {
            	print("[JMTSUPERTRANS] ServerWorldEntitiesByIdFastUtkill Transformer Called");
            	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
            	var FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var tgt = "it/unimi/dsi/fastutil/ints/Int2ObjectMap";
            	var replace = "it/unimi/dsi/fastutil/ints/Int2ObjectMap";
            	var fullReplace = "org/jmt/mcmt/paralelised/fastutil/Int2ObjectConcurrentHashMap";
            	
            	var methods = classNode.methods;
            	
            	var version = "";
            	
            	var targetMethods = {
            			"<init>": { // "Same" for 115 and 1161
            				"desc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V",
            				"fallbackdesc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",
            				"fallbackdesc2": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",
            				"update": function(methodNode) {
            					var instructions = methodNode.instructions;
            					
            					var initdesc1152 = "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V";
            					//TODO USE
            					var initdesc1162 = "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",;
            					
            					if (methodNode.desc.equals(initdesc1152)) {
            						version = "1152"
            					} else {
            						version = "1161"
            					}
            					print("[JMTSUPERTRANS] mcver:" + version);
            					
            					var initTgt = asmapi.findFirstMethodCall(methodNode, MethodType.SPECIAL, 
            							"it/unimi/dsi/fastutil/ints/Int2ObjectLinkedOpenHashMap", "<init>", "()V");
            					var putTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.PUTFIELD, instructions.indexOf(initTgt))
            					
            					var newTgt = initTgt;
            					
            					while (newTgt.getOpcode() != opcodes.NEW) {
            						newTgt = newTgt.getPrevious();
            					}
            					
            					if (initTgt == null || newTgt == null || putTgt == null) {
            						print("[JMTSUPERTRANS] MISSING TARGET INSN - INIT");
            						return false;
            					}
            					
            					newTgt.desc = fullReplace;
            					initTgt.owner = fullReplace;
        						putTgt.desc = putTgt.desc.replace(tgt, replace);
        						
        						return true;
            				}
            			},
                	}
            	
            	
            	for (var i in methods) {
            		var methodNode = methods[i];
            		
            		var op = targetMethods[methodNode.name];
            		if (op != undefined && (op.desc == methodNode.desc || op.fallbackdesc == methodNode.desc)) {
            			if (op.fallbackdesc == methodNode.desc || op.fallbackdesc2 == methodNode.desc) {
            				print("[JMTSUPERTRANS] 1.16 WARNING")
            			}
            			print("[JMTSUPERTRANS] updating method: " + methodNode.name + methodNode.desc)
            			var result = op.update(methodNode)
            			op.result = result;
            		} 
            	}
            	
            	for (var o in targetMethods) {
            		if (targetMethods[o].result != true) {
            			if (targetMethods[o]["not"+version]) {
            				continue;
            			}
            			print("[JMTSUPERTRANS] failed to update method: " + o + targetMethods[o].desc)
            		}
            	}
            	
            	return classNode;
            }
    	},
    }
}