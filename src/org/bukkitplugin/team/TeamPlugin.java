package org.bukkitplugin.team;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Team;
import org.bukkitutils.BukkitPlugin;

public class TeamPlugin extends BukkitPlugin implements Listener {
	
	public static TeamPlugin plugin;
	
	public TeamPlugin() {
		plugin = this;
	}
	
	
	public boolean showEmptyTeams;
	public boolean showTeamsWithoutPlayer;
	
	@Override
	public void onLoad() {
		TeamCommand.list();
		TeamCommand.members();
		TeamCommand.create();
		TeamCommand.delete();
		TeamCommand.displayName();
		TeamCommand.leave();
		TeamCommand.invitationsList();
		TeamCommand.invitationsInvite();
		TeamCommand.invitationsRevoke();
		TeamCommand.accept();
		TeamCommand.decline();
	}
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		showEmptyTeams = getConfig().getBoolean("show_empty_teams");
		showTeamsWithoutPlayer = getConfig().getBoolean("show_teams_without_player");
		
		TeamCommand.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
			Player player = e.getPlayer();
			for (Team team : new PlayerInvitations(player).getInvitations()) PlayerInvitations.sendInvitationMessage(player, team);
		}, 1L);
	}
	
}