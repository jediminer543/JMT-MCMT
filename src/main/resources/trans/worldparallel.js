function initializeCoreMod() {
    return {
        'mcserver': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.MinecraftServer'
                	
            },
            'transformer': function(classNode) {
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
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] World Transformer Called");

            	var methods = classNode.methods;
            	
            	var targetMethodName = asmapi.mapMethod("func_71190_q"); 
            	var targetMethodDesc = "(Ljava/util/function/BooleanSupplier;)V";
            	
            	for (var i in methods) {
            		var method = methods[i];
            		
            		
            		
            		if (!method.name.equals(targetMethodName)) {
            			continue;
            		} else if (!method.desc.equals(targetMethodDesc)) {
            			continue;
            		}
            		asmapi.log("DEBUG", "[JMTSUPERTRANS]Matched method " + method.name + " " + method.desc);
            		
            		var instructions = method.instructions;
            		
            		var preTarget = null;
            		var postTarget = null;
            		
            		var arrayLength = instructions.size();
            		for (var i = 0; i < arrayLength; ++i) {
            			var instruction = instructions.get(i);
            			if (instruction instanceof LdcInsnNode) {
            				if (instruction.cst.equals("levels")) {
            					// We have 1 before our pre-insertion point
            					preTarget = instruction;
            					preTarget = preTarget.getNext();
            				} else if (instruction.cst.equals("dim_unloading")) {
            					// we are 3 after our insertion point
            					postTarget = instruction;
            					postTarget = postTarget.getPrevious();
            					postTarget = postTarget.getPrevious();
            					postTarget = postTarget.getPrevious();
            				} else if (instruction.cst.equals("connection") && postTarget == null) {
            					asmapi.log("INFO", "YOU ARE USING 1.16 - Says coremods (if this is wrong something borked)")
            					postTarget = instruction;
            					postTarget = postTarget.getPrevious();
            					postTarget = postTarget.getPrevious();
            					//postTarget = postTarget.getPrevious();
            				}
            			}
            		}

            		
            		var callMethod = asmapi.mapMethod("func_72835_b");
            		var callClass = "net/minecraft/world/server/ServerWorld";
            		
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
            				callClass, callMethod, "(Ljava/util/function/BooleanSupplier;)V", 0);
            		
            		
            		if (callTarget != null && preTarget != null && postTarget != null) {
            			asmapi.log("INFO", "[JMTSUPERTRANS] FOUND TARGET INSNS");
            		} else {
            			asmapi.log("ERROR", "[JMTSUPERTRANS] MISSING TARGET INSNS:");
            			asmapi.log("ERROR", "[JMTSUPERTRANS] HAVE PRE:" + (preTarget != null));
            			asmapi.log("ERROR", "[JMTSUPERTRANS] HAVE POST:" + (postTarget != null));
            			asmapi.log("ERROR", "[JMTSUPERTRANS] HAVE CALL:" + (callTarget != null));
            			return classNode;
            		}
            		
            		//Call Hook
            		var skipTarget = new LabelNode();
            		
            		var il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0));
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "callTick",
            				"(Lnet/minecraft/world/server/ServerWorld;Ljava/util/function/BooleanSupplier;Lnet/minecraft/server/MinecraftServer;)V" ,false));
            		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
            		
            		instructions.insertBefore(callTarget, il);
            		instructions.insert(callTarget, skipTarget);
            		
            		//Pre Hook
            		il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0));
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "preTick",
            				"(Lnet/minecraft/server/MinecraftServer;)V" ,false));
            		instructions.insert(preTarget, il);
            		
            		//Post Hook
            		il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0));
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "postTick",
            				"(Lnet/minecraft/server/MinecraftServer;)V" ,false));
            		instructions.insert(postTarget, il);
            		
            		// FORGE BUS HACKS
            		
            		var skipTarget2 = new LabelNode();
            		var toSkip = asmapi.findFirstMethodCallAfter(method, MethodType.STATIC, 
            				"net/minecraftforge/fml/hooks/BasicEventHooks", "onPostWorldTick", "(Lnet/minecraft/world/World;)V", 0);
            		
            		il = new InsnList();
            		il.add(new InsnNode(opcodes.POP));
            		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget2));
            		instructions.insertBefore(toSkip, il);
            		instructions.insert(toSkip, skipTarget2);
            		
            		// Break because this particular coremod only targets one method
            		break;
            	}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] World Transformer Complete");
            	
                return classNode;
            }
        },
    }
}
