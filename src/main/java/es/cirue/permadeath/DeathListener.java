package es.cirue.permadeath;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathListener implements Listener {
    private final Main plugin;

    public DeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        DataManager data = plugin.getDataManager();
        int lives = Math.max(0, data.getLives(player) - 1);
        data.setLives(player, lives);

        if (lives > 0) {
            player.sendMessage(plugin.getConfigManager().message("death")
                    .replace("{lives}", String.valueOf(lives)));
            return;
        }

        player.sendMessage(plugin.getConfigManager().message("eliminated"));
        if (plugin.getConfigManager().shouldBan()) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                    player.getName(), plugin.getConfigManager().getBanReason(), null, "Permadeath");
        }
        if (plugin.getConfigManager().shouldKick()) {
            Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(
                    plugin.getConfigManager().getBanReason()));
        }
    }
}
