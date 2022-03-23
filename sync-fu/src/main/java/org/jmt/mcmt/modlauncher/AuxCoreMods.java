package org.jmt.mcmt.modlauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.minecraftforge.coremod.CoreModProvider;
import net.minecraftforge.forgespi.coremod.ICoreModFile;

/**
 * 
 * Adds support for writing your own auxiliary coremods and then putting them in the config folder
 * 
 * Probably not secure but this is minecraft so is anything secure?
 * 
 * @author jediminer543
 *
 */
public class AuxCoreMods implements ITransformationService {

	@Override
	public @NotNull String name() {
		return "jmt-auxcoremods";
	}

	CoreModProvider cme;

	static class JMTFakeCoreMod implements ICoreModFile {
		
		Path coremod;
		List<Path> additionalFiles;
		
		public JMTFakeCoreMod(Path coremod) {
			this(coremod, List.of());
		}
		
		public JMTFakeCoreMod(Path coremod, List<Path> additionalFiles) {
			this.coremod = coremod;
			this.additionalFiles = additionalFiles;
		}
		
		@Override
		public String getOwnerId() {
			return "jmt-auxcoremods";
		}

		@Override
		public Reader readCoreMod() throws IOException {
			return new FileReader(coremod.toFile());
		}

		@Override
		public Path getPath() {
			return coremod;
		}

		@Override
		public Reader getAdditionalFile(String fileName) throws IOException {
			Optional<Path> path = additionalFiles.stream().filter(p -> p.endsWith(fileName)).findFirst();
			if (path.isPresent()) {
				return new FileReader(path.get().toFile());
			} else {
				return null;
			}
		}
		
	}
	
	
	@Override
	public void initialize(IEnvironment environment) {
		cme = new CoreModProvider();
		List<String> coremodFiles = new ArrayList<String>();
		File f = new File("config/jmt_mcmt-aux-coremods-list.txt");
		if (f.exists()) {
			try (BufferedReader r = new BufferedReader(new FileReader(f))) {
				r.lines().filter(s -> !(s.startsWith("#") || s.startsWith("//") || s.equals("")))
						.forEach(t -> coremodFiles.add(t));
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
				fw.write("// This file allows you to external coremodsto be run\n"
						+ "// Lines starting with // or # are comments\n"
						+ "// This is done by specifying a file path\n"
						+ "// Additional files may be provided with brackets at the end"
						+ "// As an example: \n"
						+ "//config/jmt-auxcore/example/example.js(config/jmt-auxcore/example/example_lib.js,config/jmt-auxcore/example/example_lib2.js)\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (String s : coremodFiles) {
			String file = s.split("(")[0];
			String[] additionalFiles = new String[0];
			if (s.split("(").length > 1) {
				additionalFiles = s.split("(")[1].split(",");
			}
			cme.addCoreMod(new JMTFakeCoreMod(Path.of(file), Arrays.stream(additionalFiles).map(st->Path.of(st)).toList()));
		}
	}

	@Override
	public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public @NotNull List<ITransformer> transformers() {
		List<ITransformer> out = new ArrayList<ITransformer>();
		out.addAll(cme.getCoreModTransformers());
		return out;
	}

}
