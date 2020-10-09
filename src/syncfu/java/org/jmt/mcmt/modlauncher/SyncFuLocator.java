package org.jmt.mcmt.modlauncher;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;

/**
 * Is a full spec'd ModLocater
 * 
 * Unusable for it's intended purpose as it can't be registered as this jar uses the wrong classloader
 * 
 * @author jediminer543
 * 
 * @since 0.18.60
 *
 */
public class SyncFuLocator extends AbstractJarFileLocator {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Marker M_LOCATOR = MarkerManager.getMarker("LOCATE");

	@Override
	public List<IModFile> scanMods() {
		LOGGER.info(M_LOCATOR, "Prepping locator...");
		List<IModFile> out = new ArrayList<>();
		try {
			CodeSource src = SyncFuLocator.class.getProtectionDomain().getCodeSource();
			URL jar = src.getLocation();
			if (!jar.toString().endsWith(".jar")) {
				LOGGER.warn(M_LOCATOR, "This be dev!!!");
				return out;
			}
			URI uri = new URI("jar:".concat(jar.toString()).concat("!/"));
			//Thanks SO https://stackoverflow.com/a/48298758
			for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
		        if (provider.getScheme().equalsIgnoreCase("jar")) {
		            try {
		                provider.getFileSystem(uri);
		            } catch (FileSystemNotFoundException e) {
		                // in this case we need to initialize it first:
		                provider.newFileSystem(uri, Collections.emptyMap());
		            }
		        }
		    }
	        Path myPath = Paths.get(uri);
	        System.out.println(myPath);
	        Stream<Path> walk = Files.walk(myPath, 1).peek(p -> LOGGER.warn(M_LOCATOR, "Found {}", p)).filter(p -> p.toString().endsWith(".jar"));
	        for (Iterator<Path> it = walk.iterator(); it.hasNext();){
	        	Path file = it.next();
	        	LOGGER.info(M_LOCATOR, "Found target jar: {}", file);
	            out.add(ModFile.newFMLInstance(file, this));
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info(M_LOCATOR, "Locator complete!!!");
		return out;
	}

	@Override
	public String name() {
		return "sync_fu_bundle";
	}

	@Override
	public void initArguments(Map<String, ?> arguments) {}
	
}
