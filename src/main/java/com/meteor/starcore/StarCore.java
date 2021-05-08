package com.meteor.starcore;

import com.meteor.starcore.commands.CommandManager;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.data.PlantManager;
import com.meteor.starcore.data.time.TimeInfo;
import com.meteor.starcore.data.time.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StarCore extends JavaPlugin {
    TimeManager timeManager;
    CommandManager commandManager;
    PlantManager plantManager;
    public TimeManager getTimeManager() {
        return timeManager;
    }

    public PlantManager getPlantManager() {
        return plantManager;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Config.init(this);
        (plantManager = new PlantManager(this)).register();
        timeManager = new TimeManager(this);
        if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")){
            getLogger().info("已兼容PlaceholderAPI");
            (new PAPIhook(this)).register();
        }
        (commandManager = new CommandManager(this)).init();
        getCommand("starcore").setExecutor(commandManager);
        getLogger().info("插件已载入..");
        autoSaveData(getConfig().getInt("setting.auto-save",10)*60*20);
    }

    private void autoSaveData(long tick){
        Bukkit.getScheduler().runTaskLater(this,()->{
            plantManager.autoSaveData();
            autoSaveData(tick);
        },tick);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        timeManager.saveTime();
        plantManager.saveData();
        getLogger().info("插件已卸载..");
    }


}
