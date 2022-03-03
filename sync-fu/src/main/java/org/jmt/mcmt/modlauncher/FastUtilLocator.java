package org.jmt.mcmt.modlauncher;

import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

public class FastUtilLocator implements IModLocator {

	private static final Logger LOGGER = LogManager.getLogger();
	//private static final Marker M_LOCATOR = MarkerManager.getMarker("LOCATE");
	private boolean isActive = true;
	
	@Override
	public List<IModFile> scanMods() {
		LOGGER.info("Sync_Fu preparing...");
		LOGGER.info("Prepping fu_add...");
		Optional<URL> fujarurl = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).flatMap(path -> {
        	File file = new File(path);
        	if (file.isDirectory()) {
        		return Arrays.stream(file.list((d, n) -> n.endsWith(".jar")));
        	}
        	return Arrays.stream(new String[] {path});
        })
        .filter(p -> p.contains("fastutil")) // Can add more if necesary;
        .map(Paths::get)
        .map(path -> { 
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
		List<IModFile> imf = new ArrayList<IModFile>();
		try {
			Path path = Paths.get(rootURLf.toURI());
			Supplier<Manifest> manifestGen = () -> {
				Manifest man = new Manifest();
				return man;
			};
			Function<SecureJar, JarMetadata> modulehack = jar -> {
				JarMetadata jmb = JarMetadata.from(jar, path);
				JarMetadata out = new JarMetadata() {
					
					@Override
					public String version() {
						return jmb.version();
					}
					
					@Override
					public String name() {
						return jmb.name();
					}
					
					@Override
					public ModuleDescriptor descriptor() {
						ModuleDescriptor md = jmb.descriptor();
						ModuleDescriptor.Builder bld = ModuleDescriptor.newModule(md.name());
						md.version().ifPresent(bld::version);
						bld.packages(md.packages());
						md.packages().forEach(pkg -> bld.exports(Set.of(), pkg));
						md.packages().forEach(pkg -> bld.opens(Set.of(), pkg));
						//bld.opens(md.packages());
						return bld.build();
					}
				};
				return out;
			};
			SecureJar sj = SecureJar.from(manifestGen, modulehack, path);
			sj.getManifest().getMainAttributes().putValue("FMLModType", "GAMELIBRARY");
			ModFile mf = new ModFile(sj, this, FastUtilLocator::modfileinfoinator);
			System.out.println("SUPERTEST9001 TEST:" + mf.getType());
			imf.add(mf);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return imf;
	}
	
	private static IModFileInfo modfileinfoinator(IModFile idgaf) {
		IModFileInfo imfi = new IModFileInfo() {

			@Override
			public List<IModInfo> getMods() {
				List<IModInfo> mods = new ArrayList<>();
				return mods;
			}

			@Override
			public List<LanguageSpec> requiredLanguageLoaders() {
				List<LanguageSpec> mods = new ArrayList<>();
				return mods;
			}

			@Override
			public boolean showAsResourcePack() {
				return false;
			}

			@Override
			public Map<String, Object> getFileProperties() {
				return new HashMap<String, Object>();
			}

			@Override
			public String getLicense() {
				return "FAKE";
			}

			@Override
			public String moduleName() {
				return "???";
			}

			@Override
			public String versionString() {
				return "???";
			}

			@Override
			public List<String> usesServices() {
				List<String> mods = new ArrayList<>();
				return mods;
			}

			@Override
			public IModFile getFile() {
				return idgaf;
			}

			@Override
			public IConfigurable getConfig() {
				return new IConfigurable() {
					
					@Override
					public List<? extends IConfigurable> getConfigList(String... key) {
						List<? extends IConfigurable> mods = new ArrayList<>();
						return mods;
					}
					
					@Override
					public <T> Optional<T> getConfigElement(String... key) {
						return Optional.empty();
					}
				};
			};
		
		};
		return imfi;
	}
	
	@Override
	public String name() {
		return "sync-fu-locate";
	}
	@Override
	public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
		
	}
	@Override
	public void initArguments(Map<String, ?> arguments) {
		
	}
	@Override
	public boolean isValid(IModFile modFile) {
		return true;
	}
	
	
}
