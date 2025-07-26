package com.j3ly.moreitems;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ZombieFleshPlugin extends JavaPlugin {

    private final Random random = new Random();

    // === ZOMBIE DROP LISTENER ===
    private final EntityListener entityListener = new EntityListener() {
        @Override
        public void onEntityDeath(EntityDeathEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof Zombie) {
                List<ItemStack> drops = event.getDrops();
                drops.clear(); // remove default drops

                // 75% chance to drop token (351:2), otherwise feather
                if (random.nextInt(100) < 75) {
                    drops.add(new ItemStack(351, 1, (short) 2)); // Token
                } else {
                    drops.add(new ItemStack(288, 1)); // Feather
                }
            }
        }
    };

    // === HEAL ON TOKEN INTERACT ===
    private final PlayerListener playerListener = new PlayerListener() {
        @Override
        public void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            ItemStack item = player.getItemInHand();

            if (item != null && item.getTypeId() == 351 && item.getDurability() == 2) {
                double newHealth = Math.min(player.getHealth() + 6, 20); // +3 hearts
                player.setHealth((int) newHealth);

                int amount = item.getAmount();
                if (amount > 1) {
                    item.setAmount(amount - 1);
                } else {
                    player.setItemInHand(null);
                }
            }
        }
    };

    // === BLOCK BREAK LISTENER ===
    private final BlockListener blockListener = new BlockListener() {
        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            Block block = event.getBlock();

            // 20% chance to drop apple when breaking leaves
            if (block.getType() == Material.LEAVES) {
                if (random.nextInt(100) < 20) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(260, 1));
                }
            }

            // 10% chance to drop cocoa beans (351:3) when breaking logs
            if (block.getType() == Material.LOG) {
                if (random.nextInt(100) < 10) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(351, 1, (short) 3));
                }
            }
        }
    };

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("zomb")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getWorld().spawnCreature(player.getLocation(), org.bukkit.entity.CreatureType.ZOMBIE);
                player.sendMessage("Zombie spawned!");
                return true;
            } else {
                sender.sendMessage("Only players can use this command.");
            }
        }
        return false;
    }
}
