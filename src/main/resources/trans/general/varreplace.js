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
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var tgt = "it/unimi/dsi/fastutil/ints/Int2ObjectMap";
            	var replace = "java/util/Map";
            	var fullReplace = "java/util/concurrent/ConcurrentHashMap";
            	var valuesTgt = "it/unimi/dsi/fastutil/objects/ObjectCollection";
            	var valuesReplace = "java/util/Collection";
            	var itrTgtDesc = "it/unimi/dsi/fastutil/objects/ObjectIterator";
            	var itrRep = "java/util/Iterator"
            	
            	var fields = classNode.fields;
            	
            	var tgtField = asmapi.mapField("field_217498_x");
            	
            	
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
            	
            	var version = "";
            	
            	var targetMethods = {
        			"<init>": { // "Same" for 115 and 1161
        				"desc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V",
        				"fallbackdesc": "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveFormat$LevelSave;Lnet/minecraft/world/storage/IServerWorldInfo;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/util/RegistryKey;Lnet/minecraft/world/DimensionType;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;Lnet/minecraft/world/gen/ChunkGenerator;ZJLjava/util/List;Z)V",
        				"update": function(methodNode) {
        					var instructions = methodNode.instructions;
        					
        					var initdesc1152 = "(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/storage/SaveHandler;Lnet/minecraft/world/storage/WorldInfo;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/world/chunk/listener/IChunkStatusListener;)V";
        					
        					if (methodNode.desc.equals(initdesc1152)) {
        						version = "1152"
        					} else {
        						version = "1161"
        					}
        					print("[JMTSUPERTRANS] mcver:" + version);
        					
        					var initTgt = asmapi.findFirstMethodCall(methodNode, MethodType.SPECIAL, 
        							"it/unimi/dsi/fastutil/ints/Int2ObjectLinkedOpenHashMap", "<init>", "()V");
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
        					
        					newTgt.desc = fullReplace;
        					initTgt.owner = fullReplace;
    						putTgt.desc = putTgt.desc.replace(tgt, replace);
    						
        					
    						
    						
    						return true;
        				}
        			},
            	}
            	
            	targetMethods[asmapi.mapMethod("func_241136_z_")] = { // 1161 only
            			"desc": "()Ljava/lang/Iterable;",
            			"not1152" : true,
            			"update": function(methodNode) {
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				var valTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, tgt, "values", "()Lit/unimi/dsi/fastutil/objects/ObjectCollection;");
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_241136_z_");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				valTgt.desc = valTgt.desc.replace(valuesTgt, valuesReplace);
            				
            				return true;
            			}
            	}
            	
            	
            	targetMethods[asmapi.mapMethod("func_217439_j")] = { // getDragons; Same for 115 and 1161
            			"desc": "()Ljava/util/List;",
            			"update": function(methodNode) {
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				var valTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, tgt, "values", "()Lit/unimi/dsi/fastutil/objects/ObjectCollection;");
            				var itrTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, valuesTgt, "iterator", "()Lit/unimi/dsi/fastutil/objects/ObjectIterator;");
            				
            				if (getTgt == null || valTgt == null || itrTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_241136_z_");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				valTgt.desc = valTgt.desc.replace(valuesTgt, valuesReplace);
            				itrTgt.owner = valuesReplace;
            				itrTgt.desc = itrTgt.desc.replace(itrTgtDesc, itrRep);
            				
            				return true;
            			}
            	}
            	
            	
            	targetMethods[asmapi.mapMethod("func_217482_a")] = { // getEntities ;Same for 115 and 1161
            			"desc": "(Lnet/minecraft/entity/EntityType;Ljava/util/function/Predicate;)Ljava/util/List;",
            			"update": function(methodNode) {
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				var valTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, tgt, "values", "()Lit/unimi/dsi/fastutil/objects/ObjectCollection;");
            				var itrTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, valuesTgt, "iterator", "()Lit/unimi/dsi/fastutil/objects/ObjectIterator;");
            				
            				if (getTgt == null || valTgt == null || itrTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217482_a");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				valTgt.desc = valTgt.desc.replace(valuesTgt, valuesReplace);
            				itrTgt.owner = valuesReplace;
            				itrTgt.desc = itrTgt.desc.replace(itrTgt, itrRep);
            				
            				return true;
            			}
            	}
            	
            	
            	targetMethods[asmapi.mapMethod("func_73045_a")] = { // getEntityByID ;Same for 115 and 1161
            			"desc": "(I)Lnet/minecraft/entity/Entity;",
            			"update": function(methodNode) {
            				
            				var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
            				var valTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, tgt, "get", "(I)Ljava/lang/Object;");
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_73045_a");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				valTgt.desc = "(Ljava/lang/Object;)Ljava/lang/Object;"
            				
        					var instructions = methodNode.instructions;
            				var insn = asmapi.buildMethodCall("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", MethodType.STATIC);
            				instructions.insertBefore(valTgt, insn);
            					
            				return true;
            			}
            	}
            	
            	
            	targetMethods[asmapi.mapMethod("func_217466_a")] = { // onChunkUnloading ;Same for 115 and 1161
            			"desc": "(Lnet/minecraft/world/chunk/Chunk;)V",
            			"update": function(methodNode) {
            				
            				var getTgt = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", 0)
            				var valTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, tgt, "remove", "(I)Ljava/lang/Object;");
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217466_a");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				valTgt.desc = "(Ljava/lang/Object;)Ljava/lang/Object;"
            				
        					var instructions = methodNode.instructions;
            				var insn = asmapi.buildMethodCall("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", MethodType.STATIC);
            				instructions.insertBefore(valTgt, insn);
            					
            				return true;
            			}
            	}
            	
            	
            	targetMethods[asmapi.mapMethod("func_217465_m")] = { // onEntityAdded ;Same for 115 and 1161
            			"desc": "(Lnet/minecraft/entity/Entity;)V",
            			"update": function(methodNode) {
            				
            				var instructions = methodNode.instructions;
            				
            				var getTgt = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", 0)
            				var valTgt = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, tgt, "put", "(ILjava/lang/Object;)Ljava/lang/Object;", instructions.indexOf(getTgt));
            				
            				var getTgt2 = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", instructions.indexOf(valTgt))
            				var valTgt2 = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, tgt, "put", "(ILjava/lang/Object;)Ljava/lang/Object;", instructions.indexOf(getTgt2));
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217465_m");
        						return false;
        					}
            				
            				if (getTgt2 == null || valTgt2 == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217465_m");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				valTgt.desc = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            				
        					var insn = asmapi.buildMethodCall("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", MethodType.STATIC);
        					var il = new InsnList();
                    		il.add(new InsnNode(opcodes.SWAP));
                        	il.add(insn);
                        	il.add(new InsnNode(opcodes.SWAP));
            				
            				instructions.insertBefore(valTgt, il);
            				
            				/*
            				var printTgt = getTgt.getPrevious().getPrevious();
    						for (var i = 0; i < 30; i++) {
    							printInsnNode(printTgt);
    							printTgt = printTgt.getNext();
    						}
    						print("------------")
    						*/
            				
            				getTgt2.desc = getTgt.desc.replace(tgt, replace);
            				valTgt2.owner = replace;
            				valTgt2.desc = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            				
            				il = new InsnList();
            				insn = asmapi.buildMethodCall("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", MethodType.STATIC);
                    		il.add(new InsnNode(opcodes.SWAP));
                        	il.add(insn);
                        	il.add(new InsnNode(opcodes.SWAP));
            					
        					instructions.insertBefore(valTgt2, il);
            				
        					/*
            				var printTgt = getTgt2.getPrevious().getPrevious();
    						for (var i = 0; i < 30; i++) {
    							printInsnNode(printTgt);
    							printTgt = printTgt.getNext();
    						}
    						print("------------")
    						*/
            					
            				return true;
            			}
            	}
            	
            	targetMethods[asmapi.mapMethod("func_72835_b")] = { // tick ;Same for 115 and 1161
            			"desc": "(Ljava/util/function/BooleanSupplier;)V",
            			"update": function(methodNode) {
            				
            				var instructions = methodNode.instructions;
            				
            				var getTgt = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", 0)
            				var valTgt = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, 
            						"it/unimi/dsi/fastutil/objects/ObjectSet", "iterator", 
            						"()Lit/unimi/dsi/fastutil/objects/ObjectIterator;", instructions.indexOf(getTgt));
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_72835_b");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				
        					var insn = asmapi.buildMethodCall("org/jmt/mcmt/asmdest/ObjectIteratorHack", "intMapItrFake", 
        							"(Ljava/util/Map;)Lit/unimi/dsi/fastutil/objects/ObjectIterator;", MethodType.STATIC);
        					var skipTarget = new LabelNode();
        					var il = new InsnList();
        					il.add(insn);
        					il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
        					
            				instructions.insert(getTgt, il);
            				instructions.insert(valTgt, skipTarget);
            					
            				return true;
            			}
            	}
            	
            	targetMethods[asmapi.mapMethod("func_225322_a")] = { // writeDebugInfo ;Same for 115 and 1161
            			"desc": "(Ljava/nio/file/Path;)V",
            			"update": function(methodNode) {
            				
            				var instructions = methodNode.instructions;
            				
            				var getTgt = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", 0)
            				var valTgt = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, tgt, "size", "()I", instructions.indexOf(getTgt));
            				
            				var getTgt2 = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", instructions.indexOf(valTgt))
            				var valTgt2 = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, tgt, "values", 
            						"()Lit/unimi/dsi/fastutil/objects/ObjectCollection;", instructions.indexOf(getTgt2));
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217465_m");
        						return false;
        					}
            				
            				if (getTgt2 == null || valTgt2 == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217465_m");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				valTgt.owner = replace;
            				//valTgt.desc = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            				
            				getTgt2.desc = getTgt.desc.replace(tgt, replace);
            				valTgt2.owner = replace;
            				valTgt2.desc = valTgt2.desc.replace(valuesTgt, valuesReplace);
            				
            				/*
            				var printTgt = getTgt.getPrevious().getPrevious();
    						for (var i = 0; i < 10; i++) {
    							printInsnNode(printTgt);
    							printTgt = printTgt.getNext();
    						}
    						print("------------")
    						*/
    						
    						var printTgt = getTgt2.getPrevious().getPrevious();
    						for (var i = 0; i < 10; i++) {
    							printInsnNode(printTgt);
    							printTgt = printTgt.getNext();
    						}
    						print("------------")
            					
            				return true;
            			}
            	}/**/
            	
            	targetMethods[asmapi.mapMethod("func_217450_l")] = { // countEntities ;1152 Only
            			"desc": "()Lit/unimi/dsi/fastutil/objects/Object2IntMap;",
            			"not1162": true,
            			"update": function(methodNode) {
            				
            				var instructions = methodNode.instructions;
            				
            				var getTgt = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
            						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", 0)
            				var valTgt = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, tgt, "values", 
            						"()Lit/unimi/dsi/fastutil/objects/ObjectCollection;", instructions.indexOf(getTgt));
            				var itrTgt = valTgt.getNext();
            				
            				if (getTgt == null || valTgt == null) {
        						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_72835_b");
        						return false;
        					}
            				
            				getTgt.desc = getTgt.desc.replace(tgt, replace);
            				
            				valTgt.owner = replace;
            				valTgt.desc = valTgt.desc.replace(valuesTgt, valuesReplace);
            				
        					var insn = asmapi.buildMethodCall("org/jmt/mcmt/asmdest/ObjectIteratorHack", "itrWrap", 
        							"(Ljava/lang/Iterable;)Lit/unimi/dsi/fastutil/objects/ObjectIterator;", MethodType.STATIC);
        					var skipTarget = new LabelNode();
        					var il = new InsnList();
        					il.add(insn);
        					il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
        					
            				instructions.insert(valTgt, il);
            				instructions.insert(itrTgt, skipTarget);
            					
            				return true;
            			}
            	}
            	
            	
            	var forgeSpecialGetEntitiesDesc = "()Ljava/util/stream/Stream;";
            	var forgeSpecialGetEntitiesSig = "()Ljava/util/stream/Stream<Lnet/minecraft/entity/Entity;>;"
            	var forgeSpecialGetEntitiesFun = function(methodNode) {
            		var getTgt = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, 0)
    				var valTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, tgt, "values", "()Lit/unimi/dsi/fastutil/objects/ObjectCollection;");
    				var itrTgt = asmapi.findFirstMethodCall(methodNode, MethodType.INTERFACE, valuesTgt, "stream", "()Ljava/util/stream/Stream;");
    				
    				if (getTgt == null || valTgt == null || itrTgt == null) {
						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_241136_z_");
						return false;
					}
    				
    				getTgt.desc = getTgt.desc.replace(tgt, replace);
    				valTgt.owner = replace;
    				valTgt.desc = valTgt.desc.replace(valuesTgt, valuesReplace);
    				itrTgt.owner = valuesReplace;
    				
    				return true;
            	};
            	
            	var forgeSpecialRemoveEntityName = "removeEntity"
            	var forgeSpecialRemoveEntityDesc = "(Lnet/minecraft/entity/Entity;Z)V"
            	var forgeSpecialRemoveEntityFun = function(methodNode) {
    				
    				var instructions = methodNode.instructions;
    				
    				var getTgt = getFieldAccessAfter(methodNode, opcodes.GETFIELD, "net/minecraft/world/server/ServerWorld",
    						tgtField, "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;", 0)
    				var valTgt = asmapi.findFirstMethodCallAfter(methodNode, MethodType.INTERFACE, tgt, "remove", "(I)Ljava/lang/Object;", instructions.indexOf(getTgt));
    				
    				if (getTgt == null || valTgt == null) {
						print("[JMTSUPERTRANS] MISSING TARGET INSN - func_217467_h");
						return false;
					}
    				
    				getTgt.desc = getTgt.desc.replace(tgt, replace);
    				valTgt.owner = replace;
    				valTgt.desc = "(Ljava/lang/Object;)Ljava/lang/Object;"
    				
					var insn = asmapi.buildMethodCall("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", MethodType.STATIC);
    				
    				instructions.insertBefore(valTgt, insn);
    					
    				return true;
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
            		else if (methodNode.desc == forgeSpecialGetEntitiesDesc
            				&& methodNode.signature == forgeSpecialGetEntitiesSig) {
            			print("[JMTSUPERTRANS] FOUND FORGE SPECIAL GETENTITIES!!!")
            			
            			print("[JMTSUPERTRANS] updating method: " + methodNode.name + methodNode.desc)
            			forgeSpecialGetEntitiesFun(methodNode);
            		} else if (methodNode.desc == forgeSpecialRemoveEntityDesc
            				&& methodNode.name == forgeSpecialRemoveEntityName) {
            			print("[JMTSUPERTRANS] FOUND FORGE SPECIAL REMOVEENTITY!!!")
            			
            			print("[JMTSUPERTRANS] updating method: " + methodNode.name + methodNode.desc)
            			forgeSpecialRemoveEntityFun(methodNode);
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