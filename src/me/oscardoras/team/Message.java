package me.oscardoras.team;

import me.oscardoras.spigotutils.io.TranslatableMessage;

public class Message extends TranslatableMessage {
	
	public Message(String path, String... args) {
		super(TeamPlugin.plugin, path, args);
	}
	
}