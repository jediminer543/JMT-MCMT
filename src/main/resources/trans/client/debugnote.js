function initializeCoreMod() {
    return {
        'entityTick': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.overlay.DebugOverlayGui',
                "methodName": "func_209011_c",
        		"methodDesc": "()Ljava/util/List;"
            },
            "transformer": function(methodNode) {
            	var opcodes = Java.type('org.objectweb.asm.Opcodes');
            	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');

				asmapi.log("INFO", "[JMTSUPERTRANS] DebugScreenPatch Transformer Called");
            	
            	var tgtStr = "Integrated server";
            	var newStr = "Integrated multithreaded server";
            	
            	var instructions = methodNode.instructions;
            	
            	var target = asmapi.findFirstInstruction(methodNode, opcodes.LDC);
            	
            	if (target != null) {
            		target.cst = target.cst.replace(tgtStr, newStr)
            	}
            	
            	asmapi.log("INFO", "[JMTSUPERTRANS] DebugScreenPatch Transformer Complete");
            	
            	return methodNode;
            }
        }
    }
}
