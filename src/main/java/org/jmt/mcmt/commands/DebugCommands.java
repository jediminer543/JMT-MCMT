package org.jmt.mcmt.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.command.impl.LocateCommand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
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
				}))).then(Commands.literal("nbtdump")
						.then(Commands.argument("location", Vec3Argument.vec3()).executes(cmdCtx -> {
							ILocationArgument loc = Vec3Argument.getLocation(cmdCtx, "location");
							BlockPos bp = loc.getBlockPos(cmdCtx.getSource());
							ServerWorld sw = cmdCtx.getSource().getWorld();
							BlockState bs = sw.getBlockState(bp);
							TileEntity te = sw.getTileEntity(bp);
							if (te == null) {
								StringTextComponent message = new StringTextComponent(
										"Block at " + bp + " is " + bs.getBlock().getRegistryName() + " has no NBT");
								cmdCtx.getSource().sendFeedback(message, true);
								return 1;
							}
							CompoundNBT nbt = te.serializeNBT();
							ITextComponent itc = nbt.toFormattedComponent();
							StringTextComponent message = new StringTextComponent(
									"Block at " + bp + " is " + bs.getBlock().getRegistryName() + " with TE NBT:");
							cmdCtx.getSource().sendFeedback(message, true);
							cmdCtx.getSource().sendFeedback(itc, true);
							//System.out.println(message.toString());
							return 1;
				}))).then(Commands.literal("tick").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).then(Commands.literal("te"))
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
				.then(Commands.literal("classpathDump").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).executes(cmdCtx -> {
					Path base = Paths.get("classpath_dump/");
					try {
						Files.createDirectories(base);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					// Copypasta from syncfu;
					Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).flatMap(path -> {
			        	File file = new File(path);
			        	if (file.isDirectory()) {
			        		return Arrays.stream(file.list((d, n) -> n.endsWith(".jar")));
			        	}
			        	return Arrays.stream(new String[] {path});
			        }).filter(s -> s.endsWith(".jar"))
					.map(Paths::get).forEach(path -> {
			        	Path name = path.getFileName();
			        	try {
							Files.copy(path, Paths.get(base.toString(), name.toString()), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					
					
					StringTextComponent message = new StringTextComponent("Classpath Dumped to: " + base.toAbsolutePath().toString());
					cmdCtx.getSource().sendFeedback(message, true);
					System.out.println(message.toString());
					return 1;
				}))
				/* 1.16.1 code; AKA the only thing that changed  */
				.then(Commands.literal("test").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).then(Commands.literal("structures").executes(cmdCtx -> {
					ServerPlayerEntity p = cmdCtx.getSource().asPlayer();
					BlockPos srcpos = p.getPosition();
					UUID id = PlayerEntity.getUUID(p.getGameProfile());
					int index = structureIdx.computeIfAbsent(id.toString(), (s) -> new AtomicInteger()).getAndIncrement();
					Structure<?>[] targets = net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES.getValues().toArray(new Structure<?>[10]);
					Structure<?> target = null;
					if (index >= targets.length) {
						target = targets[0];
						structureIdx.computeIfAbsent(id.toString(), (s) -> new AtomicInteger()).set(0);
					} else {
						target = targets[index];
					}
					BlockPos dst = cmdCtx.getSource().getWorld().func_241117_a_(target, srcpos, 100, false);
					if (dst == null) {
						StringTextComponent message = new StringTextComponent("Failed locating " + target.getStructureName() + " from " + srcpos);
						cmdCtx.getSource().sendFeedback(message, true);
						return 1;
					}
					p.teleportKeepLoaded(dst.getX(), srcpos.getY(), dst.getZ());
					LocateCommand.func_241054_a_(cmdCtx.getSource(), target.getStructureName(), srcpos, dst, "commands.locate.success");
					return 1;
				})))
				/* */
				/*
				.then(Commands.literal("goinf").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).executes(cmdCtx -> {
					ServerPlayerEntity p = cmdCtx.getSource().asPlayer();
					p.setPosition(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
					return 1;
				}))
				*/
				;
	}
	
	private static Map<String, AtomicInteger> structureIdx = new ConcurrentHashMap<>();
}
