package com.meteor.starcore.data.plant;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Kettle {
    String key;
    String name;
    int maxUseNumber;
    List<String> present;
    ItemStack metaItem;
    int waterRange;
    public Kettle(ConfigurationSection configurationSection){
        this.key = configurationSection.getName();
        this.name = ChatColor.translateAlternateColorCodes('&',configurationSection.getString("name"));
        this.maxUseNumber = configurationSection.getInt("number");
        this.present = configurationSection.getStringList("item.lore");
        this.metaItem = ItemUtil.getItem(configurationSection,"item");
        this.waterRange = configurationSection.getInt("waterRange",1);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getMaxUseNumber() {
        return maxUseNumber;
    }

    public ItemStack toItemStack(){
        ItemStack metaItem = getMetaItem();
        ItemStack item = ItemUtil.getItem(Config.getInstance().getMessageYaml(),"format.kettle");
        ItemStack itemStack = metaItem.getType()== Material.STONE?item.clone():metaItem.clone();

        List<String> lore = new ArrayList<>();

        item.getItemMeta().getLore().forEach(string ->{
            if(string.equalsIgnoreCase("@present@")){
                metaItem.getItemMeta().getLore().forEach(l->lore.add(ChatColor.translateAlternateColorCodes('&',l)));
                return;
            }
            lore.add(ChatColor.translateAlternateColorCodes('&',string));
        });

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(item.getItemMeta().getDisplayName().replace("@kettle@",getName()));
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setBoolean("isKettle",true);
        nbtItem.setString("kettle",getKey());

        return nbtItem.getItem();
    }

    public ItemStack getMetaItem() {
        return metaItem;
    }

    public Kettle getKettle(ItemStack itemStack){
        if(itemStack.hasItemMeta()&&itemStack.getItemMeta().hasDisplayName()){
            if(itemStack.getItemMeta().getDisplayName().equalsIgnoreCase(name))
                return this;
        }
        return null;
    }

    public int getWaterRange() {
        return waterRange;
    }
}
