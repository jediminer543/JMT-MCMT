package org.jmt.mcmt.commands;

import org.jmt.mcmt.asmdest.ASMHookTerminator;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;

public class ConfigCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> mcmtconfig = Commands.literal("mcmt");
		mcmtconfig = mcmtconfig.then(registerConfig(Commands.literal("config")));
		mcmtconfig = mcmtconfig.then(DebugCommands.registerDebug(Commands.literal("debug")));
		mcmtconfig = mcmtconfig.then(PerfCommand.registerPerf(Commands.literal("perf")));
		mcmtconfig = StatsCommand.registerStatus(mcmtconfig);
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
		}))
		.then(Commands.literal("temanage").requires(cmdSrc -> {
				return cmdSrc.hasPermissionLevel(2);
			})
			.then(Commands.literal("list")
				.executes(cmdCtx -> {
					StringTextComponent message = new StringTextComponent("NYI");
					cmdCtx.getSource().sendFeedback(message, true);
					return 1;
				}))
			.then(Commands.literal("target")
				.requires(cmdSrc -> {
					try {
						if (cmdSrc.asPlayer() != null) {
							return true;
						}
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
					StringTextComponent message = new StringTextComponent("Only runable by player!");
					cmdSrc.sendErrorMessage(message);
					return false;
				})
				.then(Commands.literal("whitelist").executes(cmdCtx -> {
					StringTextComponent message;
					RayTraceResult rtr = cmdCtx.getSource().asPlayer().pick(20, 0.0F, false);
					if (rtr.getType() == RayTraceResult.Type.BLOCK) {
						BlockPos bp = ((BlockRayTraceResult)rtr).getPos();
						TileEntity te = cmdCtx.getSource().getWorld().getTileEntity(bp);
						if (te != null && te instanceof ITickableTileEntity) {
							GeneralConfig.teWhiteList.add(te.getClass());
							GeneralConfig.teBlackList.remove(te.getClass());
							message = new StringTextComponent("Added "+te.getClass().getName()+" to TE Whitelist");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new StringTextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendErrorMessage(message);
						return 0;
					}
					message = new StringTextComponent("Only runable by player!");
					cmdCtx.getSource().sendErrorMessage(message);
					return 0;
				}))
				.then(Commands.literal("blacklist").executes(cmdCtx -> {
					StringTextComponent message;
					RayTraceResult rtr = cmdCtx.getSource().asPlayer().pick(20, 0.0F, false);
					if (rtr.getType() == RayTraceResult.Type.BLOCK) {
						BlockPos bp = ((BlockRayTraceResult)rtr).getPos();
						TileEntity te = cmdCtx.getSource().getWorld().getTileEntity(bp);
						if (te != null && te instanceof ITickableTileEntity) {
							GeneralConfig.teBlackList.add(te.getClass());
							GeneralConfig.teWhiteList.remove(te.getClass());
							message = new StringTextComponent("Added "+te.getClass().getName()+" to TE Blacklist");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new StringTextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendErrorMessage(message);
						return 0;
					}
					message = new StringTextComponent("Only runable by player!");
					cmdCtx.getSource().sendErrorMessage(message);
					return 0;
				}))
				.then(Commands.literal("remove").executes(cmdCtx -> {
					StringTextComponent message;
					RayTraceResult rtr = cmdCtx.getSource().asPlayer().pick(20, 0.0F, false);
					if (rtr.getType() == RayTraceResult.Type.BLOCK) {
						BlockPos bp = ((BlockRayTraceResult)rtr).getPos();
						TileEntity te = cmdCtx.getSource().getWorld().getTileEntity(bp);
						if (te != null && te instanceof ITickableTileEntity) {
							GeneralConfig.teBlackList.remove(te.getClass());
							GeneralConfig.teWhiteList.remove(te.getClass());
							message = new StringTextComponent("Removed "+te.getClass().getName()+" from TE classlists");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new StringTextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendErrorMessage(message);
						return 0;
					}
					message = new StringTextComponent("Only runable by player!");
					cmdCtx.getSource().sendErrorMessage(message);
					return 0;
				}))
				.then(Commands.literal("willtick").executes(cmdCtx -> {
					StringTextComponent message;
					RayTraceResult rtr = cmdCtx.getSource().asPlayer().pick(20, 0.0F, false);
					if (rtr.getType() == RayTraceResult.Type.BLOCK) {
						BlockPos bp = ((BlockRayTraceResult)rtr).getPos();
						TileEntity te = cmdCtx.getSource().getWorld().getTileEntity(bp);
						if (te != null && te instanceof ITickableTileEntity) {
							boolean willSerial = ASMHookTerminator.filterTE((ITickableTileEntity)te);
							message = new StringTextComponent("That TE " + (!willSerial ? "will" : "will not") + " tick fully parallelised");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new StringTextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendErrorMessage(message);
						return 0;
					}
					message = new StringTextComponent("Only runable by player!");
					cmdCtx.getSource().sendErrorMessage(message);
					return 0;
				}))
			)
		);
	}

}
