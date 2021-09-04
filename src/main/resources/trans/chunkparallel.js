function printInsnNode(printTgt) {
	print(printTgt+"|"+printTgt.opcode
			+"|"+printTgt.desc+"|"+printTgt.owner+"|"+printTgt.name+"|"+printTgt["var"])
}

function initializeCoreMod() {
    return {
        'chunkLoadCache': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.server.ServerChunkProvider'
                	
            },
            'transformer': function(classNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
            	var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
            	var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
            	var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
            	var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
				var AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
            	var MethodType = asmapi.MethodType;
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] ChunkLoadCache Transformer Called");

            	var methods = classNode.methods;
            	
            	var targetMethodName = asmapi.mapMethod("func_212849_a_"); 
            	var targetMethodDesc = "(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/IChunk;";
            	
            	for (var i in methods) {
            		var method = methods[i];
            		
            		if (!method.name.equals(targetMethodName)) {
            			continue;
            		} else if (!method.desc.equals(targetMethodDesc)) {
            			continue;
            		}
            		asmapi.log("DEBUG", "[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var instructions = method.instructions;
            		
            		var jumpStartMethod = asmapi.mapMethod("func_77272_a");
            		var jumpStartClass = "net/minecraft/util/math/ChunkPos";
            		var jumpStartDesc = "(II)J";
            		
            		var jumpStartTargetPre = asmapi.findFirstMethodCallAfter(method, MethodType.STATIC, 
            				jumpStartClass, jumpStartMethod, jumpStartDesc, 0);
            		
					// Should be LSTORE 6
					var jumpStartTarget = jumpStartTargetPre.getNext();
					//printInsnNode(jumpStartTargetPre);
					//printInsnNode(jumpStartTarget); 
					if (jumpStartTarget.getOpcode() != opcodes.LSTORE) {
						asmapi.log("ERROR", "[JMTSUPERTRANS] MISSING START TARGET INSN");
            			return classNode;
					}
					
            		var jumpEndTargetPre = jumpStartTarget;

					while  (jumpEndTargetPre != null) {
						if (jumpEndTargetPre.getType() == AbstractInsnNode.LDC_INSN) {
							if (jumpEndTargetPre.cst == "getChunkCacheMiss") {
								break;
							}
						}
						jumpEndTargetPre = jumpEndTargetPre.getNext();
					}
					
					if (jumpEndTargetPre == null) {
            			asmapi.log("ERROR", "[JMTSUPERTRANS] MISSING END PRE TARGET INSN");
            			return classNode;
            		}
					
					jumpEndTarget = jumpEndTargetPre.getPrevious();
            		
            		if (jumpEndTarget.getOpcode() != opcodes.ALOAD) {
            			asmapi.log("ERROR", "[JMTSUPERTRANS] MISSING END TARGET INSN");
            			return classNode;
            		}
            		
            		// Jump instruction
            		var skipTarget = new LabelNode();
            		            		
            		var il = new InsnList();
            		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
            				"org/jmt/mcmt/asmdest/ASMHookTerminator", "shouldThreadChunks",
            				"()Z" ,false));
            		il.add(new JumpInsnNode(opcodes.IFNE, skipTarget));
            		
            		instructions.insert(jumpStartTarget, il);
            		instructions.insertBefore(jumpEndTarget, skipTarget);
            		
            		asmapi.log("INFO", "[JMTSUPERTRANS] ChunkLoadCache Transformer Complete");
            		
            		break;
            	}
            	return classNode;
            }
        }
    }
}