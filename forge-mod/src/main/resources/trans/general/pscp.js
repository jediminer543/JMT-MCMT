function initializeCoreMod() {
    return {    	
		'ServerWorldParaProvider': {
    		'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.server.level.ServerLevel'
            },
            "transformer": function(classNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
				asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldParaProvider Transformer Called");
            	
            	var tgtname = "<init>"
            		
        		var methods = classNode.methods;
            		
        		for (var i in methods) {
            		var method = methods[i];
            		
            		if (!method.name.equals(tgtname)) {
            			continue;
            		}
            		
            		asmapi.log("INFO", "[JMTSUPERTRANS] Matched method " + method.name + " " + method.desc);
            		
            		var callMethod = "<init>";
            		var callClass = "net/minecraft/server/level/ServerChunkCache";
            		//var callDesc = "(Lnet/minecraft/world/server/ServerWorld;Ljava/io/File;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/gen/feature/template/TemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/gen/ChunkGenerator;ILnet/minecraft/world/chunk/listener/IChunkStatusListener;Ljava/util/function/Supplier;)V";
            	
					var callTarget = method.instructions.getFirst();
					
					while (callTarget != null) {
						if (callTarget.opcode == opcodes.INVOKESPECIAL) {
							if (callTarget.name == callMethod && callTarget.owner == callClass) {
								break;
							}
						}
						callTarget = callTarget.getNext();
					}
					
            		if (callTarget == null) {
            			print("[JMTSUPERTRANS] MISSING TARGET INSN");
            			return;
            		}
            		
            		var tgtMethod = callMethod;
            		var tgtClass = "org/jmt/mcmt/paralelised/ParaServerChunkProvider";
            		
            		var newTgt = callTarget;
					
					while (newTgt.getOpcode() != opcodes.NEW) {
						newTgt = newTgt.getPrevious();
					}
            		
            		callTarget.owner = tgtClass;
            		callTarget.name = tgtMethod;
            		
            		newTgt.desc = tgtClass;
        		}
            		
        		asmapi.log("INFO", "[JMTSUPERTRANS] ServerWorldParaProvider Transformer Complete");
            	
            	return classNode;
            }
    	},
	}
}