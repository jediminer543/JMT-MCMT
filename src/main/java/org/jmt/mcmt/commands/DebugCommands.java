package org.jmt.mcmt.commands;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import it.unimi.dsi.fastutil.objects.Object2DoubleRBTreeMap;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class DebugCommands {

	public static LiteralArgumentBuilder<CommandSource> registerDebug(LiteralArgumentBuilder<CommandSource> root) {
		return root.then(Commands.literal("getBlockState")
				.then(Commands.argument("location", Vec3Argument.vec3()).executes(cmdCtx -> {
					ILocationArgument loc = Vec3Argument.getLocation(cmdCtx, "location");
					BlockPos bp = loc.getBlockPos(cmdCtx.getSource());
					ServerWorld sw = cmdCtx.getSource().getWorld();
					BlockState bs = sw.getBlockState(bp);
					StringTextComponent message = new StringTextComponent(
							"Block at " + bp + " is " + bs.getBlock().getRegistryName());
					cmdCtx.getSource().sendFeedback(message, true);
					System.out.println(message.toString());
					return 1;
				}))).then(Commands.literal("tick").then(Commands.literal("te"))
						.then(Commands.argument("location", Vec3Argument.vec3()).executes(cmdCtx -> {
							ILocationArgument loc = Vec3Argument.getLocation(cmdCtx, "location");
							BlockPos bp = loc.getBlockPos(cmdCtx.getSource());
							ServerWorld sw = cmdCtx.getSource().getWorld();
							TileEntity te = sw.getTileEntity(bp);
							if (te instanceof ITickableTileEntity) {
								((ITickableTileEntity) te).tick();
								StringTextComponent message = new StringTextComponent(
										"Ticked " + te.getClass().getName() + " at " + bp);
								cmdCtx.getSource().sendFeedback(message, true);
							} else {
								StringTextComponent message = new StringTextComponent("No tickable TE at " + bp);
								cmdCtx.getSource().sendErrorMessage(message);
							}
							return 1;
						})))
				.then(Commands.literal("jarjar").executes(cmdCtx -> {
					boolean jjs = true;
					for (Method m : Object2DoubleRBTreeMap.class.getMethods()) {
						if ((m.getModifiers() & Modifier.STATIC) == 0 && (m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC) {
							jjs &= ((m.getModifiers() & Modifier.SYNCHRONIZED) == Modifier.SYNCHRONIZED);
						}
					}
					StringTextComponent message = new StringTextComponent(
							"Jar Jar Syncs " + (jjs ? "has" : "hasn't") + " forced FastUtil to be synchronised");
					cmdCtx.getSource().sendFeedback(message, true);
					System.out.println(message.toString());
					return 1;
				}));
	}
}
