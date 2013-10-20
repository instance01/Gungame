package com.comze_instancelabs.gungame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
 
public class futask extends BukkitRunnable {

	private Player player;
	private Location locat;
	
    public futask(Player pla, Location loc) {
    	player = pla;
    	locat = loc;
    }
 
    public void run() {
        // What you want to schedule goes here
        tpaway(player, locat);
    }
 
    
    public void tpaway(Player pla, Location loc){
    	pla.teleport(loc);
    }
}