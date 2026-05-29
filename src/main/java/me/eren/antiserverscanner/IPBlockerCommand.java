package me.eren.antiserverscanner;

import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IPBlockerCommand implements TabExecutor {

	private final Set<String> blockedIps;
	private final int IPS_PER_PAGE = 10;

	public IPBlockerCommand(Set<String> blockedIps) {
		this.blockedIps = blockedIps;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
							 @NotNull String @NonNull [] args) {
		if (args.length == 0) {
			sender.sendRichMessage("<red>Usage: /ipblocker <block|unblock|list>");
			return true;
		}

		String subCommand = args[0].toLowerCase();

		switch (subCommand) {
			case "block" -> {
				if (args.length < 2) {
					sender.sendRichMessage("<red>Usage: /ipblocker block <ip>");
					return true;
				}
				String ip = args[1];
				if (blockedIps.add(ip)) {
					sender.sendRichMessage("<green>IP <white>" + ip + "</white> has been blocked.");
				} else {
					sender.sendRichMessage("<yellow>IP " + ip + " is already blocked.");
				}
			}
			case "unblock" -> {
				if (args.length < 2) {
					sender.sendRichMessage("<red>Usage: /ipblocker unblock <ip>");
					return true;
				}
				String ip = args[1];
				if (blockedIps.remove(ip)) {
					sender.sendRichMessage("<green>IP <white>" + ip + "</white> has been unblocked.");
				} else {
					sender.sendRichMessage("<red>IP " + ip + " was not in the block list.");
				}
			}
			case "list" -> handleList(sender, args);
			default -> sender.sendRichMessage("<red>Unknown subcommand.");
		}

		return true;
	}

	private void handleList(CommandSender sender, String[] args) {
		if (blockedIps.isEmpty()) {
			sender.sendRichMessage("<yellow>The block list is currently empty.");
			return;
		}

		int page = 1;
		if (args.length >= 2) {
			try {
				page = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendRichMessage("<red>Invalid page number.");
				return;
			}
		}

		List<String> list = new ArrayList<>(blockedIps);
		int totalPages = (int) Math.ceil((double) list.size() / IPS_PER_PAGE);

		if (page < 1 || page > totalPages) {
			sender.sendRichMessage("<red>Page " + page + " does not exist. Total pages: " + totalPages);
			return;
		}

		sender.sendRichMessage("<gold>--- Blocked IPs (Page " + page + "/" + totalPages + ") ---");
		int start = (page - 1) * IPS_PER_PAGE;
		int end = Math.min(start + IPS_PER_PAGE, list.size());

		for (int i = start; i < end; i++) {
			sender.sendRichMessage("<white>- " + list.get(i));
		}
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
									  @NotNull String @NonNull [] args) {
		if (args.length == 1) {
			return List.of("block", "unblock", "list");
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("unblock")) {
				return blockedIps.stream().toList();
			}

			if (args[0].equalsIgnoreCase("list")) {
				int totalPages = (int) Math.ceil((double) blockedIps.size() / IPS_PER_PAGE);
				if (totalPages == 0) {
					return List.of("1");
				}

				List<String> pages = new ArrayList<>();
				for (int i = 1; i <= totalPages; i++) {
					pages.add(String.valueOf(i));
				}
				return pages;
			}
		}

		return List.of();
	}

}
