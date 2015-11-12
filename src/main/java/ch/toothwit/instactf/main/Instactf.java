package ch.toothwit.instactf.main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ch.toothwit.instactf.events.PlayerEventListener;
import ch.toothwit.instactf.events.ServerEventListener;
import ch.toothwit.lobby.main.LobbyAPI;
import net.md_5.bungee.api.ChatColor; 

public class Instactf extends JavaPlugin {
	private static Instactf instance;
	
	
	@Override
	public void onEnable() { 
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), this); 
		Bukkit.getPluginManager().registerEvents(new ServerEventListener(), this); 
		
		LobbyAPI.test(); 
		
		getLogger().info("Lobby was enabled");
	}

	
	
	@Override
	public void onDisable() {
		getLogger().info("Lobby was disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { 
		if (command.getName().equalsIgnoreCase("insta") || command.getName().equalsIgnoreCase("instactf")) { 
			String cmd = args[0]; 
			Player player = (Player)sender; 
			if(sender.hasPermission("instactf.user")){
				if(cmd.equalsIgnoreCase("leave")){
					Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), (Player)sender);
				}
			}
			if(sender.hasPermission("instactf.admin")){
				if(cmd.equalsIgnoreCase("addSpawn")){
					if(args.length > 0){ 
						try{
							Game.get().teams.get(Integer.parseInt(args[0])).AddSpawnLocation(player.getLocation()); 
						}
						catch(Exception ex){ 
							player.sendMessage("Parameter muss Team sein");
						}
					} 
					player.sendMessage(ChatColor.GOLD+"Spawn hinzugef\u00FCgt."); 
				}
				else if(cmd.equalsIgnoreCase("addFlag")){
					if(args.length > 0){ 
						try{
							Game.get().teams.get(Integer.parseInt(args[0])).FlagSpawnLocation = (player.getLocation()); 
						}
						catch(Exception ex){ 
							player.sendMessage("Parameter muss Team sein");
						}
					} 
					player.sendMessage(ChatColor.GOLD+"Flaggenstandort hinzugef\u00FCgt."); 
				}
				else if(cmd.equalsIgnoreCase("stop")){
					Game.get().setGameState(GameState.STOPPED); 
					player.sendMessage(ChatColor.GOLD+"Spiel gestoppt."); 
				}
				else if(cmd.equalsIgnoreCase("setDuration")){
					Settings.get().setGameDuration(Integer.parseInt(args[1])); 
					player.sendMessage(ChatColor.GOLD+"Spieldauer auf "+ChatColor.RED+""+args[1]+ChatColor.GOLD+" Sekunden gesetzt."); 
				}
				else{
					player.sendMessage("Unbekannter Befehl."); 
				}
			} 
			
			return true; 
		}
		return false; 
	}

	public static Instactf get() {
		return instance;
	}

	public Instactf() {
		instance = this;
	}
}
