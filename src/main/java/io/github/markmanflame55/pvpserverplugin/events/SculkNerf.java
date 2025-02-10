package io.github.markmanflame55.pvpserverplugin.events;

import io.github.markmanflame55.pvpserverplugin.PvPServerPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SculkNerf implements Listener {

    PvPServerPlugin plugin = PvPServerPlugin.getPlugin();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.SCULK)) {

            Random random = new Random();

            Player player = e.getPlayer();
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            World world = player.getWorld();


            if (random.nextInt(1, 101) >= this.plugin.sculkNerf) {
                Block block = e.getBlock();
                e.setCancelled(true);
                block.setType(Material.AIR);
                if (itemStack.containsEnchantment(Enchantment.SILK_TOUCH)) {
                    world.dropItem(block.getLocation(), new ItemStack(Material.SCULK));
                }
            }
        }
    }

}
