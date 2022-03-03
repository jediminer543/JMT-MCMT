function synchronizeMethod(debugLine) {
	return function(methodNode) {
		print("[JMTSUPERTRANS] " + debugLine + " Transformer Called");
		
		var opcodes = Java.type('org.objectweb.asm.Opcodes');
		
		methodNode.access += opcodes.ACC_SYNCHRONIZED;
		
		print("[JMTSUPERTRANS] " + debugLine + " Transformer Complete");
		
		return methodNode;
	}
}

function initializeCoreMod() {
    return {
    	
    }
}