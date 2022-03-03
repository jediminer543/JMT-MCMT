module org.jmt.mcmt.modlauncher {
	requires cpw.mods.modlauncher;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	requires org.apache.logging.log4j;
	exports org.jmt.mcmt.modlauncher;
	
	provides cpw.mods.modlauncher.api.ITransformationService with org.jmt.mcmt.modlauncher.FastUtilTransformerService;
}