function initializeCoreMod() {
    return {
    	'classInheritanceMultiMapList': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.util.ClassInheritanceMultiMap',
                "methodName": "<init>",
        		"methodDesc": "(Ljava/lang/Class;)V"
            },
            "transformer": function(methodNode) {         	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var instructions = methodNode.instructions;

				asmapi.log("INFO", "[JMTSUPERTRANS] MultiMapList Transformer Called");
            	            	
            	var callMethod = "newArrayList";
        		var callClass = "com/google/common/collect/Lists";
        		var callDesc = "()Ljava/util/ArrayList;";
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(methodNode, MethodType.STATIC, 
        				callClass, callMethod, callDesc, 0);
        		            	
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
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] MultiMapList Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'ClassInheritanceMultiMapMap': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.util.ClassInheritanceMultiMap',
                "methodName": "<init>",
        		"methodDesc": "(Ljava/lang/Class;)V"
            },
            "transformer": function(methodNode) {           	
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var instructions = methodNode.instructions;

				asmapi.log("INFO", "[JMTSUPERTRANS] ClassInheritanceMultiMapMap Transformer Called");
            	
            	//com/google/common/collect/Maps.newHashMap()Ljava/util/HashMap;
            	var callMethod = "newHashMap";
        		var callClass = "com/google/common/collect/Maps";
        		var callDesc = "()Ljava/util/HashMap;";
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(methodNode, MethodType.STATIC, 
        				callClass, callMethod, callDesc, 0);
        		            	
            	var il = new InsnList();
            	// Purge old arrayList
            	il.add(new InsnNode(opcodes.POP));
            	/*
            	mv.visitTypeInsn(NEW, "java/util/concurrent/CopyOnWriteArrayList");
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/CopyOnWriteArrayList", "<init>", "()V", false);
            	 */
            	il.add(new TypeInsnNode(opcodes.NEW, "java/util/concurrent/ConcurrentHashMap"));
            	il.add(new InsnNode(opcodes.DUP));
            	il.add(new MethodInsnNode(opcodes.INVOKESPECIAL, 
            			"java/util/concurrent/ConcurrentHashMap", "<init>", "()V", false));
            	instructions.insert(callTarget, il);
            	
            	print("[JMTSUPERTRANS] ClassInheritanceMultiMapMap Transformer Complete");
            	
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
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	var instructions = methodNode.instructions;

				asmapi.log("INFO", "[JMTSUPERTRANS] ServerTickListSR Transformer Called");
            	
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
            	
        		asmapi.log("INFO", "[JMTSUPERTRANS] ServerTickListSR Transformer Complete");
        		
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
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] WorldGetTE Transformer Called");
            	
            	
            	var instructions = methodNode.instructions;
            	
            	//methodNode.access += opcodes.ACC_SYNCHRONIZED;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	if (target == null) {
            		asmapi.log("FATAL", "[JMTSUPERTRANS] WorldGetTE Transformer FAILED; this may be craftbukkit's doing");
            		asmapi.log("FATAL", "If you are not running CB or equiv, please start panicing");
            		return methodNode;
            	}
            	var target = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, instructions.indexOf(target)+1);
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] WorldGetTE Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'WorldGetTECraftBukkit': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.World',
                "methodName": "getTileEntity",
        		"methodDesc": "(Lnet/minecraft/util/math/BlockPos;Z)Lnet/minecraft/tileentity/TileEntity;"
            },
            "transformer": function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] WorldGetTECraftBukkit Transformer Called");
            	
            	
            	var instructions = methodNode.instructions;
            	
            	//methodNode.access += opcodes.ACC_SYNCHRONIZED;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.GETFIELD);
            	if (target != null) {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] Debug: " + target.descriptor);
        		} else {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] Debug: NullOP");
        		}
            	while (target != null && target.desc != "Ljava/lang/Thread;") {
            		target = asmapi.findFirstInstructionAfter(methodNode, opcodes.GETFIELD, instructions.indexOf(target)+1);
            		if (target != null) {
            			asmapi.log("DEBUG", "[JMTSUPERTRANS] Debug: " + target.descriptor);
            		} else {
            			asmapi.log("DEBUG", "[JMTSUPERTRANS] Debug: NullOP");
            		}
            	}
            	if (target == null || target.desc != "Ljava/lang/Thread;") {
            		asmapi.log("FATAL", "[JMTSUPERTRANS] You are running an unsupported version of craftbukkit, please don't");
            		if (target != null) {
            			asmapi.log("DEBUG", "[JMTSUPERTRANS] Debug: " + target.descriptor);
            		} else {
            			asmapi.log("DEBUG", "[JMTSUPERTRANS] Debug: NullOP");
            		}
            		return methodNode;
            	}
            	
            	var il = new InsnList();
            	il.add(new InsnNode(opcodes.POP));
            	il.add(new InsnNode(opcodes.DUP));
            	instructions.insert(target, il);
            	
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] WorldGetTECraftBukkit Transformer Complete");
            	
            	return methodNode;
            }
    	},
    	'ClassInheritanceMultiMapGBC': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.util.ClassInheritanceMultiMap'
            },
            "transformer": function(classNode) {
            	
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var MethodType = asmapi.MethodType;

				asmapi.log("INFO", "[JMTSUPERTRANS] ClassInheritanceMultiMapGBC Transformer Called");
            	
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
            		
            	asmapi.log("INFO", "[JMTSUPERTRANS] ClassInheritanceMultiMapGBC Transformer Complete");
            	
            	return classNode;
            }
    	},
    }
}