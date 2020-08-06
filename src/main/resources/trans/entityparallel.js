function initializeCoreMod() {
    return {
        'entityTick': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerWorld'
                	
            },
            'transformer': function(classNode) {
            	print("[JMTSUPERTRANS] Entity Transformer Called");
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;

            	var methods = classNode.methods;
            	
            	var targetMethodName = asmapi.mapMethod("func_217479_a"); 
            	var targetMethodDesc = "(Lnet/minecraft/entity/Entity;)V";
            	
            	for (var i in methods) {
            		var method = methods[i];
            		
            		
            		
            		if (!method.name.equals(targetMethodName)) {
            			continue;
            		} else if (!method.desc.equals(targetMethodDesc)) {
            			continue;
            		}
            		print("[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var instructions = method.instructions;
            		
            		var callMethod = asmapi.mapMethod("func_70071_h_");
            		var callClass = "net/minecraft/entity/Entity";
            		var callDesc = "()V";
            		
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
            				callClass, callMethod, callDesc, 0);
            		
            		if (callTarget == null) {
            			print("[JMTSUPERTRANS] MISSING TARGET INSN");
            			return;
            		}
            		
            		// Jump instruction
            		var skipTarget = new LabelNode();
            		            		
            		var il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0));
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "callEntityTick",
            				"(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/server/ServerWorld;)V" ,false));
            		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
            		
            		instructions.insertBefore(callTarget, il);
            		instructions.insert(callTarget, skipTarget);
            		
            		break;
            	}
            	return classNode;
            }
        }
    }
}