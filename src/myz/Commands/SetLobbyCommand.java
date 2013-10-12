/**
 * 
 */
package myz.Commands;

import myz.MyZ;
import myz.Support.Configuration;
import myz.Support.Messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * @author Jordan
 * 
 */
public class SetLobbyCommand implements CommandExecutor {

	WorldEditPlugin plugin;

	public SetLobbyCommand() {
		Plugin plugin = MyZ.instance.getServer().getPluginManager().getPlugin("WorldEdit");
		if (plugin != null && plugin.isEnabled() && plugin instanceof WorldEditPlugin)
			this.plugin = (WorldEditPlugin) plugin;
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (plugin != null && plugin.isEnabled()) {
				Selection selection = plugin.getSelection((Player) sender);
				if (selection != null && selection instanceof CuboidSelection) {
					Configuration.setLobbyRegion((CuboidSelection) selection);
					Messenger.sendConfigMessage(sender, "command.setlobby.updated");
				} else
					Messenger.sendConfigMessage(sender, "command.setlobby.requires_cuboid");
			} else
				Messenger.sendConfigMessage(sender, "command.setlobby.requires_cuboid");
		} else
			Messenger.sendConsoleMessage(ChatColor.RED + "That is a player-only command.");
		return true;
	}
}
