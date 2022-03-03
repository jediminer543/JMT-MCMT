package org.jmt.mcmt.commands;

import org.jmt.mcmt.asmdest.ASMHookTerminator;
import org.jmt.mcmt.config.GeneralConfig;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ConfigCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> mcmtconfig = Commands.literal("mcmt");
		mcmtconfig = mcmtconfig.then(registerConfig(Commands.literal("config")));
		mcmtconfig = mcmtconfig.then(DebugCommands.registerDebug(Commands.literal("debug")));
		mcmtconfig = StatsCommand.registerStatus(mcmtconfig);
		dispatcher.register(mcmtconfig);
	}

	public static LiteralArgumentBuilder<CommandSourceStack> registerConfig(LiteralArgumentBuilder<CommandSourceStack> root) {
		return root.then(Commands.literal("toggle").requires(cmdSrc -> {
			return cmdSrc.hasPermission(2);
		}).executes(cmdCtx -> {
			GeneralConfig.disabled = !GeneralConfig.disabled;
			TextComponent message = new TextComponent(
					"MCMT is now " + (GeneralConfig.disabled ? "disabled" : "enabled"));
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		}).then(Commands.literal("te").executes(cmdCtx -> {
			GeneralConfig.disableTileEntity = !GeneralConfig.disableTileEntity;
			TextComponent message = new TextComponent("MCMT's tile entity threading is now "
					+ (GeneralConfig.disableTileEntity ? "disabled" : "enabled"));
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		})).then(Commands.literal("entity").executes(cmdCtx -> {
			GeneralConfig.disableEntity = !GeneralConfig.disableEntity;
			TextComponent message = new TextComponent(
					"MCMT's entity threading is now " + (GeneralConfig.disableEntity ? "disabled" : "enabled"));
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		})).then(Commands.literal("environment").executes(cmdCtx -> {
			GeneralConfig.disableEnvironment = !GeneralConfig.disableEnvironment;
			TextComponent message = new TextComponent("MCMT's environment threading is now "
					+ (GeneralConfig.disableEnvironment ? "disabled" : "enabled"));
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		})).then(Commands.literal("world").executes(cmdCtx -> {
			GeneralConfig.disableWorld = !GeneralConfig.disableWorld;
			TextComponent message = new TextComponent(
					"MCMT's world threading is now " + (GeneralConfig.disableWorld ? "disabled" : "enabled"));
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		})).then(Commands.literal("chunkprovider").executes(cmdCtx -> {
			GeneralConfig.disableChunkProvider = !GeneralConfig.disableChunkProvider;
			TextComponent message = new TextComponent(
					"MCMT's SCP threading is now " + (GeneralConfig.disableChunkProvider ? "disabled" : "enabled"));
			cmdCtx.getSource().sendSuccess(message, true);
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
			TextComponent message = new TextComponent(messageString.toString());
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		}))
		.then(Commands.literal("save").requires(cmdSrc -> {
			return cmdSrc.hasPermission(2);
		}).executes(cmdCtx -> {
			TextComponent message = new TextComponent("Saving MCMT config to disk...");
			cmdCtx.getSource().sendSuccess(message, true);
			GeneralConfig.saveConfig();
			message = new TextComponent("Done!");
			cmdCtx.getSource().sendSuccess(message, true);
			return 1;
		}))
		.then(Commands.literal("temanage").requires(cmdSrc -> {
				return cmdSrc.hasPermission(2);
			})
			.then(Commands.literal("list")
				.executes(cmdCtx -> {
					TextComponent message = new TextComponent("NYI");
					cmdCtx.getSource().sendSuccess(message, true);
					return 1;
				}))
			.then(Commands.literal("target")
				.requires(cmdSrc -> {
					try {
						if (cmdSrc.getPlayerOrException() != null) {
							return true;
						}
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
					TextComponent message = new TextComponent("Only runable by player!");
					cmdSrc.sendFailure(message);
					return false;
				})
				.then(Commands.literal("whitelist").executes(cmdCtx -> {
					TextComponent message;
					HitResult rtr = cmdCtx.getSource().getPlayerOrException().pick(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getLevel().getBlockEntity(bp);
						if (te != null && te instanceof TickingBlockEntity) {
							GeneralConfig.teWhiteList.add(te.getClass());
							GeneralConfig.teBlackList.remove(te.getClass());
							message = new TextComponent("Added "+te.getClass().getName()+" to TE Whitelist");
							cmdCtx.getSource().sendSuccess(message, true);
							return 1;
						}
						message = new TextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendFailure(message);
						return 0;
					}
					message = new TextComponent("Only runable by player!");
					cmdCtx.getSource().sendFailure(message);
					return 0;
				}))
				.then(Commands.literal("blacklist").executes(cmdCtx -> {
					TextComponent message;
					HitResult rtr = cmdCtx.getSource().getPlayerOrException().pick(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getLevel().getBlockEntity(bp);
						if (te != null && te instanceof TickingBlockEntity) {
							GeneralConfig.teBlackList.add(te.getClass());
							GeneralConfig.teWhiteList.remove(te.getClass());
							message = new TextComponent("Added "+te.getClass().getName()+" to TE Blacklist");
							cmdCtx.getSource().sendSuccess(message, true);
							return 1;
						}
						message = new TextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendFailure(message);
						return 0;
					}
					message = new TextComponent("Only runable by player!");
					cmdCtx.getSource().sendFailure(message);
					return 0;
				}))
				.then(Commands.literal("remove").executes(cmdCtx -> {
					TextComponent message;
					HitResult rtr = cmdCtx.getSource().getPlayerOrException().pick(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getLevel().getBlockEntity(bp);
						if (te != null && te instanceof TickingBlockEntity) {
							GeneralConfig.teBlackList.remove(te.getClass());
							GeneralConfig.teWhiteList.remove(te.getClass());
							message = new TextComponent("Removed "+te.getClass().getName()+" from TE classlists");
							cmdCtx.getSource().sendSuccess(message, true);
							return 1;
						}
						message = new TextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendFailure(message);
						return 0;
					}
					message = new TextComponent("Only runable by player!");
					cmdCtx.getSource().sendFailure(message);
					return 0;
				}))
				.then(Commands.literal("willtick").executes(cmdCtx -> {
					TextComponent message;
					HitResult rtr = cmdCtx.getSource().getPlayerOrException().pick(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getLevel().getBlockEntity(bp);
						if (te != null && te instanceof TickingBlockEntity) {
							boolean willSerial = ASMHookTerminator.filterTE((TickingBlockEntity)te);
							message = new TextComponent("That TE " + (!willSerial ? "will" : "will not") + " tick fully parallelised");
							cmdCtx.getSource().sendSuccess(message, true);
							return 1;
						}
						message = new TextComponent("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendFailure(message);
						return 0;
					}
					message = new TextComponent("Only runable by player!");
					cmdCtx.getSource().sendFailure(message);
					return 0;
				}))
			)
		);
	}

}
