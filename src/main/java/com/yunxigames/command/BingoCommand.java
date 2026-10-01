package com.yunxigames.command;

import com.mojang.brigadier.CommandDispatcher;
import com.yunxigames.BingoConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

/**
 * {@code /yg bingo ...}：Bingo 集卡玩法（物品板 / 击杀板）的游戏内启停与状态。
 *
 * <p>各玩法包统一往 {@code /yg} 根下挂以玩法名命名的子树（Brigadier 会把各包注册的
 * 同名根节点合并成一棵命令树），与 drops 包的 {@code /yg drops} 同一布局。
 * off 同时关闭两块板；已发下的地图不再刷新盖章判定，下次开启就是新一局（一局制零持久化）。
 */
public final class BingoCommand {
	/** 与 drops 包同一权限档（等价旧「权限等级 2」，OP 可用）。 */
	private static final PermissionCheck PERMISSION = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

	private BingoCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("yg")
				.requires(Commands.hasPermission(PERMISSION))
				.then(Commands.literal("bingo")
						.executes(context -> status(context.getSource()))
						.then(Commands.literal("on")
								.executes(context -> toggle(context.getSource(), true)))
						.then(Commands.literal("off")
								.executes(context -> toggle(context.getSource(), false)))));
	}

	private static int toggle(CommandSourceStack source, boolean enabled) {
		BingoConfig config = BingoConfig.get();
		config.enableItemBingo = enabled;
		config.enableKillBingo = enabled;
		config.save();
		source.sendSuccess(() -> Component.literal("[yg] Bingo 玩法整体：" + (enabled ? "开启" : "关闭")
				+ "（物品板 + 击杀板）"), false);
		return status(source);
	}

	private static int status(CommandSourceStack source) {
		BingoConfig config = BingoConfig.get();
		source.sendSuccess(() -> Component.literal(String.format(
				"[yg] Bingo 玩法=%s | 物品板=%s 击杀板=%s 连线奖励=%d件 全清大奖=%d件",
				config.enableItemBingo || config.enableKillBingo ? "开" : "关",
				config.enableItemBingo ? "开" : "关",
				config.enableKillBingo ? "开" : "关",
				config.bingoLineRewardCount,
				config.bingoClearRewardCount)), false);
		return 1;
	}
}
