function initializeCoreMod() {
    return {
        'teTick': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.World'
            },
            'transformer': function(classNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
            	var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] TE Transformer Called");

            	var methods = classNode.methods;
            	
            	var targetMethodName = asmapi.mapMethod("func_217391_K"); 
            	var targetMethodDesc = "()V";
            	
            	for (var i in methods) {
            		var method = methods[i];
            		
            		
            		
            		if (!method.name.equals(targetMethodName)) {
            			continue;
            		} else if (!method.desc.equals(targetMethodDesc)) {
            			continue;
            		}
            		asmapi.log("DEBUG", "[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var instructions = method.instructions;
            		
            		var callMethod = asmapi.mapMethod("func_73660_a");
            		var callClass = "net/minecraft/tileentity/ITickableTileEntity";
            		var callDesc = "()V";
            		
            		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.INTERFACE, 
            				callClass, callMethod, callDesc, 0);
            		
            		if (callTarget == null) {
            			asmapi.log("ERROR", "[JMTSUPERTRANS] MISSING TARGET INSN");
            			return classNode;
            		}
            		
            		// Jump instruction
            		var skipTarget = new LabelNode();
            		            		
            		var il = new InsnList();
            		il.add(new VarInsnNode(opcodes.ALOAD, 0));
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "callTileEntityTick",
            				"(Lnet/minecraft/tileentity/ITickableTileEntity;Lnet/minecraft/world/World;)V" ,false));
            		il.add(new JumpInsnNode(opcodes.GOTO, skipTarget));
            		
            		instructions.insertBefore(callTarget, il);
            		instructions.insert(callTarget, skipTarget);
            		
            		break;
            	}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] TE Transformer Complete");
            	
            	return classNode;
            }
        }
    }
}