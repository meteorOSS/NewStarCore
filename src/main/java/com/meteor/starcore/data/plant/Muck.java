package com.meteor.starcore.data.plant;

import com.meteor.meteorlib.mysql.column.Column;
import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Muck {
    String key;
    String name;
    int speedDay;
    ItemStack metaItem;
    List<String> present;
    public Muck(ConfigurationSection configurationSection){
        this.key = configurationSection.getName();
        this.name = ChatColor.translateAlternateColorCodes('&',configurationSection.getString("name"));
        this.speedDay = configurationSection.getInt("day");
        this.metaItem = ItemUtil.getItem(configurationSection,"item");
        this.present = configurationSection.getStringList("item.lore");
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getSpeedDay() {
        return speedDay;
    }

    public Muck getMuck(ItemStack itemStack){
        if(itemStack.hasItemMeta()&&itemStack.getItemMeta().hasDisplayName()){
            if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(name)){
                return this;
            }
        }
        return null;
    }

    public ItemStack getMetaItem() {
        return metaItem;
    }

    public List<String> getPresent() {
        return present;
    }

    public ItemStack toItemStack(){
        ItemStack metaItem = getMetaItem();
        ItemStack item = ItemUtil.getItem(Config.getInstance().getMessageYaml(),"format.muck");
        ItemStack itemStack = metaItem.getType()== Material.STONE?item.clone():metaItem.clone();

        List<String> lore = new ArrayList<>();

        item.getItemMeta().getLore().forEach(string ->{
            if(string.equalsIgnoreCase("@present@")){
                metaItem.getItemMeta().getLore().forEach(l->lore.add(ChatColor.translateAlternateColorCodes('&',l)));
                return;
            }
            string = string.replace("@day@",speedDay+"");
            lore.add(ChatColor.translateAlternateColorCodes('&',string));
        });

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(item.getItemMeta().getDisplayName().replace("@muck@",getName()));
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setBoolean("isMuck",true);
        nbtItem.setString("muck",getKey());

        return nbtItem.getItem();
    }

}
