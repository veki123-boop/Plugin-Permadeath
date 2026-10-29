package es.cirue.permadeath;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReviveMenuGUI implements InventoryHolder, Listener {
    private final Main plugin;
    private final Map<Integer, UUID> targets = new HashMap<>();
    private Inventory inventory;

    public ReviveMenuGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        targets.clear();
        inventory = Bukkit.createInventory(this, 54, plugin.getConfigManager().guiText("title"));
        Map<UUID, OfflinePlayer> eliminated = new LinkedHashMap<>();
        for (OfflinePlayer target : plugin.getDataManager().getEliminatedPlayers()) {
            eliminated.put(target.getUniqueId(), target);
        }
        for (OfflinePlayer target : Bukkit.getOfflinePlayers()) {
            if (plugin.getDataManager().getLives(target) == 0) {
                eliminated.put(target.getUniqueId(), target);
            }
        }
        int slot = 0;
        for (OfflinePlayer target : eliminated.values()) {
            if (slot >= inventory.getSize()) {
                break;
            }
            inventory.setItem(slot, createPlayerItem(target));
            targets.put(slot, target.getUniqueId());
            slot++;
        }
        if (targets.isEmpty()) {
            inventory.setItem(22, createEmptyItem());
        }
        viewer.openInventory(inventory);
    }

    private ItemStack createPlayerItem(OfflinePlayer target) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName(plugin.getConfigManager().guiText("player-name")
                .replace("{player}", target.getName() == null ? target.getUniqueId().toString() : target.getName()));
        List<String> lore = plugin.getConfigManager().guiLore("player-lore");
        List<String> replaced = new ArrayList<>();
        for (String line : lore) {
            replaced.add(line.replace("{player}", target.getName() == null ? "" : target.getName())
                    .replace("{lives}", "0")
                    .replace("{cost}", String.valueOf(plugin.getConfigManager().getReviveCost())));
        }
        meta.setLore(replaced);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEmptyItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().guiText("empty-name"));
        meta.setLore(plugin.getConfigManager().guiLore("empty-lore"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ReviveMenuGUI menu)
                || menu != this) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player reviver) || event.getRawSlot() < 0
                || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }
        UUID targetId = targets.get(event.getRawSlot());
        if (targetId == null) {
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);
        if (plugin.getDataManager().getLives(target) != 0) {
            reviver.sendMessage(plugin.getConfigManager().message("error"));
            reviver.closeInventory();
            return;
        }
        int cost = plugin.getConfigManager().getReviveCost();
        if (!hasDiamonds(reviver, cost)) {
            reviver.sendMessage(plugin.getConfigManager().message("not-enough-diamonds")
                    .replace("{cost}", String.valueOf(cost)));
            return;
        }
        removeDiamonds(reviver, cost);
        plugin.getDataManager().setLives(target, 1);
        if (target.getName() != null) {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(target.getName());
        }
        reviver.sendMessage(plugin.getConfigManager().message("revive-success")
                .replace("{player}", target.getName() == null ? targetId.toString() : target.getName()));
        if (target.isOnline()) {
            target.getPlayer().sendMessage(plugin.getConfigManager().message("revived-notification")
                    .replace("{reviver}", reviver.getName()));
        }
        reviver.closeInventory();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ReviveMenuGUI) {
            event.setCancelled(true);
        }
    }

    private boolean hasDiamonds(Player player, int amount) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                total += item.getAmount();
                if (total >= amount) {
                    return true;
                }
            }
        }
        return amount == 0;
    }

    private void removeDiamonds(Player player, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getSize() && remaining > 0; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType() != Material.DIAMOND) {
                continue;
            }
            int removed = Math.min(remaining, item.getAmount());
            item.setAmount(item.getAmount() - removed);
            remaining -= removed;
            if (item.getAmount() == 0) {
                player.getInventory().setItem(slot, null);
            }
        }
    }
}
