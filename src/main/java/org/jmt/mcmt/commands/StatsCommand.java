package org.jmt.mcmt.commands;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmt.mcmt.asmdest.ASMHookTerminator;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;

public class StatsCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> registerStatus(LiteralArgumentBuilder<CommandSourceStack> root) {
		return root.then(Commands.literal("stats").then(Commands.literal("reset").executes(cmdCtx -> {
			resetAll();
			return 1;
		})).executes(cmdCtx -> {
			if (!threadStats) {
				TextComponent message = new TextComponent("Stat calcs are disabled so stats are out of date"); 
				cmdCtx.getSource().sendSuccess(message, true);
			}
			StringBuilder messageString = new StringBuilder(
					"Current max threads " + mean(maxThreads, liveValues) + " (");
			messageString.append("World:" + mean(maxWorlds, liveValues));
			messageString.append(" Entity:" + mean(maxEntities, liveValues));
			messageString.append(" TE:" + mean(maxTEs, liveValues));
			messageString.append(" Env:" + mean(maxEnvs, liveValues) + ")");
			TextComponent message = new TextComponent(messageString.toString());
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		}).then(Commands.literal("toggle").requires(cmdSrc -> {
			return cmdSrc.hasPermission(2);
		}).executes(cmdCtx -> {
			threadStats = !threadStats;
			TextComponent message = new TextComponent("Stat calcs are " + 
					(!threadStats ? "disabled" : "enabled") + "!");
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		})).then(Commands.literal("startlog").requires(cmdSrc -> {
			return cmdSrc.hasPermission(2);
		}).executes(cmdCtx -> {
			doLogging = true;
			TextComponent message = new TextComponent("Logging started!");
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		})).then(Commands.literal("stoplog").requires(cmdSrc -> {
			return cmdSrc.hasPermission(2);
		}).executes(cmdCtx -> {
			TextComponent message = new TextComponent("Logging stopping...");
			cmdCtx.getSource().sendSuccess(message, true);
			doLogging = false;
			return 1;
		})));
	}

	public static float mean(int[] data, int max) {
		float total = 0;
		for (int i = 0; i < max; i++) {
			total += data[i];
		}
		total /= max;
		return total;
	}
	
	static MinecraftServer mcs;
	
	public static void setServer(MinecraftServer nmcs) {
		mcs = nmcs;
	}

	static boolean resetThreadStats = false;
	static boolean threadStats = false;
	static boolean doLogging = false;

	// Thread Stats
	static final int samples = 100;
	static final int stepsPer = 35;
	static int maxThreads[] = new int[samples];
	static int maxWorlds[] = new int[samples];
	static int maxTEs[] = new int[samples];
	static int maxEntities[] = new int[samples];
	static int maxEnvs[] = new int[samples];
	static int currentSteps = 0;
	static int currentPos = 0;
	static int liveValues = 0;
	
	// Logging
	static FileWriter logFile;

	// Stuff
	static Thread statsThread;

	static int warnLog = 0;

	static Logger mtlog = LogManager.getLogger("MCMT Dev Warning");
	
	static final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss.SSS");

	public static void resetAll() {
		resetThreadStats = true;
	}
	
	static int warningDelay = 15000;
	
	public static void resetWarnDelay() {
		warningDelay = 15000;
	}

	public static void runDataThread() {
		statsThread = new Thread(() -> {
			while (true) {
				try {
					while (true) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (threadStats || doLogging) {
							if (resetThreadStats) {
								maxThreads = new int[samples];
								maxWorlds = new int[samples];
								maxTEs = new int[samples];
								maxEntities = new int[samples];
								maxEnvs = new int[samples];
								currentSteps = 0;
								currentPos = 0;
								liveValues = 0;
								resetThreadStats = false;
							}
							if (++currentSteps % stepsPer == 0) {
								currentPos++;
								currentPos = currentPos % samples;
								liveValues = Math.min(liveValues + 1, samples);
								maxWorlds[currentPos] = 0;
								maxTEs[currentPos] = 0;
								maxEntities[currentPos] = 0;
								maxEnvs[currentPos] = 0;
								maxThreads[currentPos] = 0;
							}
							int total = 0;
							int worlds = ASMHookTerminator.currentWorlds.get();
							maxWorlds[currentPos] = Math.max(maxWorlds[currentPos],
									worlds);
							int tes = ASMHookTerminator.currentTEs.get();
							maxTEs[currentPos] = Math.max(maxTEs[currentPos], tes);
							int entities = ASMHookTerminator.currentEnts.get();
							maxEntities[currentPos] = Math.max(maxEntities[currentPos],
									entities);
							int envs = ASMHookTerminator.currentEnvs.get();
							maxEnvs[currentPos] = Math.max(maxEnvs[currentPos], envs);
							total = worlds+tes+entities+envs;
							maxThreads[currentPos] = Math.max(maxThreads[currentPos], total);
									
						}
						if (mcs != null && !mcs.isRunning()) {
							doLogging = false;
						}
						if (doLogging) {
							if (logFile == null) {
								logFile = new FileWriter("mcmt-stats-log-"+sdf.format(new Date()) +".csv");
								logFile.write("TickTime,Memory,AvgMCMPThreads,Enabled,\n");
							}
							if (currentSteps % stepsPer == 0) {
								float ticktime = -1;
								if (mcs != null && mcs.isRunning()) {
									ticktime = mcs.getAverageTickTime();
								}
								float threadCount = mean(maxThreads, liveValues);
								long memory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
								int enabled = 0; 
								enabled |= GeneralConfig.disabled ? 1 : 0;
								enabled |= GeneralConfig.disableWorld ? 2 : 0;
								enabled |= GeneralConfig.disableEntity ? 4 : 0;
								enabled |= GeneralConfig.disableEnvironment ? 8 : 0;
								enabled |= GeneralConfig.disableChunkProvider ? 16 : 0;
								enabled |= GeneralConfig.chunkLockModded ? 256 : 0;
								logFile.write(ticktime+","+memory+","+threadCount+","+enabled+",\n");
							}
						} else if (logFile != null) {
							logFile.flush();
							logFile.close();
							logFile = null;
						}

						warnLog++;
						if (warnLog % warningDelay == 0) {
							mtlog.warn("MCMT is installed; error logs are likely invalid for any other mods");
							warningDelay *= 1.2;
							warningDelay = Math.min(warningDelay, Math.max(GeneralConfig.logcap, 15000)); // Max delay ~~ 2 hours
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		statsThread.setDaemon(true);
		statsThread.start();
		statsThread.setName("MCMT Stats Thread");
	}
}
