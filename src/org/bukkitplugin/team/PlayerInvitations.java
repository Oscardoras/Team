package org.bukkitplugin.team;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkitutils.io.ConfigurationFile;

public class PlayerInvitations {
	
	protected static ConfigurationFile config = new ConfigurationFile(Bukkit.getWorlds().get(0).getWorldFolder().getPath() + "/data/invitations.yml");
	
	
	protected final OfflinePlayer offlinePlayer;
	
	public PlayerInvitations(OfflinePlayer offlinePlayer) {
		this.offlinePlayer = offlinePlayer;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return offlinePlayer;
	}
	
	public List<Team> getInvitations() {
		List<Team> invitations = new ArrayList<Team>();
		if (config.contains("invitations." + offlinePlayer.getUniqueId().toString())) {
			for (String t : config.getStringList("invitations." + offlinePlayer.getUniqueId().toString())) {
				Team team = TeamCommand.scoreboard.getTeam(t);
				if (team != null) invitations.add(team);
			}
		}
		return invitations;
	}
	
	public boolean invite(Team team) {
		if (!team.equals(TeamCommand.scoreboard.getEntryTeam(offlinePlayer.getName()))) {
			List<String> invitations;
			if (config.contains("invitations." + offlinePlayer.getUniqueId().toString()))
				invitations = config.getStringList("invitations." + offlinePlayer.getUniqueId().toString());
			else invitations = new ArrayList<String>();
			if (!invitations.contains(team.getName())) {
				invitations.add(team.getName());
				config.set("invitations." + offlinePlayer.getUniqueId().toString(), invitations);
				config.save();
				return true;
			}
		}
		return false;
	}
	
	public boolean delete(Team team) {
		if (config.contains("invitations." + offlinePlayer.getUniqueId().toString())) {
			List<String> invitations = config.getStringList("invitations." + offlinePlayer.getUniqueId().toString());
			if (invitations.contains(team.getName())) {
				invitations.remove(team.getName());
				config.set("invitations." + offlinePlayer.getUniqueId().toString(), invitations);
				config.save();
				return true;
			}
		}
		return false;
	}
	
	public boolean accept(Team team) {
		if (config.contains("invitations." + offlinePlayer.getUniqueId().toString())) {
			List<String> invitations = config.getStringList("invitations." + offlinePlayer.getUniqueId().toString());
			if (invitations.contains(team.getName())) {
				invitations.remove(team.getName());
				config.set("invitations." + offlinePlayer.getUniqueId().toString(), invitations);
				config.save();
				team.addEntry(offlinePlayer.getName());
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof PlayerInvitations && offlinePlayer.equals(((PlayerInvitations) object).offlinePlayer);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 4 + offlinePlayer.hashCode();
		return hash;
	}
	
	
	public static void sendInvitationMessage(Player player, Team team) {
		String text = new Message("invitations.get").getMessage(player, team.getDisplayName());
		if (Bukkit.getVersion().toLowerCase().contains("spigot")) {
			net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent();
			message.setText(text);
			
			message.addExtra(new net.md_5.bungee.api.chat.TextComponent(" "));
			
			net.md_5.bungee.api.chat.TextComponent accept = new net.md_5.bungee.api.chat.TextComponent();
			accept.setText(new Message("accept").getMessage(player));
			accept.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/t accept " + team.getName()));
			message.addExtra(accept);
			
			message.addExtra(new net.md_5.bungee.api.chat.TextComponent(" "));
			
			net.md_5.bungee.api.chat.TextComponent decline = new net.md_5.bungee.api.chat.TextComponent();
			decline.setText(new Message("decline").getMessage(player));
			decline.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/t decline " + team.getName()));
			message.addExtra(decline);
			
			player.spigot().sendMessage(message);
		} else {
			player.sendMessage(text);
		}
	}
	
}