package com.meteor.starcore.data.plant;

import com.meteor.starcore.data.time.Season;
import com.meteor.starcore.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Seed {
    String key;
    String name;
    List<Season> plantSeasons;
    List<RewardItem> normalRewards;
    List<RewardItem> wiltRewards;

    String fruit;
    Map<Integer,StageData> stageDataMap;

    int mellowStage;
    int breakChance;


    List<String> present;

    String material;
    short durable;

    ItemStack metaItem;
    List<RewardItem> rewardList;



    public Seed(String key,YamlConfiguration yamlConfiguration){
        this.key = key;
        this.name = ChatColor.translateAlternateColorCodes('&',yamlConfiguration.getString("name"));
        List<Season> seasonList = new ArrayList<>();
        this.stageDataMap = new HashMap<>();
        yamlConfiguration.getStringList("seasons").forEach(s -> seasonList.add(Season.valueOf(s)));
        this.plantSeasons = seasonList;
        this.normalRewards = RewardItem.getRewardItems(yamlConfiguration.getStringList("reward.normal"));
        this.wiltRewards = RewardItem.getRewardItems(yamlConfiguration.getStringList("reward.wilt"));
        ConfigurationSection stageConfig = yamlConfiguration.getConfigurationSection("stage");
        stageConfig.getKeys(false).forEach(stage->stageDataMap.put(Integer.valueOf(stage),new StageData(stageConfig.getConfigurationSection(stage))));
        this.fruit = yamlConfiguration.getString("fruit.id");
        this.mellowStage = yamlConfiguration.getInt("mellow");

        this.present = yamlConfiguration.getStringList("present");
        this.material = yamlConfiguration.getString("material");
        this.durable = (short)yamlConfiguration.getInt("durable");

        this.metaItem = ItemUtil.getItem(yamlConfiguration,"item");
        this.rewardList = new ArrayList<>();
        ConfigurationSection rewardConfig = yamlConfiguration.getConfigurationSection("fruit.reward");
        rewardConfig.getKeys(false).forEach(r->{
            RewardItem rewardItem = new RewardItem(r,r,rewardConfig.getInt(r+".amount"),rewardConfig.getDouble(r+".chance"));
            rewardList.add(rewardItem);
        });

    }

    public int getBreakChance() {
        return breakChance;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getFruit() {
        return fruit;
    }

    public int getMellowStage() {
        return mellowStage;
    }

    public List<Season> getPlantSeasons() {
        return plantSeasons;
    }

    public List<RewardItem> getNormalRewards() {
        return normalRewards;
    }

    public List<RewardItem> getWiltRewards() {
        return wiltRewards;
    }


    public Map<Integer, StageData> getStageDataMap() {
        return stageDataMap;
    }

    public ItemStack getMetaItem() {
        return metaItem;
    }

    public int getTotalDay(){
        AtomicInteger i= new AtomicInteger();
        stageDataMap.forEach((k,v)-> i.addAndGet(v.getDay()));
        return i.get();
    }

    public List<RewardItem> getRewardList() {
        return rewardList;
    }
}
