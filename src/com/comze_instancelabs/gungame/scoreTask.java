package com.comze_instancelabs.gungame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
 
public class scoreTask extends BukkitRunnable {

	private Player player;
	private Integer lvl;
	
    public scoreTask(Player pla, Integer lv) {
    	player = pla;
    	lvl = lv;
    }
 
    public void run() {
        // What you want to schedule goes here
    	this.updateScoreBoard(player, lvl);
    }
 
    
    public void updateScoreBoard(Player p, Integer lv){
    	ScoreboardManager manager = Bukkit.getScoreboardManager();
    	Scoreboard board = manager.getNewScoreboard();
    	board.clearSlot(DisplaySlot.BELOW_NAME);
    	Objective objective = board.registerNewObjective("test", "dummy");
    	objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    	objective.setDisplayName("§3Lv");
    	
    	board.resetScores(p);
    	Score score = objective.getScore(p);
    	score.setScore(lv);
    	
    	p.setScoreboard(board);
    	
    	objective.setDisplayName("§3Lv");
    }
    
}