package es.cirue.permadeath;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class Main extends JavaPlugin implements CommandExecutor, TabCompleter {
    private ConfigManager configManager;
    private DataManager dataManager;
    private ReviveMenuGUI reviveMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);

        reviveMenu = new ReviveMenuGUI(this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(reviveMenu, this);
        if (getCommand("permadeath") != null) {
            getCommand("permadeath").setExecutor(this);
            getCommand("permadeath").setTabCompleter(this);
        }
        getLogger().info("Permadeath habilitado.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("permadeath.admin")) {
                sender.sendMessage(configManager.message("no-permission"));
                return true;
            }
            configManager.reload();
            sender.sendMessage(configManager.message("config-reloaded"));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("revive")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(configManager.message("player-only"));
                return true;
            }
            reviveMenu.open(player);
            return true;
        }
        sender.sendMessage(configManager.message("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("revive", "reload");
        }
        return Collections.emptyList();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
