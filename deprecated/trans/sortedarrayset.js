// Was used to poke sorted arrayset; shouldn't be needed
return {
	'SortedArraySetAdd': {
        'target': {
            'type': 'METHOD',
            'class': 'net.minecraft.util.SortedArraySet',
            "methodName": "add",
    		"methodDesc": "(Ljava/lang/Object;)Z"
        },
        "transformer": synchronizeMethod("SortedArraySetAdd")
	},
	'SortedArraySetClear': {
        'target': {
            'type': 'METHOD',
            'class': 'net.minecraft.util.SortedArraySet',
            "methodName": "clear",
    		"methodDesc": "()V"
        },
        "transformer": synchronizeMethod("SortedArraySetClear")
	},
	'SortedArraySetContains': {
        'target': {
            'type': 'METHOD',
            'class': 'net.minecraft.util.SortedArraySet',
            "methodName": "contains",
    		"methodDesc": "(Ljava/lang/Object;)Z"
        },
        "transformer": synchronizeMethod("SortedArraySetContains")
	},
	'SortedArraySet_func_226175_a_': {
        'target': {
            'type': 'METHOD',
            'class': 'net.minecraft.util.SortedArraySet',
            "methodName": "func_226175_a_",
    		"methodDesc": "(Ljava/lang/Object;)Ljava/lang/Object;"
        },
        "transformer": synchronizeMethod("SortedArraySet_func_226175_a_")
	},
	'SortedArraySetNewSet': {
		'target': {
            'type': 'METHOD',
            'class': 'net.minecraft.util.SortedArraySet',
            'methodName': "func_226172_a_",
            'methodDesc': "(I)Lnet/minecraft/util/SortedArraySet"
        },
        "transformer": function(methodNode) {
        	print("[JMTSUPERTRANS] SortedArraySetNewSet Transformer Called");
        	
        	//java.util.Comparator.nullsFirst(Comparator<? super T>)
        	var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        	var MethodType = asmapi.MethodType;
        	
        	var callTarget = asmapi.findFirstMethodCallAfter(methodNode, MethodType.STATIC, 
        			"java/util/Comparator", "naturalOrder", "()Ljava/util/Comparator;", 0);
        	
        	var insn = asmapi.buildMethodCall("java/util/Comparator", "nullsLast", "(Ljava/util/Comparator;)Ljava/util/Comparator;", MethodType.STATIC);
        	instructions.insert(callTarget, insn);
        	
        	print("[JMTSUPERTRANS] SortedArraySetNewSet Transformer Complete");
        	
        	return methodNode
        }
	}
}