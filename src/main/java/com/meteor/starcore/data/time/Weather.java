package com.meteor.starcore.data.time;

import com.meteor.starcore.data.Config;
import com.meteor.starcore.data.plant.RewardItem;
import com.meteor.starcore.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public enum Weather {
    //天气
    STORM("暴风雨"),
    RAIN("雨天"),
    SUNNY("晴天");
    String name;
    Weather(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public  static Weather getRandomWeather(){
        int index = (int)(1+Math.random()*(3-1+1));
        return Weather.values()[index-1];
    }

    public static Weather lootteryWeather(Season season){
        List<RewardItem> rewardItemList = new ArrayList<>();
        String[] strings = Config.getInstance().getConfig().getString("rain-chance."+season.toString()).split("/");
        for(int i=0;i<Weather.values().length;i++){
            Weather weather = Weather.values()[i];
            double chance = Double.parseDouble(strings[i]);
            rewardItemList.add(new RewardItem(weather.toString(),weather.toString(),1,chance));
        }
        RewardItem rewardItem = ItemUtil.lottery(rewardItemList);
        return Weather.valueOf(rewardItem.getName());
    }
}
