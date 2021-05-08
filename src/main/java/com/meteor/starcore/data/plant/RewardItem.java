package com.meteor.starcore.data.plant;


import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class RewardItem {
    String id;
    String name;
    int amount;
    double chance;

    public RewardItem(String id, String name, int amount, double chance) {
        this.id = id;
        this.name = name;
        this.name = ChatColor.translateAlternateColorCodes('&',this.name);
        this.amount = amount;
        this.chance = chance;
    }



    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public double getChance() {
        return chance;
    }

    public static List<RewardItem> getRewardItems(List<String> stringList){
        List<RewardItem> rewardItems = new ArrayList<>();
        stringList.forEach(s -> {
            String[] strings = s.split(",");
            String name = ChatColor.translateAlternateColorCodes('&',strings[1]);
            int amount = Integer.valueOf(strings[2]);
            double chance = Double.valueOf(strings[3]);
            RewardItem rewardItem = new RewardItem(strings[0],name,amount,chance);
            rewardItems.add(rewardItem);
        });
        return rewardItems;
    }
}
