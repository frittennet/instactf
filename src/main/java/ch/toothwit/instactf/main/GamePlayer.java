package ch.toothwit.instactf.main;
 
import java.util.List; 
 
import org.bukkit.Location; 
import org.bukkit.entity.Player; 

public class GamePlayer { 
	public Player player; 
	public Team team; 
	public int kills = 0; 
	public int deaths = 0; 
	
	public GamePlayer(Player player, Team team){
		this.player = player; 
		this.team = team; 
	} 
	
	public void respawn(){
		List<Location> respawns = team.SpawnLocations; 
		int random = (int)(Math.random()*respawns.size()); 
		player.teleport(respawns.get(random));  
	}

	public boolean getAllowShooting() {
		return Game.get().getCooldown(this.player); 
	} 
} 
