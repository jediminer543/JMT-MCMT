package org.jmt.mcmt.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.jmt.mcmt.asmdest.ASMHookTerminator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class PerfCommand {

	public static LiteralArgumentBuilder<CommandSource> registerPerf(LiteralArgumentBuilder<CommandSource> root) {
		return root.then(Commands.literal("mspt").executes(cmdCtx -> {
			try {
				double mspt = Arrays.stream(ASMHookTerminator.lastTickTime)
						.boxed().collect(Collectors.toList()).subList(0, ASMHookTerminator.lastTickTimeFill)
						.stream().mapToDouble(i->i/1000000.0).average().orElse(0);
				double last = ASMHookTerminator.lastTickTime[(ASMHookTerminator.lastTickTimePos-1)%ASMHookTerminator.lastTickTime.length]/1000000.0;
				StringTextComponent message = new StringTextComponent(
						"Average MSPT was " + mspt + "ms (Last: " + last + "ms)");
				cmdCtx.getSource().sendFeedback(message, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 1;
		})).then(Commands.literal("tps").executes(cmdCtx -> {
			double mspt = Arrays.stream(ASMHookTerminator.lastTickTime)
					.boxed().collect(Collectors.toList()).subList(0, ASMHookTerminator.lastTickTimeFill)
					.stream().mapToDouble(i->i/1000000.0).average().orElse(0);
			StringTextComponent message = new StringTextComponent(
					"Theoretical peak average TPS is " + 1000/mspt + "tps");
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}));
	}
}
