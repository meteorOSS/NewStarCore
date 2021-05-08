package com.meteor.starcore.data.plant;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fruit {
    String key;
    String name;
    List<String> present;
    Map<FruitQuality,Double> priceMap;
    ItemStack metaItem;
    List<String> cmds;

    public Fruit(String key,YamlConfiguration yamlConfiguration){
        this.key = key;
        this.name = yamlConfiguration.getString("name");
        this.name = ChatColor.translateAlternateColorCodes('&',this.name);
        this.metaItem = ItemUtil.getItem(yamlConfiguration,"item");
        this.present = yamlConfiguration.getStringList("item.lore");
        this.priceMap = new HashMap<>();
        this.cmds = yamlConfiguration.getStringList("cmds");
        for(FruitQuality fr : FruitQuality.values()){
            priceMap.put(fr,yamlConfiguration.getDouble("price."+fr.name().toLowerCase()));
        }
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<String> getPresent() {
        return present;
    }

    public Map<FruitQuality, Double> getPriceMap() {
        return priceMap;
    }

    public ItemStack getMetaItem() {
        return metaItem;
    }

    public List<String> getCmds() {
        return cmds;
    }

    public ItemStack toItemStack(FruitQuality fruitQuality){
        ItemStack metaItem = getMetaItem();
        ItemStack item = ItemUtil.getItem(Config.getInstance().getMessageYaml(),"format.fruit");
        ItemStack itemStack = metaItem.getType()== Material.STONE?item.clone():metaItem.clone();

        List<String> lore = new ArrayList<>();

        item.getItemMeta().getLore().forEach(string ->{
            if(string.equalsIgnoreCase("@present@")){
                metaItem.getItemMeta().getLore().forEach(l->lore.add(ChatColor.translateAlternateColorCodes('&',l)));
                return;
            }
            string = string.replace("@fq@",fruitQuality.getName())
                    .replace("@price@",priceMap.get(fruitQuality)+"");
            lore.add(ChatColor.translateAlternateColorCodes('&',string));
        });

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(item.getItemMeta().getDisplayName().replace("@fruit@",getName())
        .replace("@pz@",fruitQuality.getName()));
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setBoolean("isFruit",true);
        nbtItem.setString("fruit",getKey());
        nbtItem.setString("quality",fruitQuality.name());

        return nbtItem.getItem();
    }
}
