//TODO

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