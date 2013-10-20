package com.comze_instancelabs.gungame;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;



public final class Main extends JavaPlugin implements Listener{
	
	//TODO: (99%)
	// [LOW] 1. Add Enderpearls
	
	//TODO: BUGS
	// [LOW] 1. players respawning in arena 1, no matter in which one died
	// [HIGH] 2. not always upgrades when pushed into water
	// [LOW] 3. user gets unlimited stuff from shop after reaching lv 30
	// [LOW] 4. when player in gungame gets killed by a player outside of gungame -> NPE at 898
	
	public boolean create = false;
	static HashMap<Player, Integer> arenap = new HashMap<Player, Integer>();
	public String aren = "";
	public Integer intt = 0;
	ArrayList<String> arenas = new ArrayList<String>();
	Boolean skip = false;
	
	Plugin plugin;
	
	@Override
    public void onEnable(){
		getLogger().info("Initializing gun-game . . .");
		getServer().getPluginManager().registerEvents(this, this);
		
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		plugin = this;
		
		arenas.addAll(getConfig().getConfigurationSection("arena.").getKeys(false));
        
        if(getConfig().getBoolean("config.changearenas") && !skip){
	        int id = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			    public void run() {
			    	Map<Player, Integer> map = arenap;
			    	if(arenap.size() > 0 && arenas.size() > 0){
				    	for (Player key : map.keySet()) {
				    		getLogger().info(Integer.toString(arenap.size()) + " arenas:" + Integer.toString(arenas.size()));
				    		Location t = new Location(key.getWorld(), getConfig().getDouble("arena." + arenas.get(intt) + ".spawn.x"), getConfig().getDouble("arena." + aren + ".spawn.y"), getConfig().getDouble("arena." + aren + ".spawn.z"));
				            key.teleport(t);
				    	}
				    	intt += 1;
				    	if(intt > arenas.size() - 1){
				    		intt = 0;
				    	}
			    	}
			    	
			    }
			}, getConfig().getLong("config.changeinterval"), getConfig().getLong("config.changeinterval"));
        }
		
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("Disabling gungame :(");
    	Map<Player, Integer> map = arenap;
    	for (Player key : map.keySet()) {
    		Location t = new Location(key.getWorld(), getConfig().getDouble("arena." + aren + ".lobbyspawn.x"), getConfig().getDouble("arena." + aren + ".lobbyspawn.y"), getConfig().getDouble("arena." + aren + ".lobbyspawn.z"));
            key.teleport(t);
    	}
    }
    
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("createarena-gun")){
    		if(args.length < 1){
    			sender.sendMessage("Usage: /createarena [Name]");
    		}else{
    			create = true;
    			this.getConfig().set("arena." + args[0] + ".name", args[0]);
    			this.saveConfig();
    			arenaname = args[0];
    			sender.sendMessage("§2Arenaname saved. Now select two points for the area and afterwards create a spawn and a lobby spawn.");
    			
    			ItemStack selectwand = new ItemStack(Material.STICK);
                ItemMeta meta = (ItemMeta) selectwand.getItemMeta();
                meta.setDisplayName("gunwand");
                selectwand.setItemMeta(meta);
                ShapelessRecipe recipe = new ShapelessRecipe(selectwand).addIngredient(Material.POTION);
                Player p = (Player)sender;
                this.getServer().addRecipe(recipe);
                
                p.getInventory().addItem(selectwand);
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("removearena-gun")){
    		if(args.length < 1){
    			sender.sendMessage("Usage: /removearena [Name]");
    		}else{
    			create = true;
    			this.getConfig().set("arena." + args[0], null);
    			this.saveConfig();
    			sender.sendMessage("§4Arenaname removed.");
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("cleararena-gun")){
    		if(args.length < 1){
    			sender.sendMessage("Usage: /cleararena [Name]");
    		}else{
    			while (arenap.values().remove(args[0]));
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("setspawn-gun")){
    		if(args.length < 1){
    			sender.sendMessage("Please provide an arenaname. Usage: /setspawn-gun [name]");
    		}else{
	    		String arena = args[0];
	    		Player p = (Player) sender;
	    		Location l = p.getLocation();
	    		getConfig().set("arena." + args[0] + ".spawn.x", (int)l.getX());
	    		getConfig().set("arena." + args[0] + ".spawn.y", (int)l.getY());
	    		getConfig().set("arena." + args[0] + ".spawn.z", (int)l.getZ());
	    		this.saveConfig();
	    		sender.sendMessage("§2Spawn set!");
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("setlobbyspawn-gun")){
    		if(args.length < 1){
    			sender.sendMessage("Please provide an arenaname. Usage: /setlobbyspawn [name]");
    		}else{
	    		String arena = args[0];
	    		Player p = (Player) sender;
	    		Location l = p.getLocation();
	    		getConfig().set("arena." + args[0] + ".lobbyspawn.x", (int)l.getX());
	    		getConfig().set("arena." + args[0] + ".lobbyspawn.y", (int)l.getY());
	    		getConfig().set("arena." + args[0] + ".lobbyspawn.z", (int)l.getZ());
	    		this.saveConfig();
	    		sender.sendMessage("§2Lobbyspawn set!");
    		}
			return true;
    	}else if(cmd.getName().equalsIgnoreCase("gp")){
    		sender.sendMessage("§2You current gp: " + Integer.toString(getConfig().getInt("player." + sender.getName() + ".gp"))); //gun-points
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("leaderboards-gun") || cmd.getName().equalsIgnoreCase("lb-gun")){
    		sender.sendMessage("§2Leaderboards:\n" + this.getTop5());
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("gungameshop") || cmd.getName().equalsIgnoreCase("ggshop")){
    		IconMenu iconm = new IconMenu("test", 9, new IconMenu.OptionClickEventHandler() {
    			@Override
                public void onOptionClick(IconMenu.OptionClickEvent event) {
                    String d = event.getName();
                    if(d.equalsIgnoreCase("Diamond Sword Lv I")){
                    	event.getPlayer().sendMessage("You need 50 gp for a " + event.getName());
                    	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 49){
                    		getConfig().set("player." + event.getPlayer().getName() + ".items.Diamond_Sword_Lv_I", "Diamond Sword Lv I");
                            getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 50);
                            plugin.saveConfig();
                            addextraitems(event.getPlayer());
                    	}
                    }else if(d.equalsIgnoreCase("Diamond Sword Lv II")){
                    	event.getPlayer().sendMessage("You need 120 gp for a " + event.getName());
                    	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 119){
                    		getConfig().set("player." + event.getPlayer().getName() + ".items.Diamond_Sword_Lv_II", "Diamond Sword Lv II");
                    		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 120);
                            plugin.saveConfig();
                            addextraitems(event.getPlayer());
                    	}
                    }else if(d.equalsIgnoreCase("OP Bow")){
                    	event.getPlayer().sendMessage("You need 80 gp for a " + event.getName());
                    	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 79){
                    		getConfig().set("player." + event.getPlayer().getName() + ".items.OP_Bow", "OP Bow");
                    		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 80);
                            plugin.saveConfig();
                            addextraitems(event.getPlayer());
                    	}
                    }else if(d.equalsIgnoreCase("Instant Heal")){
                    	event.getPlayer().sendMessage("You need 30 gp for a " + event.getName());
                    	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 29){
                    		getConfig().set("player." + event.getPlayer().getName() + ".items.Instant_Heal", "Instant Heal");
                    		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 30);
                            plugin.saveConfig();
                            addextraitems(event.getPlayer());
                    	}
                    }else if(d.equalsIgnoreCase("Diamond Armor")){
                    	event.getPlayer().sendMessage("You need 200 gp for a " + event.getName());
                    	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 199){
                    		getConfig().set("player." + event.getPlayer().getName() + ".items.Diamond_Armor", "Diamond Armor");
                    		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 200);
                            plugin.saveConfig();
                            addextraitems(event.getPlayer());
                    	}
                    }
                    event.setWillClose(true);
                }
            }, this)
    		.setOption(2, new ItemStack(Material.DIAMOND_SWORD, 1), "Diamond Sword Lv I", "Beat them with a Lv I Diamond Sword! [COST: 50gp]")
            .setOption(3, new ItemStack(Material.DIAMOND_SWORD, 1), "Diamond Sword Lv II", "UUhh, someone with a Lv II Dia Sword coming! [COST: 120gp]")
            .setOption(4, new ItemStack(Material.BOW, 1), "OP Bow", "Fuckin Camper! [COST: 80gp]")
            .setOption(5, new ItemStack(Material.POTION, 1), "Instant Heal", "Getting healed 4ever! [COST: 30gp]")
            .setOption(6, new ItemStack(Material.DIAMOND_CHESTPLATE, 1), "Diamond Armor", "You're too good for all this people. [COST: 200gp]");
        	
        	iconm.open((Player) sender);
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("setgunlevel")){
    		if(args.length < 2){
    			sender.sendMessage("Please provide a Player and a level. Usage: /setgunlevel [player] [amount]");
    		}else{
    			if(arenap.containsKey(getServer().getPlayer(args[0]))){
    				arenap.put(getServer().getPlayer(args[0]), Integer.parseInt(args[1]));
    				sender.sendMessage("Set to " + args[1]);
    			}else{
    				sender.sendMessage("This player is not playing gungame right now.");
    			}
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("setgp")){
    		if(args.length < 2){
    			sender.sendMessage("Please provide a Player and an amount. Usage: /setgp [player] [amount]");
    		}else{
    			getConfig().set("player." + args[0] + ".gp", Integer.parseInt(args[1]));
    			this.saveConfig();
    			sender.sendMessage(args[1]);
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("gg")){
    		if(args.length < 1){
    			sender.sendMessage("§2GunGame Help:");
    			sender.sendMessage("§2/gg createarena [name]");
    			sender.sendMessage("§2/gg setspawn [name]");
    			sender.sendMessage("§2/gg setlobby [name]");
    			return true;
    		}else{
    			if(args.length > 0){
    				String action = args[0];
    				//TODO: DO it
    				if(action.equalsIgnoreCase("createarena") && args.length > 1){
    					if (sender.hasPermission("gungame.create"))
    	                {
    						Player temp = (Player)sender;
	    	    			this.getConfig().set(args[1] + ".name", args[1]);
	    	    			this.getConfig().set(args[1] + ".world", temp.getWorld().getName());
	    	    			this.saveConfig();
	    	    			arenaname = args[1];
	    	    			sender.sendMessage(getConfig().getString("strings.createarena"));
    	                }else{
    	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
    	                }
    				}else if(action.equalsIgnoreCase("setlobby") && args.length > 1){
    					if (sender.hasPermission("gungame.setlobby"))
    	                {
	    	    			String arena = args[1];
	    		    		Player p = (Player) sender;
	    		    		Location l = p.getLocation();
	    		    		getConfig().set(args[1] + ".lobbyspawn.x", (int)l.getX());
	    		    		getConfig().set(args[1] + ".lobbyspawn.y", (int)l.getY());
	    		    		getConfig().set(args[1] + ".lobbyspawn.z", (int)l.getZ());
	    		    		getConfig().set(args[1] + ".lobbyspawn.world", p.getWorld().getName());
	    		    		this.saveConfig();
	    		    		sender.sendMessage(getConfig().getString("strings.lobbycreated"));
    	                }else{
    	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
    	                }
    					
    				}else if(action.equalsIgnoreCase("setspawn") && args.length > 1){
    					if (sender.hasPermission("gungame.setspawn"))
    	                {
    						String arena = args[1];
    			    		Player p = (Player) sender;
    			    		Location l = p.getLocation();
    			    		getConfig().set(args[1] + ".spawn.x", (int)l.getX());
    			    		getConfig().set(args[1] + ".spawn.y", (int)l.getY());
    			    		getConfig().set(args[1] + ".spawn.z", (int)l.getZ());
    			    		getConfig().set(args[1] + ".spawn.world", p.getWorld().getName());
    			    		this.saveConfig();
    			    		sender.sendMessage(getConfig().getString("strings.spawn1"));
    	                }else{
    	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
    	                }
    				}else if(action.equalsIgnoreCase("removearena") && args.length > 1){
    					if (sender.hasPermission("gungame.remove"))
    	                {
	    	    			this.getConfig().set(args[1], null);
	    	    			this.saveConfig();
	    	    			sender.sendMessage(getConfig().getString("strings.arenaremoved"));
    	                }else{
    	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
    	                }
    				}else if(action.equalsIgnoreCase("leave")){
    					Player p2 = (Player)sender;
    					//getLogger().info("There are " + Integer.toString(arenap.size() - 1) + " Players in the arena now.");
    			    	if(arenap.containsKey(p2)){
    				    	p2.updateInventory();
    				    	p2.getInventory().clear();
    				    	p2.updateInventory();
    				    	
    				    	String arena = aren;
    			    		
    			    		Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
    				    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
    				    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
    			    		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
    				    	Location t = new Location(w, x, y, z);
    			    		
    			    		BukkitTask task = new futask(p2, t).runTaskLater(this, 40);
    				    	
    				    	arenap.remove(p2);
    				    }
    				}
    			}
    			return true;
    		} //end of if(args.length < 1)
    	}
    	return false;
    }
    



    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
    	if(event.getEntity().getKiller() != null){
            if(event.getEntity().getKiller() instanceof Player && event.getEntity() instanceof Player && arenap.containsKey(event.getEntity()) && arenap.containsKey(event.getEntity().getKiller())){
                event.getEntity().setHealth(20);
            	String killerName = event.getEntity().getKiller().getName();
                String entityKilled = event.getEntity().getName();
                getLogger().info(killerName + " killed " + entityKilled);
                Player p1 = event.getEntity().getKiller();
                Player p2 = event.getEntity();
                
                Double x = getConfig().getDouble(aren + ".spawn.x");
    	    	Double y = getConfig().getDouble(aren + ".spawn.y");
    	    	Double z = getConfig().getDouble(aren + ".spawn.z");
    	    	World w = Bukkit.getWorld(getConfig().getString(aren + ".spawn.world"));
    	    	
    	    	Location t = new Location(w, x, y, z);
    	    	p2.teleport(t);
    	    	
    	    	BukkitTask task = new futask(p2, t).runTaskLater(this, 20);
                
                
                //gp updaten:
                Integer gpkiller = 0;
                Integer gploser = 0;
                if(this.configContains(killerName, "player.")){
                	gpkiller = getConfig().getInt("player." + killerName + ".gp") + 2; // +2 gp!
                }
                if(this.configContains(entityKilled, "player.")){
                	gploser = getConfig().getInt("player." + entityKilled + ".gp") - 1; // -1 gp!
                }
                //getLogger().info("gpkiller:" + Integer.toString(0) + " gploser:" + Integer.toString(gploser));
                getConfig().set("player." + killerName + ".gp", gpkiller);
                getConfig().set("player." + entityKilled + ".gp", gploser);
                this.saveConfig();
                
                //inv updaten von p2:
                p2.getInventory().clear();
                p2.getInventory().setHelmet(null);
                p2.getInventory().setChestplate(null);
                p2.getInventory().setLeggings(null);
                p2.getInventory().setBoots(null);
                p2.getInventory().setArmorContents(null);
                ItemStack selectwand = new ItemStack(Material.WOOD_SWORD, 1);
                ItemMeta meta = (ItemMeta) selectwand.getItemMeta();
                meta.setDisplayName("gunsword");
                selectwand.setItemMeta(meta);
                p2.playSound(p2.getLocation(), Sound.CAT_MEOW, 1F, 1);
                
                p2.getInventory().addItem(selectwand);
                p2.updateInventory();
                p2.setFoodLevel(20);
                
                arenap.put(p2, 0);
                
                ArrayList<String> keys = new ArrayList<String>();
                if(!getConfig().isConfigurationSection("player." + p2.getName() + ".items")){
                	getLogger().info("The killed player has no special items.");
                }else{
	                keys.addAll(getConfig().getConfigurationSection("player." + p2.getName() + ".items").getKeys(false));
	                for(int i = 0; i < keys.size(); i++){
	                	getConfig().set("player." + p2.getName() + ".items." + keys.get(i), null);
	                	this.saveConfig();
	                }
	                getConfig().set("player." +  p2.getName() + ".items", null);
	                this.saveConfig();
                }
                
                for (PotionEffect effect : p2.getActivePotionEffects())
                    p2.removePotionEffect(effect.getType());
                
                for (PotionEffect effect : p1.getActivePotionEffects())
                    p1.removePotionEffect(effect.getType());
                
                
                //inv updaten von p1
                p1.playEffect(p1.getLocation(), Effect.POTION_BREAK, 5);
                Integer current = arenap.get(p1);
                arenap.put(p1, current + 1);
                p1.sendMessage("You got an upgrade: " + arenap.get(p1));
                Level.updatelv(arenap, p1);
                /*Player[] onlinePlayers = this.getServer().getOnlinePlayers();
                for (Player player : onlinePlayers){
                	player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    this.updateScoreBoard(player, arenap.get(player));
                }*/
                p2.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                //this.updateScoreBoard(p2, arenap.get(p1));
                this.updateScoreBoard();
                //BukkitTask task2 = new scoreTask(p1, arenap.get(p1)).runTaskLater(this, 20);
                p1.setFoodLevel(20);
                p1.setHealth(20);
                p2.setHealth(20);
                p2.setFoodLevel(20);
                
                this.addextraitems(p1);
                this.addextraitems(p2);
            }
        }	
    }

    
    /*@EventHandler
    public void onEntityDamage(EntityDamageEvent event)
	{
	    if (event.getCause().equals(DamageCause.ENTITY_ATTACK)){
	    	EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
	    	if (e.getDamager() instanceof Player && e.getEntity() instanceof Player)
	    	{
				Player damager = (Player)e.getDamager();
				Player damaged = (Player)e.getEntity();
				
				getLogger().info(Integer.toString(damaged.getHealth()));
				
				if(damaged.getHealth() <= 20 || damaged.isDead()){
					event.setCancelled(true);
					damaged.setHealth(40);
					
					String killerName = damager.getName();
	                String entityKilled = damaged.getName();
	                getLogger().info(killerName + " killed " + entityKilled);
	                Player p1 = damager;
	                Player p2 = damaged;
	                
	                p2.setHealth(10);
	                Double x = getConfig().getDouble(aren + ".spawn.x");
	    	    	Double y = getConfig().getDouble(aren + ".spawn.y");
	    	    	Double z = getConfig().getDouble(aren + ".spawn.z");
	    	    	World w = p2.getWorld();
	    	    	
	    	    	Location t = new Location(w, x, y, z);
	    	    	p2.teleport(t);
	    	    	
	    	    	BukkitTask task = new futask(p2, t).runTaskLater(this, 20);
	                
	                //gp updaten:
	                Integer gpkiller = 0;
	                Integer gploser = 0;
	                if(this.configContains(killerName)){
	                	gpkiller = getConfig().getInt(killerName + ".gp") + 2; // +2 gp!
	                }
	                if(this.configContains(entityKilled)){
	                	gploser = getConfig().getInt(entityKilled + ".gp") - 1; // -1 gp!
	                }
	                getConfig().set(killerName + ".gp", gpkiller);
	                getConfig().set(entityKilled + ".gp", gploser);
	                this.saveConfig();
	                
	                //inv updaten von p2:
	                p2.getInventory().clear();
	                p2.getInventory().setHelmet(null);
	                p2.getInventory().setChestplate(null);
	                p2.getInventory().setLeggings(null);
	                p2.getInventory().setBoots(null);
	                p2.getInventory().setArmorContents(null);
	                ItemStack selectwand = new ItemStack(Material.WOOD_SWORD, 1);
	                ItemMeta meta = (ItemMeta) selectwand.getItemMeta();
	                meta.setDisplayName("gunsword");
	                selectwand.setItemMeta(meta);
	                p2.playSound(p2.getLocation(), Sound.CAT_MEOW, 1F, 1);
	                
	                p2.getInventory().addItem(selectwand);
	                p2.updateInventory();
	                
	                arenap.put(p2, 0);
	                
	                //inv updaten von p1
	                p1.playEffect(p1.getLocation(), Effect.POTION_BREAK, 5);
	                Integer current = arenap.get(p1);
	                arenap.put(p1, current + 1);
	                p1.sendMessage("You got an upgrade: " + arenap.get(p1));
	                switch(arenap.get(p1)){
		                case 0:
		                	break;
		                case 1:
		                	p1.getInventory().clear();
		                    p1.getInventory().setHelmet(null);
		                    p1.getInventory().setChestplate(null);
		                    p1.getInventory().setLeggings(null);
		                    p1.getInventory().setBoots(null);
		                    InventoryAdding.addtoinv(p1, Material.WOOD_SWORD, 1, "gunsword");
		                    InventoryAdding.addtoinv(p1, Material.BOW, 1, "gunbow");
		                    InventoryAdding.addtoinv(p1, Material.ARROW, 64, "gunsarrow");
		                	break;
		                case 2:
		                	p1.getInventory().clear();
		                    p1.getInventory().setHelmet(null);
		                    p1.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
		                    p1.getInventory().setLeggings(null);
		                    p1.getInventory().setBoots(null);
		                    InventoryAdding.addtoinv(p1, Material.WOOD_SWORD, 1, "gunsword");
		                    InventoryAdding.addtoinv(p1, Material.BOW, 1, "gunbow");
		                    InventoryAdding.addtoinv(p1, Material.ARROW, 64, "gunsarrow");
		                    InventoryAdding.addtoinv(p1, Material.ARROW, 64, "gunsarrow");
		                	break;
		                case 3:
		                	p1.getInventory().clear();
		                    p1.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
		                    p1.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
		                    p1.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
		                    p1.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
		                    InventoryAdding.addtoinv(p1, Material.WOOD_SWORD, 1, "gunsword");
		                    InventoryAdding.addtoinv(p1, Material.BOW, 1, "gunbow");
		                    InventoryAdding.addtoinv(p1, Material.ARROW, 64, "gunsarrow");
		                    InventoryAdding.addtoinv(p1, Material.ARROW, 64, "gunsarrow");
		                default:
		                	p1.sendMessage("Something went wrong while updating your inventory!");
	                }
				}
	    	}
	    }  
    }*/

    
    public void updateScoreBoard(){
    	ScoreboardManager manager = Bukkit.getScoreboardManager();
    	Scoreboard board = manager.getNewScoreboard();
    	board.clearSlot(DisplaySlot.BELOW_NAME);
    	Objective objective = board.registerNewObjective("test", "dummy");
    	objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    	objective.setDisplayName("§3Lv");
    	
    	
    	for(Player pla : arenap.keySet()){
    		Integer lv = arenap.get(pla);
    		
	    	board.resetScores(pla);
	    	Score score = objective.getScore(pla);
	    	score.setScore(lv);
	    	
	    	pla.setScoreboard(board);
	    	
	    	objective.setDisplayName("§3Lv");
	    	
	    	//getLogger().info(pla.getName() + " " + lv.toString());
    	}

    }

    
    public boolean configContains(String arg){
        boolean boo = false;
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(getConfig().getKeys(false));
        for(int i = 0; i < keys.size(); i++){
            if(keys.get(i).equalsIgnoreCase(arg)){
                boo = true;
            }
        }
        if(boo){
            return true;
        } else {
        return false;
        }
    }
    
    public boolean configContains(String arg, String arg1){
        boolean boo = false;
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(getConfig().getConfigurationSection(arg1).getKeys(false));
        for(int i = 0; i < keys.size(); i++){
            if(keys.get(i).equalsIgnoreCase(arg)){
                boo = true;
            }
        }
        if(boo){
            return true;
        } else {
        return false;
        }
    }
    
    public String getTop5(){
        boolean boo = false;
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(getConfig().getConfigurationSection("player.").getKeys(false));
        ArrayList<Integer> serious = new ArrayList<Integer>();
        HashMap<Integer, String> seriousp = new HashMap<Integer, String>();
        for(int i = 0; i < keys.size(); i++){
        	serious.add(getConfig().getInt("player." + keys.get(i) + ".gp"));
        	seriousp.put(getConfig().getInt("player." + keys.get(i) + ".gp"), keys.get(i));
        }
        Comparator<Integer> comparator = Collections.<Integer>reverseOrder();
        Collections.sort( serious, comparator );
        String retstr = "";
        for(int i = 0; i < serious.size(); i++){
        	retstr += serious.get(i).toString() + " - " + seriousp.get(serious.get(i)) + "\n";
        	if(i == 6){
        		break;
        	}
        }
        return retstr;
    }
    
    /*@EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
    	Player p2 = event.getPlayer();
    	if(arenap.containsKey(p2)){
	    	Double x = getConfig().getDouble(aren + ".spawn.x");
	    	Double y = getConfig().getDouble(aren + ".spawn.y");
	    	Double z = getConfig().getDouble(aren + ".spawn.z");
	    	World w = p2.getWorld();
	    	
	    	Location t = new Location(w, x, y, z);
	    	
	    	BukkitTask task = new futask(p2, t).runTaskLater(this, 40);
	    	
    	}
    }*/
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
    	Player p2 = event.getPlayer();
    	if(arenap.containsKey(p2)){
	    	p2.getInventory().clear();
	        p2.getInventory().setHelmet(null);
	        p2.getInventory().setChestplate(null);
	        p2.getInventory().setLeggings(null);
	        p2.getInventory().setBoots(null);
	        p2.getActivePotionEffects().clear();
	        arenap.remove(p2);
    		Location t = new Location(Bukkit.getWorld(getConfig().getString(aren + ".lobbyspawn.world")), getConfig().getDouble(aren + ".lobbyspawn.x"), getConfig().getDouble("arena." + aren + ".lobbyspawn.y"), getConfig().getDouble("arena." + aren + ".lobbyspawn.z"));
            p2.teleport(t);
	    }
    }
    
    
    
    @EventHandler
    public void onSignUse(PlayerInteractEvent event)
    {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
            {
                Sign s = (Sign) event.getClickedBlock().getState();
 
                for (int i = 0; i < s.getLines().length - 1; i++)
                {
                    if (s.getLine(i).equalsIgnoreCase("§4[GunGame]"))
                    {
                    	//add player to hashmap
                    	String arena = s.getLine(i + 1);
                    	aren = arena;
                    	s.setLine(i + 2, "§4" + Integer.toString(arenap.size()));
                    			
            			arenap.put(event.getPlayer(), 0);
                        Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getDouble(arena + ".spawn.x"), getConfig().getDouble(arena + ".spawn.y"), getConfig().getDouble(arena + ".spawn.z"));
                        event.getPlayer().teleport(t);
                        
                        event.getPlayer().getInventory().clear();
                        event.getPlayer().getInventory().setHelmet(null);
                        event.getPlayer().getInventory().setChestplate(null);
                        event.getPlayer().getInventory().setLeggings(null);
                        event.getPlayer().getInventory().setBoots(null);
                        event.getPlayer().setGameMode(GameMode.SURVIVAL);
                        
                        ItemStack selectwand = new ItemStack(Material.WOOD_SWORD, 1);
                        ItemMeta meta = (ItemMeta) selectwand.getItemMeta();
                        meta.setDisplayName("gunsword");
                        selectwand.setItemMeta(meta);
                        //ShapelessRecipe recipe = new ShapelessRecipe(selectwand).addIngredient(Material.POTION);
                        ShapelessRecipe recipe = new ShapelessRecipe(selectwand);
                        this.getServer().addRecipe(recipe);
                        
                        event.getPlayer().getInventory().addItem(selectwand);
                        event.getPlayer().updateInventory();
	                    
                        //this.updateScoreBoard(event.getPlayer(), 0);
                        this.updateScoreBoard();

                        this.addextraitems(event.getPlayer());

                    } //end of if s.getline .. [BOAT]
                    else if(s.getLine(i).equalsIgnoreCase("[GG-LEAVE]")){
                    	event.getPlayer().getInventory().clear();
                        event.getPlayer().getInventory().setHelmet(null);
                        event.getPlayer().getInventory().setChestplate(null);
                        event.getPlayer().getInventory().setLeggings(null);
                        event.getPlayer().getInventory().setBoots(null);
                        event.getPlayer().updateInventory();
                    	Location t = new Location(Bukkit.getWorld(getConfig().getString(aren + ".lobbyspawn.world")), getConfig().getDouble(aren + ".lobbyspawn.x"), getConfig().getDouble(aren + ".lobbyspawn.y"), getConfig().getDouble(aren + ".lobbyspawn.z"));
                        event.getPlayer().teleport(t);
                        arenap.remove(event.getPlayer());
                        event.getPlayer().getActivePotionEffects().clear();
                        
                        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }else if(s.getLine(i).equalsIgnoreCase("[GP]")){
                		event.getPlayer().sendMessage("§2You current gp: " + Integer.toString(getConfig().getInt("player." + event.getPlayer().getName() + ".gp"))); //gun-points
                    }else if(s.getLine(i).equalsIgnoreCase("[LB-GUN]")){
                    	event.getPlayer().sendMessage("§2Leaderboards:\n" + this.getTop5());
                    }else if(s.getLine(i).equalsIgnoreCase("[GG-SHOP]")){
                    	IconMenu iconm = new IconMenu("test", 9, new IconMenu.OptionClickEventHandler() {
                            @Override
                            public void onOptionClick(IconMenu.OptionClickEvent event) {
                                String d = event.getName();
                                if(d.equalsIgnoreCase("Diamond Sword Lv I")){
                                	event.getPlayer().sendMessage("You need 50 gp for a " + event.getName());
                                	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 49){
                                		getConfig().set("player." + event.getPlayer().getName() + ".items.Diamond_Sword_Lv_I", "Diamond Sword Lv I");
                                        getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 50);
                                        plugin.saveConfig();
                                        addextraitems(event.getPlayer());
                                	}
                                }else if(d.equalsIgnoreCase("Diamond Sword Lv II")){
                                	event.getPlayer().sendMessage("You need 120 gp for a " + event.getName());
                                	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 119){
                                		getConfig().set("player." + event.getPlayer().getName() + ".items.Diamond_Sword_Lv_II", "Diamond Sword Lv II");
                                		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 120);
                                        plugin.saveConfig();
                                        addextraitems(event.getPlayer());
                                	}
                                }else if(d.equalsIgnoreCase("OP Bow")){
                                	event.getPlayer().sendMessage("You need 80 gp for a " + event.getName());
                                	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 79){
                                		getConfig().set("player." + event.getPlayer().getName() + ".items.OP_Bow", "OP Bow");
                                		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 80);
                                        plugin.saveConfig();
                                        addextraitems(event.getPlayer());
                                	}
                                }else if(d.equalsIgnoreCase("Instant Heal")){
                                	event.getPlayer().sendMessage("You need 30 gp for a " + event.getName());
                                	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 29){
                                		getConfig().set("player." + event.getPlayer().getName() + ".items.Instant_Heal", "Instant Heal");
                                		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 30);
                                        plugin.saveConfig();
                                        addextraitems(event.getPlayer());
                                	}
                                }else if(d.equalsIgnoreCase("Diamond Armor")){
                                	event.getPlayer().sendMessage("You need 200 gp for a " + event.getName());
                                	if(getConfig().getInt("player." + event.getPlayer().getName() + ".gp") > 199){
                                		getConfig().set("player." + event.getPlayer().getName() + ".items.Diamond_Armor", "Diamond Armor");
                                		getConfig().set("player." + event.getPlayer().getName() + ".gp", getConfig().getInt("player." + event.getPlayer().getName() + ".gp") - 200);
                                        plugin.saveConfig();
                                        addextraitems(event.getPlayer());
                                	}
                                }
                                event.setWillClose(true);
                            }
                        }, this)
                        .setOption(2, new ItemStack(Material.DIAMOND_SWORD, 1), "Diamond Sword Lv I", "Beat them with a Lv I Diamond Sword! [COST: 50gp]")
                        .setOption(3, new ItemStack(Material.DIAMOND_SWORD, 1), "Diamond Sword Lv II", "UUhh, someone with a Lv II Dia Sword coming! [COST: 120gp]")
                        .setOption(4, new ItemStack(Material.BOW, 1), "OP Bow", "Fuckin Camper! [COST: 80gp]")
                        .setOption(5, new ItemStack(Material.POTION, 1), "Instant Heal", "Getting healed 4ever! [COST: 30gp]")
                        .setOption(6, new ItemStack(Material.DIAMOND_CHESTPLATE, 1), "Diamond Armor", "You're too good for all this people. [COST: 200gp]");
                    	
                    	iconm.open(event.getPlayer());
                    }
                }
            }
        }
    }
    
    // if player under water -> die
    @EventHandler
    public void onmove(PlayerMoveEvent event){ 
    	if(arenap.containsKey(event.getPlayer())){
	        int x = event.getFrom().getBlockX();
	        int fromy = event.getFrom().getBlockY();
	        int y = fromy;
	        int z = event.getFrom().getBlockZ();
	        Location loc = new Location(event.getFrom().getWorld(), x, y, z);
	        
	        if (loc.getBlock().isLiquid()){
	            Player p2 = event.getPlayer();
	            p2.getInventory().clear();
		        p2.getInventory().setHelmet(null);
		        p2.getInventory().setChestplate(null);
		        p2.getInventory().setLeggings(null);
		        p2.getInventory().setBoots(null);
		        Location t = new Location(Bukkit.getWorld(getConfig().getString(aren + ".spawn.world")), getConfig().getDouble(aren + ".spawn.x"), getConfig().getDouble(aren + ".spawn.y"), getConfig().getDouble(aren + ".spawn.z"));
                event.getPlayer().teleport(t);
                
                ArrayList<String> keys = new ArrayList<String>();
                if(!getConfig().isConfigurationSection("player." + p2.getName() + ".items")){
                	getLogger().info("The killed player has no special items.");
                }else{
	                keys.addAll(getConfig().getConfigurationSection("player." + p2.getName() + ".items").getKeys(false));
	                for(int i = 0; i < keys.size(); i++){
	                	getConfig().set("player." + p2.getName() + ".items." + keys.get(i), null);
	                	this.saveConfig();
	                }
	                getConfig().set("player." +  p2.getName() + ".items", null);
	                this.saveConfig();
                }
                
                
                //add the sword again:
                ItemStack selectwand = new ItemStack(Material.WOOD_SWORD, 1);
                ItemMeta meta = (ItemMeta) selectwand.getItemMeta();
                meta.setDisplayName("gunsword");
                selectwand.setItemMeta(meta);
                event.getPlayer().getInventory().addItem(selectwand);
                event.getPlayer().updateInventory();
                
                arenap.put(p2, 0);
                
                //updateScoreBoard(p2, 0);
                this.updateScoreBoard();
	        	//event.getPlayer().setHealth(0);
                boolean continue_ = true;
                Player p1 = null;
                try{
                	EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event.getPlayer().getLastDamageCause();
                	p1 = (Player) e.getDamager();
                }catch(Exception ex){
                	continue_ = false;
                }
                
                p2.setHealth(20);
	            p2.setFoodLevel(20);
	                
                if(continue_){
	                p1.setFoodLevel(20);
	                p1.setHealth(20);
	                
	                
	                String killerName = p1.getName();
	                String entityKilled = event.getPlayer().getName();
	                
	                Integer gpkiller = 0;
	                Integer gploser = 0;
	                if(this.configContains(killerName, "player.")){
	                	gpkiller = getConfig().getInt("player." + killerName + ".gp") + 2; // +2 gp!
	                }
	                if(this.configContains(entityKilled, "player.")){
	                	gploser = getConfig().getInt("player." + entityKilled + ".gp") - 1; // -1 gp!
	                }
	                //getLogger().info("gpkiller:" + Integer.toString(0) + " gploser:" + Integer.toString(gploser));
	                getConfig().set("player." + killerName + ".gp", gpkiller);
	                getConfig().set("player." + entityKilled + ".gp", gploser);
	                this.saveConfig();
	                
	                p1.playEffect(p1.getLocation(), Effect.POTION_BREAK, 5);
	                Integer current = arenap.get(p1);
	                arenap.put(p1, current + 1);
	                p1.sendMessage("You got an upgrade: " + arenap.get(p1));
	                Level.updatelv(arenap, p1);
	                this.updateScoreBoard();
	                p1.setFoodLevel(20);
	                p1.setHealth(20);
	                p2.setHealth(20);
	                p2.setFoodLevel(20);
	                
	                //add extra items
	                this.addextraitems(p1);
                }
                
                
	        }	
    	}
    }
    
    
    public void addextraitems(Player p){
    	ArrayList<String> keys = new ArrayList<String>();
    	boolean continue_ = true;
    	try{
    		keys.addAll(getConfig().getConfigurationSection("player." + p.getName() + ".items").getKeys(false));
    	}catch(Exception ex){
    		continue_ = false;
    	}
    	if(continue_){
	    	for(int i = 0; i < keys.size(); i++){
	        	if(keys.get(i).toString().equalsIgnoreCase("Diamond_Sword_Lv_I")){
	        		InventoryAdding.addtoinv(p, Material.DIAMOND_SWORD, 1, "gunsword", Enchantment.DAMAGE_ALL, 1);
	        	}
	        	if(keys.get(i).toString().equalsIgnoreCase("Diamond_Sword_Lv_II")){
	        		InventoryAdding.addtoinv(p, Material.DIAMOND_SWORD, 1, "gunsword", Enchantment.DAMAGE_ALL, 4);
	        	}
	        	if(keys.get(i).toString().equalsIgnoreCase("Instant_Heal")){
	        		PotionEffect heal = PotionEffectType.HEAL.createEffect(99999999, 7);
	                p.addPotionEffect(heal, true);
	        	}
	        	if(keys.get(i).toString().equalsIgnoreCase("OP_Bow")){
	        		InventoryAdding.addtoinv(p, Material.BOW, 1, "gunbow", Enchantment.ARROW_DAMAGE, 5);
	        	}
	        	if(keys.get(i).toString().equalsIgnoreCase("Diamond_Armor")){
	        		p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
	        		p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
	        		p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
	        		p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1));
	        	}
	        }
    	}
        
    }
    
    
    //Disable fall damage!
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
	{
	    if (event.getCause().equals(DamageCause.FALL) && arenap.containsKey((Player)event.getEntity()) && event.getEntity() instanceof Player){
	    	event.setCancelled(true);
	    }
	}
    
    
    //ARENA CREATION ->
    
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).equalsIgnoreCase("[GUNGAME]")){
        	if (event.getPlayer().hasPermission("comze.gungame.sign"))
            {
        		event.setLine(0, "§4[GunGame]");
        		String name = event.getLine(1);
	            p.sendMessage("Gun-game successfully created!");
            }else{
            	event.setLine(0, "INVALID");
            	p.sendMessage("You don't have permission to create arenas.");
            }
        }
    }

    
    public String arenaname = ""; 
    
    public int xo = 0;
    public int yo = 0;
    public int zo = 0;
    
    public int xp = 0;
    public int yp = 0;
    public int zp = 0;
    
    @EventHandler
    public void onblockbreak(BlockBreakEvent event) {
    	if(create && event.getPlayer().getItemInHand() != null ){
    		if(event.getPlayer().getItemInHand().hasItemMeta() && event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()){
                if(event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("gunwand")){
	                Player p = event.getPlayer();
			        Block bp = event.getBlock();
			        int id = bp.getTypeId();
			        String currentjob = "";
			
			        Location l = bp.getLocation();
			        xo = (int)l.getX();
			        yo = (int)l.getY();
			        zo = (int)l.getZ();
			        
			        event.getPlayer().sendMessage("Registered first point.");
			        event.isCancelled();
                }
           }
    	}
    }

    @EventHandler
    public void onRightclick(PlayerInteractEvent event){
    	if(create){
    		if(event.getPlayer().getItemInHand().hasItemMeta() && event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()){
                if(event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("gunwand")){
		            org.bukkit.event.block.Action click = event.getAction();
			        if(click == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK){
			            Block block = event.getClickedBlock();
			            int blockid = block.getTypeId();
			            
			            Location l = block.getLocation();
			            
			            xp = (int)l.getX();
			            yp = (int)l.getY();
			            zp = (int)l.getZ();
			            
			            getConfig().set("arena." + arenaname + ".xo", xo);
			            getConfig().set("arena." + arenaname + ".yo", yo);
			            getConfig().set("arena." + arenaname + ".zo", zo);
			            getConfig().set("arena." + arenaname + ".xp", xp);
			            getConfig().set("arena." + arenaname + ".yp", yp);
			            getConfig().set("arena." + arenaname + ".zp", zp);
			            
			            this.saveConfig();
			            
			            event.getPlayer().sendMessage("Arena successfully created.");
			            
			            create = false;
			        }
                }//end of "if metaname = boatwand"
    		}
    	}
        
    }
    
}
