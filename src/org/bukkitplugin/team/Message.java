package org.bukkitplugin.team;

import org.bukkitutils.io.TranslatableMessage;

public class Message extends TranslatableMessage {
	
	public Message(String path, String... args) {
		super(TeamPlugin.plugin, path, args);
	}
	
}