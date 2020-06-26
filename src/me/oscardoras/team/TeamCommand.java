package me.oscardoras.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.oscardoras.spigotutils.command.v1_16_1_V1.Argument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CustomArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.LiteralArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister.CommandExecutorType;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.GreedyStringArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.OfflinePlayerArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.ScoreboardEntryArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.ScoreboardTeamArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.StringArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.ScoreboardEntryArgument.EntrySelector;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public final class TeamCommand {
	private TeamCommand() {}
	
	
	public static Scoreboard scoreboard;
	
	public static void list() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("list", new LiteralArgument("list").withPermission(new Permission("team.command.team.list")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			List<Object> list = new ArrayList<Object>();
			for (Team team : scoreboard.getTeams()) {
				if (TeamPlugin.plugin.showEmptyTeams || !team.getEntries().isEmpty()) {
					boolean show = TeamPlugin.plugin.showTeamsWithoutPlayer;
					if (!show) {
						for (String entry : team.getEntries()) {
							try {
								UUID.fromString(entry);
							} catch (IllegalArgumentException e) {
								show = true;
								break;
							}
						}
					}
					if (show) {
						list.add(new ComponentBuilder(team.getDisplayName())
							.event(new HoverEvent(Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(team.getName())}))
							.create()
						);
					}
				}
			}
			cmd.sendListMessage(list, new Object[] {new Message("team.list.list")}, new Object[] {new Message("team.list.empty")});
			return list.size();
		});
	}
	
	public static void members() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("members", new LiteralArgument("members").withPermission(new Permission("team.command.team.members")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				List<String> list = new ArrayList<String>();
				for (String entry : team.getEntries()) list.add(entry);
				cmd.sendListMessage(list, new Object[] {new Message("team.members.list")}, new Object[] {new Message("team.members.empty")});
				return list.size();
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
		
		arguments.put("team", new ScoreboardTeamArgument().withPermission(new Permission("team.command.team.members.*")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			List<String> list = new ArrayList<String>();
			for (String entry : ((Team) cmd.getArg(0)).getEntries()) list.add(entry);
			cmd.sendListMessage(list, new Object[] {new Message("team.members.list")}, new Object[] {new Message("team.members.empty")});
			return list.size();
		});
	}
	
	public static void create() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("create", new LiteralArgument("create").withPermission(new Permission("team.command.team.create")));
		arguments.put("team", new StringArgument());
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			if (scoreboard.getEntryTeam(cmd.getExecutor().getName()) == null) {
				if (scoreboard.getTeam((String) cmd.getArg(0)) == null) {
					Team team = scoreboard.registerNewTeam((String) cmd.getArg(0));
					team.addEntry(cmd.getExecutor().getName());
					cmd.sendMessage(new Message("team.create"));
					return 1;
				} else {
					cmd.sendFailureMessage(new TranslatableComponent("commands.team.add.duplicate"));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("sender.must_leave_team"));
				return 0;
			}
		});
	}
	
	public static void delete() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("delete", new LiteralArgument("delete").withPermission(new Permission("team.command.team.delete")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				cmd.sendMessage(new Message("team.delete"));
				team.unregister();
				return 1;
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
	}
	
	public static void displayName() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("displayName_literal", new LiteralArgument("displayName").withPermission(new Permission("team.command.team.name")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				String name = team.getDisplayName();
				cmd.sendMessage(new Message("team.name.get", name != null ? name : team.getName()));
				return 1;
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("displayName_literal", new LiteralArgument("displayName").withPermission(new Permission("team.command.team.name")));
		arguments.put("displayName", new GreedyStringArgument());
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				team.setDisplayName((String) cmd.getArg(0));
				cmd.sendMessage(new Message("team.name.set", team.getDisplayName()));
				return 1;
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
	}
	
	public static void leave() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("leave", new LiteralArgument("leave").withPermission(new Permission("team.command.team.leave")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				cmd.sendMessage(new Message("team.leave", cmd.getExecutor().getName()));
				team.removeEntry(cmd.getExecutor().getName());
				if (team.getEntries().isEmpty()) team.unregister();
				return 1;
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
		
		arguments.put("targets", new CustomArgument<String>() {
			protected String parse(String arg, SuggestedCommand cmd) {
				return arg;
			}
		}.withSuggestionsProvider((cmd) -> {
			List<String> list = new ArrayList<String>();
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) list.addAll(team.getEntries());
			return list;
		}).withPermission(new Permission("team.command.team.leave.*")));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				String entry = (String) cmd.getArg(0);
				cmd.sendMessage(new Message("team.leave", entry));
				team.removeEntry(entry);
				if (team.getEntries().isEmpty()) team.unregister();
				return 1;
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
	}
	
	public static void invitationsList() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("invitations", new LiteralArgument("invitations").withPermission(new Permission("team.command.team.invitations")));
		arguments.put("list", new LiteralArgument("list"));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			List<String> list = new ArrayList<String>();
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null)
				for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
					if (new PlayerInvitations(offlinePlayer).getInvitations().contains(team)) list.add(offlinePlayer.getName());
			cmd.sendListMessage(list, new Object[] {new Message("invitations.list.list")}, new Object[] {new Message("invitations.list.empty")});
			return list.size();
		});
	}
	
	public static void invitationsInvite() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("invitations", new LiteralArgument("invitations").withPermission(new Permission("team.command.team.invitations")));
		arguments.put("invite", new LiteralArgument("invite"));
		arguments.put("targets", new ScoreboardEntryArgument(EntrySelector.MANY_ENTITIES).withSuggestionsProvider((cmd) -> {
			List<String> list = new ArrayList<String>();
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null)
				for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
					if (!team.equals(scoreboard.getEntryTeam(offlinePlayer.getName())))
						if (!new PlayerInvitations(offlinePlayer).getInvitations().contains(team)) list.add(offlinePlayer.getName());
			return list;
		}));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				@SuppressWarnings("unchecked")
				Collection<String> entries = (Collection<String>) cmd.getArg(0);
				int value = 0;
				for (String entry : entries) {
					try {
						UUID.fromString(entry);
						if (scoreboard.getEntryTeam(entry) == null) {
							team.addEntry(entry);
							cmd.sendMessage(new Message("invitations.invite", entry));
							value++;
						} else cmd.sendFailureMessage(new Message("invitations.already_sent"));
					} catch (IllegalArgumentException e) {
						@SuppressWarnings("deprecation")
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry);
						if (new PlayerInvitations(offlinePlayer).invite(team)) {
							cmd.sendMessage(new Message("invitations.invite", offlinePlayer.getName()));
							if (offlinePlayer.isOnline()) PlayerInvitations.sendInvitationMessage(offlinePlayer.getPlayer(), team);
							value++;
						} else cmd.sendFailureMessage(new Message("invitations.already_sent"));
					}
				}
				return value;
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
	}
	
	public static void invitationsRevoke() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("invitations", new LiteralArgument("invitations").withPermission(new Permission("team.command.team.invitations")));
		arguments.put("revoke", new LiteralArgument("revoke"));
		arguments.put("targets", new OfflinePlayerArgument().withSuggestionsProvider((cmd) -> {
			List<String> list = new ArrayList<String>();
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null)
				for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers())
					if (new PlayerInvitations(offlinePlayer).getInvitations().contains(team)) list.add(offlinePlayer.getName());
			return list;
		}));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = scoreboard.getEntryTeam(cmd.getExecutor().getName());
			if (team != null) {
				OfflinePlayer offlinePlayer = (OfflinePlayer) cmd.getArg(0);
				if (new PlayerInvitations(offlinePlayer).delete(team)) {
					cmd.sendMessage(new Message("invitations.revoke", offlinePlayer.getName()));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("invitations.not_sent", offlinePlayer.getName()));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
	}
	
	public static void accept() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("accept", new LiteralArgument("accept").withPermission(new Permission("team.command.team.accept")));
		arguments.put("team", new ScoreboardTeamArgument().withSuggestionsProvider((cmd) -> {
			List<String> list = new ArrayList<String>();
			if (scoreboard.getEntryTeam(cmd.getExecutor().getName()) == null)
				for (Team team : new PlayerInvitations((Player) cmd.getExecutor()).getInvitations()) list.add(team.getName());
			return list;
		}));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.PLAYER, (cmd) -> {
			if (scoreboard.getEntryTeam(cmd.getExecutor().getName()) == null) {
				Team team = (Team) cmd.getArg(0);
				if (new PlayerInvitations((Player) cmd.getExecutor()).accept(team)) {
					cmd.sendMessage(new Message("invitations.accept", cmd.getExecutor().getName()));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("sender.is_not_invited", team.getName()));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("sender.must_leave_team"));
				return 0;
			}
		});
	}
	
	public static void decline() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("decline", new LiteralArgument("decline").withPermission(new Permission("team.command.team.decline")));
		arguments.put("team", new ScoreboardTeamArgument().withSuggestionsProvider((cmd) -> {
			List<String> list = new ArrayList<String>();
			for (Team team : new PlayerInvitations((Player) cmd.getExecutor()).getInvitations()) list.add(team.getName());
			return list;
		}));
		CommandRegister.register("t", arguments, new Permission("team.command.team"), CommandExecutorType.PLAYER, (cmd) -> {
			Player player = (Player) cmd.getExecutor();
			if (scoreboard.getEntryTeam(cmd.getExecutor().getName()) == null) {
				Team team = (Team) cmd.getArg(0);
				if (new PlayerInvitations(player).delete(team)) {
					cmd.sendMessage(new Message("invitations.decline", cmd.getExecutor().getName()));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("sender.is_not_invited", team.getName()));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("sender.must_leave_team"));
				return 0;
			}
		});
	}
	
}