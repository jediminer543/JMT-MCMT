package org.jmt.mcmt.modlauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.spi.FileSystemProvider;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class FastUtilTransformerService implements ITransformer<ClassNode>, ITransformationService {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker M_LOCATOR = MarkerManager.getMarker("LOCATE");
	private boolean isActive = true;

	private static Object THE_UNSAFE;
	private static Method GET_REF_METHOD;
	private static Method GET_ADDR_METHOD;
	//private static Method PUT_BOOL_METHOD;

	static {
		try {
			Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
			Field theUnsafeField = unsafeType.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			THE_UNSAFE = theUnsafeField.get(null);
			System.out.println(Arrays.toString(unsafeType.getDeclaredMethods()));
			GET_REF_METHOD = unsafeType.getMethod("getObject", Object.class, long.class);
			GET_ADDR_METHOD = unsafeType.getMethod("objectFieldOffset", Field.class);
			//PUT_BOOL_METHOD = unsafeType.getMethod("putBoolean", Object.class, long.class, boolean.class);
		} catch (Throwable t) {
			//throw new ExceptionInInitializerError(t);
			t.printStackTrace();
			System.exit(9001);
		}
	}
	
	@Override
	public void argumentValues(OptionResult option) {
		String val = System.getProperty("JMT-LAUNCH-OVERIDE");
		if (val != null && "REGICIDE COMMITTED".equals(val)) {
			return;
		}
		try {
			// RESTART THE GAME!!!
			Launcher l = Launcher.INSTANCE;
			Field f = Launcher.class.getDeclaredField("argumentHandler");
			long addr = (Long)GET_ADDR_METHOD.invoke(THE_UNSAFE, f);
			ArgumentHandler ah = (ArgumentHandler)GET_REF_METHOD.invoke(THE_UNSAFE, l, addr);
			Field af = ArgumentHandler.class.getDeclaredField("args");
			long aaddr = (Long)GET_ADDR_METHOD.invoke(THE_UNSAFE, af);
			String[] args = (String[])GET_REF_METHOD.invoke(THE_UNSAFE, ah, aaddr);
			//System.out.println(Arrays.toString(ah.buildArgumentList()));
			Class<?> tgt = Class.forName("cpw.mods.bootstraplauncher.BootstrapLauncher");
			// Patch method
			//System.out.println(Arrays.toString(Class.class.getDeclaredMethods()));
			//System.out.println(Arrays.toString(Method.class.getSuperclass().getSuperclass().getDeclaredFields()));
			//Field of = Method.class.getSuperclass().getSuperclass().getDeclaredField("override");
			//long oaddr = (Long)GET_ADDR1_METHOD.invoke(THE_UNSAFE, Method.class.getSuperclass().getSuperclass(), "override");
			//PUT_BOOL_METHOD.invoke(THE_UNSAFE, met, oaddr, true);
			// Do Launch
			ClassLoader parentClassLoader = tgt.getClassLoader().getParent();
			class HackClassLoader extends ClassLoader {
				@Override
				public Class<?> loadClass(String name) throws ClassNotFoundException {
					if (name.startsWith("cpw.mods.bootstraplauncher.")) {
						String file = "/"+name.replace(".", "/").concat(".class");
						System.out.println(file);
						InputStream is = tgt.getResourceAsStream(file);
						byte[] clazz;
						try {
							clazz = is.readAllBytes();
							ClassReader cr = new ClassReader(clazz);
							ClassNode cn = new ClassNode();
							cr.accept(cn, 0);
							for (MethodNode mn : cn.methods) {
								if (mn.name.equals("main")) {
									AbstractInsnNode init = mn.instructions.getFirst();
									while (true) {
										if (init instanceof TypeInsnNode) {
											TypeInsnNode tin = (TypeInsnNode) init;
											if (tin.desc.equals("cpw/mods/cl/ModuleClassLoader")) {
												break;
											}
										}
										init = init.getNext();
										if (init == null) {
											throw new ClassNotFoundException(name);
										}
									}
									TypeInsnNode tin = (TypeInsnNode) init;
									tin.desc = "org/jmt/mcmt/modlauncher/CheatyModuleClassLoader";
									while (!((init instanceof MethodInsnNode) && ((MethodInsnNode)init).getOpcode()==Opcodes.INVOKESPECIAL)) {
										init = init.getNext();
									}
									MethodInsnNode min = ((MethodInsnNode)init);
									min.owner = "org/jmt/mcmt/modlauncher/CheatyModuleClassLoader";
									System.out.println("[JMTSUPERTRANS] BOOTLOADER BYPASS COMPLETE");
								}
							}
							ClassWriter cw = new ClassWriter(cr, 0);
							cn.accept(cw);
							byte[] patched = cw.toByteArray();
							return defineClass(name, patched, 0, patched.length);
						} catch (IOException e) {
							e.printStackTrace();
						}
						throw new ClassNotFoundException(name);
					} else if (name.equals("org.jmt.mcmt.modlauncher.CheatyModuleClassLoader")) {
						/*
						try {
							String file = "/"+name.replace(".", "/").concat(".class");
							InputStream is = FastUtilTransformerService.this.getClass().getClassLoader().getResourceAsStream(file);
							byte[] cd = is.readAllBytes();
							return defineClass(name, cd, 0, cd.length);
						} catch (Exception e) {
							throw new ClassNotFoundException(name);
						}
						*/
						try {
							return CheatyModuleClassLoader.class;
						} catch (Exception e) {
							throw new ClassNotFoundException(name);
						}
					} else if (name.equals("org.jmt.mcmt.modlauncher.FastUtilTransformerService")) {
						try {
							return FastUtilTransformerService.this.getClass();
						} catch (Exception e) {
							throw new ClassNotFoundException(name);
						}
					} else {
						return parentClassLoader.loadClass(name);
					}
				}
			}
			ClassLoader hcl = new HackClassLoader();
			Class<?> ntgt = hcl.loadClass("cpw.mods.bootstraplauncher.BootstrapLauncher");
			Method met = ntgt.getMethod("main", String[].class);
			// COMMIT
			System.setProperty("JMT-LAUNCH-OVERIDE", "REGICIDE COMMITTED");
			/*
			String prop = System.getProperty("ignoreList","asm,securejarhandler");
			prop += ",fastutil";
			System.setProperty("ignoreList", prop);
			System.out.println(System.getProperty("ignoreList","asm,securejarhandler"));
			*/
			try {
				met.invoke(null, (Object)args);
			} catch (java.lang.reflect.InvocationTargetException e) {
				//throw e.getCause();
				e.printStackTrace();
			}
			//Configuration bootstrapConfiguration = Launcher.class.getModule().getLayer().configuration();
			//Configuration rebootstrapConfiguration = 
			//ModuleClassLoader bootstrapReplacer = new ModuleClassLoader("RE-BOOTSTRAP", rebootstrapConfiguration, List.of(ModuleLayer.boot()));
			// BROKEN ON SERVERS 
			//System.exit(0);
			// Make thread sit dead
			//Thread.currentThread().setDaemon(true);
			Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {});
			throw new Error("KILLING TO AVOID REBOOT");
			//BootstrapLauncher.main();
		} catch (Exception t) {
			t.printStackTrace();
			Thread.currentThread().setUncaughtExceptionHandler((thr, e) -> {});
			throw new Error("KILLING TO AVOID REBOOT");
		}
	}
	
	@Override
	public String name() {
		return "sync_fu";
	}

	@Override
	public Entry<Set<String>, Supplier<Function<String, Optional<URL>>>> additionalClassesLocator() {
		LOGGER.info("Sync_Fu preparing...");
		LOGGER.info("Prepping fu_add...");
		Optional<URL> fujarurl = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
				.flatMap(path -> {
					File file = new File(path);
					if (file.isDirectory()) {
						return Arrays.stream(file.list((d, n) -> n.endsWith(".jar")));
					}
					return Arrays.stream(new String[] { path });
				}).filter(p -> p.contains("fastutil")) // Can add more if necesary;
				.map(Paths::get).map(path -> {
					try {
						return path.toUri().toURL();
					} catch (Exception e) {
						return null;
					}
				}).findFirst();
		URL rootUrl = null;
		try {
			rootUrl = fujarurl.get();
		} catch (Exception e) {
			LOGGER.warn("Failed to find FastUtil jar; this WILL result in more exceptions");
			isActive = false;
		}
		LOGGER.info("Sync_Fu found fu...");
		if (!isActive) {
			// We are dead
			return null;
		}
		final URL rootURLf = rootUrl;
		return new Entry<Set<String>, Supplier<Function<String, Optional<URL>>>>() {

			@Override
			public Set<String> getKey() {
				Set<String> out = new HashSet<String>();
				out.add("it.unimi.dsi.fastutil.");
				return out;
			}

			@Override
			public Supplier<Function<String, Optional<URL>>> getValue() {
				return () -> {
					return s -> {
						URL urlOut;
						try {
							urlOut = new URL("jar:" + rootURLf.toString() + "!/" + s);
							// LOGGER.debug(urlOut.toString());
							return Optional.of(urlOut);
						} catch (MalformedURLException e) {
							e.printStackTrace();
							return null;
						}
					};
				};
			}

			@Override
			public Supplier<Function<String, Optional<URL>>> setValue(Supplier<Function<String, Optional<URL>>> value) {
				throw new IllegalStateException();
			}

		};
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<ITransformer> transformers() {

		List<ITransformer> out = new ArrayList<>();
		out.add(this);
		// TODO add development testing
		// out.add(new DevModeEnabler());
		return out;
	}

	int posfilter = Opcodes.ACC_PUBLIC;
	int negfilter = Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT
			| Opcodes.ACC_ABSTRACT | Opcodes.ACC_BRIDGE;

	private static final Marker marker = MarkerManager.getMarker("JMTSUPERTRANS");

	@Override
	public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
		LOGGER.info(marker, "sync_fu " + input.name + " Transformer Called");
		if (!input.name.contains("$")) {
			for (MethodNode mn : input.methods) {
				if ((mn.access & posfilter) == posfilter && (mn.access & negfilter) == 0 && !mn.name.equals("<init>")) {
					mn.access |= Opcodes.ACC_SYNCHRONIZED;
					LOGGER.debug(marker, "Patching " + mn.name);
				}
			}
			LOGGER.info(marker, "sync_fu " + input.name + " Transformer Complete");
			for (InnerClassNode cn : input.innerClasses) {
				String iname = cn.name.replace("/", ".");
				if (!targets().stream().anyMatch(t->t.getClassName().equals(iname))) {
					LOGGER.warn(marker, "sync_fu: you are missing " +  iname + " this may bite you later");
				}
			}
			return input;
		} else {
			String parent = null;
			for (FieldNode fn : input.fields) {
				if (fn.name.equals("this$0")) {
					parent = fn.desc;
				}
			}
			if (parent == null) {
				LOGGER.error(marker, "Inner class faliure; parent not found ");
				return input;
			}
			LOGGER.info(marker, "sync_fu inner class of " + parent);
			for (MethodNode mn : input.methods) {
				InsnList start = new InsnList();
				InsnList end = new InsnList();
				if ((mn.access & posfilter) == posfilter && (mn.access & negfilter) == 0 && !mn.name.equals("<init>")) {
					start = new InsnList();
					start.add(new VarInsnNode(Opcodes.ALOAD, 0));
					start.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, "this$0", parent));
					start.add(new InsnNode(Opcodes.MONITORENTER));
					end = new InsnList();
					end.add(new VarInsnNode(Opcodes.ALOAD, 0));
					end.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, "this$0", parent));
					end.add(new InsnNode(Opcodes.MONITOREXIT));
					InsnList il = mn.instructions;
					AbstractInsnNode ain = il.getFirst();
					while (ain != null) {
						if (ain.getOpcode() == Opcodes.RETURN || ain.getOpcode() == Opcodes.ARETURN
								|| ain.getOpcode() == Opcodes.DRETURN || ain.getOpcode() == Opcodes.FRETURN
								|| ain.getOpcode() == Opcodes.IRETURN || ain.getOpcode() == Opcodes.LRETURN) {
							il.insertBefore(ain, end);
							end = new InsnList();
							end.add(new VarInsnNode(Opcodes.ALOAD, 0));
							end.add(new FieldInsnNode(Opcodes.GETFIELD, input.name, "this$0", parent));
							end.add(new InsnNode(Opcodes.MONITOREXIT));
						}
						ain = ain.getNext();
					}
					il.insertBefore(il.getFirst(), start);
				}
			}
			LOGGER.info(marker, "sync_fu " + input.name + " InnerClass Transformer Complete");
			return input;
		}
	}

	@Override
	public TransformerVoteResult castVote(ITransformerVotingContext context) {
		return TransformerVoteResult.YES;
	}

	@Override
	public Set<Target> targets() {
		Set<Target> out = new HashSet<ITransformer.Target>();
		if (!isActive) {
			// Is Dead
			return out;
		}
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap$ValueIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap$MapIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$ValueIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$KeySet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$KeyIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapEntrySet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$FastEntryIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$EntryIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapEntry"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ObjectMap$FastEntrySet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$ValueIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$KeySet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$KeyIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$MapEntrySet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$FastEntryIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$EntryIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$MapIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap$MapEntry"));
		//out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet"));
		//out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet"));
		//out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.LongArrayList"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.longs.LongAVLTreeSet"));
		// out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap$MapIterator"));
		// out.add(Target.targetClass("it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.shorts.ShortOpenHashSet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.objects.ObjectOpenHashSet"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.objects.ObjectOpenHashSet$SetIterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.objects.ObjectOpenHashSet$SetSpliterator"));
		out.add(Target.targetClass("it.unimi.dsi.fastutil.objects.ObjectCollections$SizeDecreasingSupplier"));
		File f = new File("config/jmt_mcmt-sync-fu-list.txt");
		if (f.exists()) {
			try (BufferedReader r = new BufferedReader(new FileReader(f))) {
				r.lines().filter(s -> !(s.startsWith("#") || s.startsWith("//") || s.equals("")))
						.map(s -> Target.targetClass(s)).forEach(t -> out.add(t));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			try {
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileWriter fw = new FileWriter(f);
				fw.write("// This file allows you to add targets to sync-fu\n"
						+ "// Lines starting with // or # are comments\n"
						+ "// This is done by specifying a class name\n" + "// As an example: \n"
						+ "//it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

	@Override
	public void initialize(IEnvironment environment) {

	}

	@Override
	public List<Resource> beginScanning(IEnvironment environment) {
		System.out.println("HAI1");
		/*
		 * try { Field f = FMLLoader.class.getDeclaredField("coreModProvider");
		 * f.setAccessible(true); ICoreModProvider icmp = (ICoreModProvider)
		 * f.get(null); f.set(null, new FakeCoreModProvider(icmp)); } catch (Exception
		 * e) { e.printStackTrace(); }
		 */
		try {
			CodeSource src = FastUtilTransformerService.class.getProtectionDomain().getCodeSource();
			URL jar = src.getLocation();
			//System.out.println("Path: " + jar.toString());
			/*
			if (!jar.toString().endsWith(".jar")) {
				LOGGER.warn(M_LOCATOR, "This be dev!!!");
				return List.of();
			}
			URI uri = new URI("jar:".concat(jar.toString()).concat("!/"));
			*/
			// Thanks SO https://stackoverflow.com/a/48298758
			for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
				if (provider.getScheme().equalsIgnoreCase("union")) {
					try {
						provider.getFileSystem(jar.toURI());
					} catch (FileSystemNotFoundException e) {
						// in this case we need to initialize it first:
						provider.newFileSystem(jar.toURI(), Collections.emptyMap());
					}
				}
			}
			Path myPath = Paths.get(jar.toURI());
			//System.out.println(myPath);
			Stream<Path> walk = Files.walk(myPath, 1).peek(p -> LOGGER.warn(M_LOCATOR, "Found {}", p))
					.filter(p -> p.toString().endsWith(".jar"));
			// Path root = FMLPaths.MODSDIR.get();
			Path root = Paths.get("mods");
			for (Iterator<Path> it = walk.iterator(); it.hasNext();) {
				Path file = it.next();
				LOGGER.info(M_LOCATOR, "Found target jar: {}", file);
				Files.copy(file, root.resolve(file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return List.of();
	}

	@Override
	public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
	}

}
