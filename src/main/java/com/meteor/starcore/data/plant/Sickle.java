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

public class Sickle {
    String key;
    String name;
    List<RewardItem> rewardItems;
    ItemStack metaItem;
    List<String> present;

    public Sickle(ConfigurationSection configurationSection){
        this.key = configurationSection.getName();
        this.name = configurationSection.getString("name");
        this.name = ChatColor.translateAlternateColorCodes('&',this.name);
        this.rewardItems = new ArrayList<>();
        configurationSection.getStringList("reward").forEach(s->{
            String[] strings = s.split(",");
            RewardItem rewardItem = new RewardItem(strings[0],strings[0],Integer.valueOf(strings[1]),Double.valueOf(strings[2]));
            rewardItems.add(rewardItem);
        });
        this.metaItem = ItemUtil.getItem(configurationSection,"item");
        this.present = configurationSection.getStringList("item.lore");
    }

    public ItemStack getMetaItem() {
        return metaItem;
    }

    public String getKey() {
        return key;
    }


    public String getName() {
        return name;
    }

    public List<RewardItem> getRewardItems() {
        return rewardItems;
    }

    public List<String> getPresent() {
        return present;
    }

    public ItemStack toItemStack(){
        ItemStack metaItem = getMetaItem();
        ItemStack item = ItemUtil.getItem(Config.getInstance().getMessageYaml(),"format.sickle");
        ItemStack itemStack = metaItem.getType()== Material.STONE?item.clone():metaItem.clone();

        List<String> lore = new ArrayList<>();

        item.getItemMeta().getLore().forEach(string ->{
            if(string.equalsIgnoreCase("@present@")){
                metaItem.getItemMeta().getLore().forEach(l->lore.add(ChatColor.translateAlternateColorCodes('&',l)));
                return;
            }else if(string.equalsIgnoreCase("@reward@")){
                getRewardItems().forEach(rewardItem -> {
                    String message = Config.getInstance().getMessageManager().getString("format.reward");
                    message = message.replace("@reward@",rewardItem.getName())
                            .replace("@chance@",rewardItem.getChance()+"")
                            .replace("@amount@",rewardItem.getAmount()+"");
                    lore.add(ChatColor.translateAlternateColorCodes('&',message));
                });
                return;
            }
            lore.add(ChatColor.translateAlternateColorCodes('&',string));
        });

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(item.getItemMeta().getDisplayName().replace("@sickle@",getName()));
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setBoolean("isSickle",true);
        nbtItem.setString("sickle",getKey());

        return nbtItem.getItem();
    }
}
