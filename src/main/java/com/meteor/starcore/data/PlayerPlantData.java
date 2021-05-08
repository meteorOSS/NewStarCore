package com.meteor.starcore.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class PlayerPlantData {
    String playerName;
    int amount;

    public PlayerPlantData(String playerName,int amount){
        this.playerName = playerName;
        this.amount = amount;
    }

    public PlayerPlantData(YamlConfiguration yamlConfiguration){
        this.playerName = yamlConfiguration.getString("player");
        this.amount = yamlConfiguration.getInt("amount");
    }

    public YamlConfiguration toYaml(){
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("player",playerName);
        yamlConfiguration.set("amount",amount);
        return yamlConfiguration;
    }

    public int getLimit(){
        Player p = Bukkit.getPlayer(this.playerName);
        String perm = "mpt.limit.";
        for(PermissionAttachmentInfo permissionAttachmentInfo : p.getEffectivePermissions()){
            String permisson = permissionAttachmentInfo.getPermission();
            if(permisson.startsWith(perm)){
                return Integer.valueOf(permisson.substring(perm.length()));
            }
        }
        return Config.getInstance().getConfig().getInt("setting.limit",100);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void takeAmount(){
        this.amount = this.amount-1;
        this.amount = this.amount<0?0:this.amount;
    }

    public String getPlayerName() {
        return playerName;
    }
}
