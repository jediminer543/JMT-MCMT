package org.jmt.mcmt.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmt.mcmt.asmdest.ASMHookTerminator;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class ToggleCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> mcmtconfig = Commands.literal("mcmt");
		mcmtconfig = mcmtconfig.then(registerConfig(Commands.literal("config")));
		mcmtconfig = mcmtconfig.then(DebugCommands.registerDebug(Commands.literal("debug")));
		mcmtconfig = registerStatus(mcmtconfig);
		dispatcher.register(mcmtconfig);
	}

	public static LiteralArgumentBuilder<CommandSource> registerConfig(LiteralArgumentBuilder<CommandSource> root) {
		return root.then(Commands.literal("toggle").requires(cmdSrc -> {
			return cmdSrc.hasPermissionLevel(2);
		}).executes(cmdCtx -> {
			GeneralConfig.disabled = !GeneralConfig.disabled;
			StringTextComponent message = new StringTextComponent(
					"MCMT is now " + (GeneralConfig.disabled ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}).then(Commands.literal("te").executes(cmdCtx -> {
			GeneralConfig.disableTileEntity = !GeneralConfig.disableTileEntity;
			StringTextComponent message = new StringTextComponent("MCMT's tile entity threading is now "
					+ (GeneralConfig.disableTileEntity ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(Commands.literal("entity").executes(cmdCtx -> {
			GeneralConfig.disableEntity = !GeneralConfig.disableEntity;
			StringTextComponent message = new StringTextComponent(
					"MCMT's entity threading is now " + (GeneralConfig.disableEntity ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(Commands.literal("environment").executes(cmdCtx -> {
			GeneralConfig.disableEnvironment = !GeneralConfig.disableEnvironment;
			StringTextComponent message = new StringTextComponent("MCMT's environment threading is now "
					+ (GeneralConfig.disableEnvironment ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(Commands.literal("world").executes(cmdCtx -> {
			GeneralConfig.disableWorld = !GeneralConfig.disableWorld;
			StringTextComponent message = new StringTextComponent(
					"MCMT's world threading is now " + (GeneralConfig.disableWorld ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(Commands.literal("chunkprovider").executes(cmdCtx -> {
			GeneralConfig.disableChunkProvider = !GeneralConfig.disableChunkProvider;
			StringTextComponent message = new StringTextComponent(
					"MCMT's SCP threading is now " + (GeneralConfig.disableChunkProvider ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}))).then(Commands.literal("state").executes(cmdCtx -> {
			StringBuilder messageString = new StringBuilder(
					"MCMT is currently " + (GeneralConfig.disabled ? "disabled" : "enabled"));
			if (!GeneralConfig.disabled) {
				messageString.append(" World:" + (GeneralConfig.disableWorld ? "disabled" : "enabled"));
				messageString.append(" Entity:" + (GeneralConfig.disableEntity ? "disabled" : "enabled"));
				messageString.append(" TE:" + (GeneralConfig.disableTileEntity ? "disabled"
						: "enabled" + (GeneralConfig.chunkLockModded ? "(ChunkLocking Modded)" : "")));
				messageString.append(" Env:" + (GeneralConfig.disableEnvironment ? "disabled" : "enabled"));
				messageString.append(" SCP:" + (GeneralConfig.disableChunkProvider ? "disabled" : "enabled"));
			}
			StringTextComponent message = new StringTextComponent(messageString.toString());
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}))
		.then(Commands.literal("save").requires(cmdSrc -> {
			return cmdSrc.hasPermissionLevel(2);
		}).executes(cmdCtx -> {
			StringTextComponent message = new StringTextComponent("Saving MCMT config to disk...");
			cmdCtx.getSource().sendFeedback(message, true);
			GeneralConfig.saveConfig();
			message = new StringTextComponent("Done!");
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}));
	}

	public static LiteralArgumentBuilder<CommandSource> registerStatus(LiteralArgumentBuilder<CommandSource> root) {
		return root.then(Commands.literal("status").then(Commands.literal("reset").executes(cmdCtx -> {
			reset = true;
			return 1;
		})).executes(cmdCtx -> {
			StringBuilder messageString = new StringBuilder("Current max threads " + maxThreads + " (");
			messageString.append("World:" + maxWorlds);
			messageString.append(" Entity:" + maxEntities);
			messageString.append(" TE:" + maxTEs);
			messageString.append(" Env:" + maxEnvs + ")");
			StringTextComponent message = new StringTextComponent(messageString.toString());
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}));
	}

	static boolean reset = false;
	static int maxThreads = 0;
	static int maxWorlds = 0;
	static int maxTEs = 0;
	static int maxEntities = 0;
	static int maxEnvs = 0;
	static Thread countingThread;
	
	static int warnLog = 0;
	
	static Logger mtlog = LogManager.getLogger("MCMT Dev Warning");

	public static void runDataThread() {
		countingThread = new Thread(() -> {
			while (true) {
				try {
					while (true) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (reset) {
							maxWorlds = 0;
							maxTEs = 0;
							maxEntities = 0;
							maxEnvs = 0;
						}
						maxWorlds = Math.max(maxWorlds, ASMHookTerminator.currentWorlds.get());
						maxTEs = Math.max(maxTEs, ASMHookTerminator.currentTEs.get());
						maxEntities = Math.max(maxEntities, ASMHookTerminator.currentEnts.get());
						maxEnvs = Math.max(maxEnvs, ASMHookTerminator.currentEnvs.get());
						reset = false;
						maxThreads = maxWorlds + maxTEs + maxEntities + maxEnvs;
						
						warnLog++;
						if (warnLog % 15000 == 0) {
							mtlog.warn("MCMT is enabled; error logs are invalid for any other mods");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		countingThread.setDaemon(true);
		countingThread.start();
	}

}
