package ch.toothwit.instactf.main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable; 

public class FlagReturnRoutine extends BukkitRunnable { 
	public Block FlagBlock; 
	public Team FlagTeam; 
	
	public FlagReturnRoutine(Block flagBlock, Team team) {
		this.FlagBlock = flagBlock; 
		this.FlagTeam = team; 
	} 
	
	@Override
	public void run() {
		this.FlagBlock.setType(Material.AIR); 
		this.FlagTeam.FlagSpawnLocation.getBlock().setType(Material.STANDING_BANNER); 
	}
}
