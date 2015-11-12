package ch.toothwit.instactf.main;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import ch.toothwit.lobby.main.LobbyAPI;
import ch.toothwit.lobby.main.LobbyEventHandler;
import net.md_5.bungee.api.ChatColor;

public class Game implements LobbyEventHandler { 
	private static Game instance; 
	private HashMap<String, GamePlayer> gamePlayers = new HashMap<String, GamePlayer>(); 
	private HashMap<String, Integer> cooldowns = new HashMap<String, Integer>(); 
	
	private GameState gameState; 
	 
	public List<Team> teams = new ArrayList<Team>(); 
	
	public Game(){ 
		LobbyAPI.subscribe(this); 
		this.reload(); 
		
		// cooldown background task ( decrements integer from cooldowns and removes if 0 is hit ) 
		new BukkitRunnable() { 
			public void run() { 
				for(Iterator<Map.Entry<String, Integer>> it = cooldowns.entrySet().iterator(); it.hasNext(); ) { 
					Map.Entry<String, Integer> entry = it.next(); 
					if(entry.getValue().intValue() <= 0) { 
						it.remove(); 
				    } 
					else{
						entry.setValue(entry.getValue()-1); 
					}
				} 
			} 
		}.runTaskTimer(Instactf.get(), 0L, 2L); 
	} 
	
	public static Game get(){
		if(instance == null){
			instance = new Game(); 
		} 
		return instance; 
	} 
	
	private void reload(){
		LobbyAPI.reload(); 
		Bukkit.getScheduler().cancelTask(countdownTask); 
		
		this.gameState = GameState.LOBBY; 
		this.gamePlayers = new HashMap<String, GamePlayer>(); 
		this.teams = new ArrayList<Team>(); 
		for(int n=1;n<=Settings.get().getTeamCount();n++){ 
			teams.set(n, Settings.get().getTeam(n)); 
		} 
	} 
	
	public void setCooldown(Player player){
		cooldowns.put(player.getUniqueId().toString(), 16); 
	}
	
	public boolean getCooldown(Player player){
		return cooldowns.get(player.getUniqueId().toString()) != null; 
	}
	
	int countdownTask; 
	
	@SuppressWarnings("deprecation") 
	public void StartGame(List<Player> players) { 
		ItemStack weapon = new ItemStack(Material.STICK); 
		ItemMeta meta = weapon.getItemMeta(); 
		meta.setDisplayName("Rifle"); 
		weapon.setItemMeta(meta); 
		int n=0; 
		for(Player player : players){ 
			Inventory inventory = player.getInventory(); 
			int teamId = n%this.teams.size(); 
			this.gamePlayers.put(player.getUniqueId().toString(), new GamePlayer(player, this.teams.get(teamId))); 
			inventory.clear(); 
			inventory.addItem(weapon); 
			n++; 
		} 
		this.gameState = GameState.RUNNING; 
		
		
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("gameStarted"))); 
		n = 0; 
		for(GamePlayer player : this.gamePlayers.values()){ 
			List<Location> locations = player.team.SpawnLocations; 
			player.player.teleport(locations.get(n%locations.size())); 
			n++; 
		} 
		
		countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Instactf.get(), new BukkitRunnable() { 
			private int timeLeft = Settings.get().getGameDuration(); 
			public void run() {
				timeLeft--; 
				if(timeLeft > 60 && timeLeft % 60 == 0){
					Bukkit.broadcastMessage(MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("minutesLeft")), timeLeft/60)); 
				}
				if(timeLeft == 60){
					Bukkit.broadcastMessage(MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("minuteLeft")), 1)); 
				}
				if(timeLeft == 30){
					Bukkit.broadcastMessage(MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("secondsLeft")), timeLeft)); 
				}
				if(timeLeft <= 5 && timeLeft > 0){
					Bukkit.broadcastMessage(MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("secondsLeft")), timeLeft)); 
				}
				if(timeLeft <= 0){ 
					Game.get().endGame(); 
				}
			} 
		}, 0L, 20L); 
	} 
	
	public GamePlayer getGamePlayer(Player player){ 
		return gamePlayers.get(player.getUniqueId().toString());  
	} 
	
	public void onPlayerKilled(Player shooter, Player victim){
		getGamePlayer(victim).deaths++; 
		getGamePlayer(victim).respawn(); 
		getGamePlayer(shooter).kills++; 
		Bukkit.broadcastMessage(MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("killed")), victim.getName(), shooter.getName())); 
	}
	
	@SuppressWarnings({ "deprecation" })
	public void endGame(){ 
		Bukkit.getScheduler().cancelTask(countdownTask); 
		gameState = GameState.FINISHED; 
		
		List<GamePlayer> ranked = new ArrayList<GamePlayer>(); 
		
		for(Entry<String, GamePlayer> entry : this.gamePlayers.entrySet()) {
			ranked.add(entry.getValue());  
		}
		
		Collections.sort(ranked, new Comparator<GamePlayer>(){
		     public int compare(GamePlayer o1, GamePlayer o2){
		         if(o1.kills == o2.kills)
		             return 0;
		         return o1.kills > o2.kills ? -1 : 1;
		     }
		}); 
		
		int n=1 ; 
		Bukkit.broadcastMessage(ChatColor.GOLD+"===================================="); 
		for(GamePlayer gamePlayer : ranked){ 
			Bukkit.broadcastMessage(ChatColor.RED+"        "+n+""+ChatColor.GOLD+". "+gamePlayer.player.getName()+" ["+gamePlayer.kills+"/"+gamePlayer.deaths+"]"); 
			n++; 
		} 
		Bukkit.broadcastMessage(ChatColor.GOLD+"===================================="); 
		
		Bukkit.broadcastMessage(MessageFormat.format(ChatColor.translateAlternateColorCodes('&', Settings.get().getString("lobbyMessage")), 5)); 
		Bukkit.getScheduler().runTaskLater(Instactf.get(), new BukkitRunnable() { 
			public void run() {                
				for(Player p : Bukkit.getOnlinePlayers()){ 
					p.getInventory().clear(); 
					p.setResourcePack("http://collab.toothwit.ch/emptyPack.zip"); 
					Util.SendToBungeeServer(LobbyAPI.getBungeeLobbyServer(), p); 
				} 
				Game.get().reload(); 
			} 
		}, 5*20L); 
	}
	
	public void setGameState(GameState gameState) { 
		this.gameState = gameState; 
	} 

	public GameState getGameState() { 
		return this.gameState;
	} 
} 
