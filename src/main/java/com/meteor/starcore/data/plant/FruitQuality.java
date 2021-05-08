package com.meteor.starcore.data.plant;

import com.meteor.starcore.StarCore;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.util.ItemUtil;

import java.util.Arrays;
import java.util.List;

public enum  FruitQuality {
    NORMAL("普通"),
    SILVER_STAR("银星"),
    GOLD_STAR("金星");

    FruitQuality(String name){
        this.name = name;
    }

    private String name;


    public String getName() {
        return name;
    }

    public static FruitQuality lottery(double chance1,double chance2){
        RewardItem rewardItem = new RewardItem(NORMAL.toString(),NORMAL.toString(),1,Config.getInstance().getConfig().getDouble("chance.normal"));
        RewardItem rewardItem2 = new RewardItem(SILVER_STAR.toString(),SILVER_STAR.toString(),1,Config.getInstance().getConfig().getDouble("chance.silver")+chance1);
        RewardItem rewardItem3 = new RewardItem(GOLD_STAR.toString(),GOLD_STAR.toString(),1,Config.getInstance().getConfig().getDouble("chance.gold")+chance2);

        List<RewardItem> rewardItemList = Arrays.asList(rewardItem,rewardItem2,rewardItem3);

        return FruitQuality.valueOf(ItemUtil.lottery(rewardItemList).getName());
    }
}
