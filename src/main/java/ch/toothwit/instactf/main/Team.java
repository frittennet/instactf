package ch.toothwit.instactf.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Team {
	public int Identifier; 
	public List<Location> SpawnLocations = new ArrayList<Location>(); 
	public List<GamePlayer> GamePlayers = new ArrayList<GamePlayer>(); 
	public int Score = 0; 
	public Location FlagSpawnLocation; 
	public Block FlagBlock; 
	public BukkitRunnable returnRoutine; 
	
	public static DyeColor[] FlagColors = new DyeColor[]{DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN, DyeColor.YELLOW}; 
	
	public void AddSpawnLocation(Location l){ 
		this.SpawnLocations.add(l); 
		Settings.get().setTeam(this); 
	}
	
	public void SetFlagLocation(Location l){ 
		if(this.FlagBlock != null){
			this.FlagBlock.setType(Material.AIR); 
		}
		
		this.FlagSpawnLocation = l; 
		this.FlagBlock = this.FlagSpawnLocation.getWorld().getBlockAt(this.FlagSpawnLocation); 
		this.FlagBlock.setType(Material.STANDING_BANNER); 
		Banner banner = (Banner)this.FlagBlock.getState();
		banner.setBaseColor(FlagColors[this.Identifier]); 
		banner.update();
		Settings.get().setTeam(this); 
	} 
	
	public void OnFlagPickup(GamePlayer gamePlayer){ 
		ItemStack banner = new ItemStack(Material.STANDING_BANNER); 
		BannerMeta meta = (BannerMeta)banner.getItemMeta();
		meta.setBaseColor(DyeColor.RED); 
		
		gamePlayer.player.getInventory().setHelmet(banner); 
		this.FlagBlock.setType(Material.AIR); 
		
		gamePlayer.player.playSound(gamePlayer.player.getLocation(), "instactf.flag.pickup", 1f, 1f); 
		
		if(this.returnRoutine != null){ 
			this.returnRoutine.cancel(); 
			this.returnRoutine = null; 
		} 
	} 
	
	public void OnFlagDrop(GamePlayer gamePlayer){ 
		ItemStack banner = gamePlayer.player.getInventory().getHelmet(); 
		BannerMeta meta = (BannerMeta)banner.getItemMeta(); 
		DyeColor color = meta.getBaseColor(); 
		
		Location l = gamePlayer.player.getLocation(); 
		while(l.getBlock().getType() == Material.AIR){ 
			l.setX(l.getX()-1); 
		} 
		l.setX(l.getX()+1); 
		
		this.FlagBlock = l.getWorld().getBlockAt(l); 
		this.FlagBlock.setType(Material.STANDING_BANNER); 
		Banner blockBanner = (Banner)this.FlagBlock.getState(); 
		blockBanner.setBaseColor(color); 
		gamePlayer.player.playSound(gamePlayer.player.getLocation(), "instactf.flag.drop", 1f, 1f); 
		this.returnRoutine = new FlagReturnRoutine(this.FlagBlock, this); // change it to the actual team the flag is coming from , not the last team that got it 
		this.returnRoutine.runTaskLater(Instactf.get(), 20 * 20L); 
	} 
} 
