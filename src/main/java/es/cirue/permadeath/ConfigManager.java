package es.cirue.permadeath;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public final class ConfigManager {
    private final Main plugin;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public int getReviveCost() {
        return Math.max(0, config.getInt("revive-cost", 20));
    }

    public int getMaxLives() {
        return Math.max(1, config.getInt("max-lives", 3));
    }

    public boolean shouldBan() {
        return config.getBoolean("elimination.ban", true);
    }

    public boolean shouldKick() {
        return config.getBoolean("elimination.kick", true);
    }

    public String getBanReason() {
        return color(config.getString("elimination.ban-reason", ""));
    }

    public String message(String path) {
        return color(config.getString("messages." + path, ""));
    }

    public String guiText(String path) {
        return color(config.getString("gui." + path, ""));
    }

    public List<String> guiLore(String path) {
        return config.getStringList("gui." + path).stream()
                .map(this::color)
                .collect(Collectors.toList());
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
