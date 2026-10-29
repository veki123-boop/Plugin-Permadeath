package es.cirue.permadeath;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class DataManager {
    private final Main plugin;
    private final File file;
    private final YamlConfiguration data;

    public DataManager(Main plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("No se pudo crear la carpeta de datos.");
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public int getLives(UUID uuid) {
        String path = "players." + uuid + ".lives";
        if (!data.contains(path)) {
            return plugin.getConfigManager().getMaxLives();
        }
        return Math.max(0, data.getInt(path));
    }

    public int getLives(OfflinePlayer player) {
        return getLives(player.getUniqueId());
    }

    public void setLives(OfflinePlayer player, int lives) {
        String base = "players." + player.getUniqueId();
        data.set(base + ".name", player.getName());
        data.set(base + ".lives", Math.max(0, lives));
        save();
    }

    public void removeElimination(OfflinePlayer player) {
        setLives(player, Math.max(1, getLives(player)));
    }

    public List<OfflinePlayer> getEliminatedPlayers() {
        List<OfflinePlayer> players = new ArrayList<>();
        if (data.getConfigurationSection("players") == null) {
            return players;
        }
        for (String value : data.getConfigurationSection("players").getKeys(false)) {
            try {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(value));
                if (getLives(player) == 0) {
                    players.add(player);
                }
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("UUID inválido en data.yml: " + value);
            }
        }
        return players;
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo guardar data.yml.", exception);
        }
    }
}
