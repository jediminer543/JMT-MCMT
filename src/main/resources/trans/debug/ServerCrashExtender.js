/*
function initializeCoreMod() {
    return {
         'ServerWatchdogExtender': {
            'target': {
            	'type': 'METHOD',
                'class': 'net.minecraft.server.dedicated.ServerHangWatchdog',
                "methodName": "run",
        		"methodDesc": "()V"
            },
            'transformer': function(methodNode) {
            	//net.minecraft.server.dedicated.ServerHangWatchdog.run()
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

				asmapi.log("INFO", "[JMTSUPERTRANS] ServerWatchdogExtender Transformer Called");

            	//INVOKEVIRTUAL net/minecraft/server/dedicated/DedicatedServer.addServerInfoToCrashReport(Lnet/minecraft/crash/CrashReport;)Lnet/minecraft/crash/CrashReport;
            	var targetMethodOwner = "net/minecraft/server/dedicated/DedicatedServer";
            	var targetMethodName = asmapi.mapMethod("func_71230_b"); 
            	var targetMethodDesc = "(Lnet/minecraft/crash/CrashReport;)Lnet/minecraft/crash/CrashReport;";
            	
            	var method = methodNode
            	        		
        		var instructions = methodNode.instructions;
        		
        		var callTarget = asmapi.findFirstMethodCallAfter(method, MethodType.VIRTUAL, 
        				targetMethodOwner, targetMethodName, targetMethodDesc, 0);
        		
        		if (callTarget != null) {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] FOUND TARGET INSNS");
        		} else {
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] MISSING TARGET INSNS:");
        			asmapi.log("DEBUG", "[JMTSUPERTRANS] HAVE CALL:" + (callTarget != null));
        			return;
        		}
        		
        		//Call Hook
        		
        		var il = new InsnList();
        		il.add(new MethodInsnNode(opcodes.INVOKESTATIC, 
        				"org/jmt/mcmt/asmdest/ASMHookTerminator", "populateCrashReport",
        				"(Lnet/minecraft/crash/CrashReport;)Lnet/minecraft/crash/CrashReport;"
        				,false));        		
        		instructions.insert(callTarget, il);

				asmapi.log("INFO", "[JMTSUPERTRANS] ServerWatchdogExtender Transformer Complete");
          
                return methodNode;
            }
        },
	}
}
*/