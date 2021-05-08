package com.meteor.starcore.data;

import com.meteor.starcore.data.plant.Fruit;
import com.meteor.starcore.data.plant.RewardItem;
import com.meteor.starcore.data.time.Season;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodData {
    Map<Integer, Map<Season,List<RewardItem>>> fruitMap;

    public FoodData(ConfigurationSection configurationSection){
        ConfigurationSection fruitConfig = configurationSection.getConfigurationSection("fruit");
        fruitMap = new HashMap<>();
        fruitConfig.getKeys(false).forEach(key->{
            int index = Integer.valueOf(key);
            Map<Season,List<RewardItem>> seasonListMap = new HashMap<>();
            ConfigurationSection seasonConfig = fruitConfig.getConfigurationSection(key);
            seasonConfig.getKeys(false).forEach(string -> {
                Season season = Season.valueOf(string);
                List<RewardItem> rewardItemList = new ArrayList<>();
                seasonConfig.getStringList(string).forEach(s->{
                    String[] strings = s.split("#");
                    rewardItemList.add(new RewardItem(strings[0],strings[0],Integer.parseInt(strings[1]),Double.parseDouble(strings[2])));
                });
                seasonListMap.put(season,rewardItemList);
            });
            this.fruitMap.put(index,seasonListMap);
        });
    }

    public List<RewardItem> getRewardList(int data,Season season){
        if(!fruitMap.containsKey(data)||!fruitMap.get(data).containsKey(season))
            return null;
        return fruitMap.get(data).get(season);
    }


}
