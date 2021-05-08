package com.meteor.starcore.data.plant;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class StageData {
    int day;
    String entityName;
    public StageData(ConfigurationSection configurationSection){
        this.day = configurationSection.getInt("day");
        this.entityName = configurationSection.getString("name");
        this.entityName = ChatColor.translateAlternateColorCodes('&',this.entityName);
    }

    public int getDay() {
        return day;
    }

    public String getEntityName() {
        return entityName;
    }
}
