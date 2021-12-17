module org.jmt.mcmt.modlauncher {
	requires cpw.mods.modlauncher;
	requires org.apache.logging.log4j;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	exports org.jmt.mcmt.modlauncher;
	
	provides cpw.mods.modlauncher.api.ITransformationService with org.jmt.mcmt.modlauncher.FastUtilTransformerService;
}