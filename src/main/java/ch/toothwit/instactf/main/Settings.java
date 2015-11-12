package ch.toothwit.instactf.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import ch.toothwit.instactf.main.Instactf; 

public class Settings {
	private static Settings instance; 
	private FileConfiguration config; 
	private int gameDuration; 
	
	public static Settings get() {
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}

	public Settings() { 
		Instactf.get().saveDefaultConfig(); 
		this.config = Instactf.get().getConfig(); 
		
		reloadConfig(); 
	}

	public void reloadConfig() {
		Instactf.get().reloadConfig(); 
		config = Instactf.get().getConfig(); 

		this.gameDuration = config.getInt("game.duration"); 
	}

	public void saveConfig() {
		config.set("game.duration", this.gameDuration);
		
		File gameConfig = new File(Instactf.get().getDataFolder() + "/" + "config.yml");
		try {
			config.save(gameConfig);
		} catch (IOException e) {
			Bukkit.getLogger().warning("Could not save config");
		}
	} 

	public void setLocationList(String path, List<Location> locations){
		List<String> locs = new ArrayList<String>();
		for(Location loc : locations){
		    locs.add(loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
		}
		config.set(path, locs); 
	}
	
	public List<Location> getLocationList(String path){ 
		List<String> locstrings = config.getStringList(path);
		List<Location> locs = new ArrayList<Location>();
		for(String s : locstrings){
		    locs.add(new Location(Bukkit.getWorld(s.split(" ")[0]), Double.parseDouble(s.split(" ")[1]), Double.parseDouble(s.split(" ")[2]), Double.parseDouble(s.split(" ")[3]))); 
		}
		return locs; 
	} 
	
	public void setLocation(String path, Location loc){ 
		if(loc == null){ 
			loc = new Location(Bukkit.getWorlds().get(0), 0d, 100d, 0d); 
		}
		config.set(path, loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ()); 
	}
	
	public Location getLocation(String path){ 
		String s = config.getString(path); 
		return new Location(Bukkit.getWorld(s.split(" ")[0]), Double.parseDouble(s.split(" ")[1]), Double.parseDouble(s.split(" ")[2]), Double.parseDouble(s.split(" ")[3])); 
	} 
	
	public Team getTeam(int team){ 
		Team t = new Team(); 
		t.Identifier = team; 
		t.SpawnLocations = getLocationList("game.teams.team"+team+".spawnLocations"); 
		t.FlagSpawnLocation = getLocation("game.teams.team"+team+".flagLocation"); 
		return t; 
	}
	
	public void setTeam(Team t){
		setLocationList("game.teams.team"+t.Identifier+".spawnLocations", t.SpawnLocations); 
		setLocation("game.teams.team"+t.Identifier+".flagLocation", t.FlagSpawnLocation); 
	}
	
	public int getTeamCount(){ 
		int n = 1; 
		while(config.getConfigurationSection("game.teams.team"+n) != null){ 
			n++; 
		} 
		
		return n; 
	} 
	
	public int getGameDuration() {
		return gameDuration;
	}

	public void setGameDuration(int gameDuration) {
		this.gameDuration = gameDuration;
		saveConfig(); 
	} 
	
	public String getString(String key){ 
		return Settings.get().config.getString("game."+key); 
	}
}
