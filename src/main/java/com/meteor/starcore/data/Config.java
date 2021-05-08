package com.meteor.starcore.data;

import com.meteor.meteorlib.message.MessageManager;
import com.meteor.starcore.StarCore;
import com.meteor.starcore.data.plant.*;
import com.meteor.starcore.data.time.TimeInfo;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static Config config;
    StarCore plugin;
    Map<String, Seed> seedMap;
    MessageManager messageManager;
    Map<String, Kettle> kettleMap;
    Map<String,Muck> muckMap;
    Map<String, Fruit> fruitMap;
    Map<String,Sickle> sickleMap;
    Map<String,FoodData> foodDataMap;
    private Config(StarCore plugin){
        this.plugin = plugin;
        this.reload();
    }
    public static void init(StarCore plugin){
        config = new Config(plugin);
    }

    private void checkConfigFile(){
        Arrays.asList("config.yml","message.yml","kettle.yml","muck.yml","sickle.yml","kettle.yml","food.yml").forEach(name->{
            File file = new File(plugin.getDataFolder()+"/"+name);
            if(!file.exists()){
                plugin.saveResource(name,false);
            }
        });
        File f = new File(plugin.getDataFolder()+"/seeds/test.yml");
        if(!f.exists()){
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.set("name","&b无花果");
            yamlConfiguration.set("stage.1.day",4);
            yamlConfiguration.set("stage.1.name","无花果_1");
            yamlConfiguration.set("stage.2.day",5);
            yamlConfiguration.set("stage.2.name","无花果_2");
            yamlConfiguration.set("break-chance",20);
            yamlConfiguration.set("mellow",2);
            yamlConfiguration.set("fruit.id","无花果");
            yamlConfiguration.set("fruit.reward.1.amount",1);
            yamlConfiguration.set("fruit.reward.1.chance",10);
            yamlConfiguration.set("reward.normal",Arrays.asList("test,测试物品,1,10.0"));
            yamlConfiguration.set("reward.wilt",Arrays.asList("test,测试物品,1,10.0"));
            yamlConfiguration.set("seasons",Arrays.asList("SUMMER","WINTER"));
            yamlConfiguration.set("item.lore",Arrays.asList("这里是介绍"));
            try {
                yamlConfiguration.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File f2 = new File(plugin.getDataFolder()+"/fruit/无花果.yml");
        if(!f2.exists()){
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.set("name","&a无花果果实");
            yamlConfiguration.set("item.lore",Arrays.asList("这里是介绍"));
            yamlConfiguration.set("cmds",Arrays.asList("give @p@ stone 1"));
            for(FruitQuality fq : FruitQuality.values())
                yamlConfiguration.set("price."+fq.name().toLowerCase(),100);
            try {
                yamlConfiguration.save(f2);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void reload(){
        this.checkConfigFile();
        this.plugin.reloadConfig();
        this.messageManager = new MessageManager(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/message.yml")),true);
        this.reloadSeeds();
        this.reloadKettles();
        this.reloadMucks();
        this.reloadFruit();
        this.reloadSickle();
        this.reloadFoodData();
    }

    private void reloadFoodData(){
        foodDataMap = new HashMap<>();
        File file = new File(plugin.getDataFolder()+"/food.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.getKeys(false).forEach(key->foodDataMap.put(key,new FoodData(yamlConfiguration.getConfigurationSection(key))));

    }

    private void reloadFruit(){
        fruitMap = new HashMap<>();
        File file = new File(plugin.getDataFolder()+"/fruit");
        for(File f :file.listFiles()){
            String key = f.getName();
            key = key.substring(0,key.indexOf("."));
            fruitMap.put(key,new Fruit(key,YamlConfiguration.loadConfiguration(f)));
        }
    }

    public Map<String, Fruit> getFruitMap() {
        return fruitMap;
    }

    private void reloadSeeds(){
        seedMap = new HashMap<>();
        File files = new File(plugin.getDataFolder()+"/seeds");
        for(File file :files.listFiles()){
            String key = file.getName();
            key = key.substring(0,key.indexOf("."));
            seedMap.put(key,new Seed(key,YamlConfiguration.loadConfiguration(file)));
        }
    }

    public Map<String, Kettle> getKettleMap() {
        return kettleMap;
    }

    private void reloadKettles(){
        kettleMap = new HashMap<>();
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/kettle.yml"));
        yamlConfiguration.getKeys(false).forEach(s -> kettleMap.put(s,new Kettle(yamlConfiguration.getConfigurationSection(s))));
    }

    private void reloadSickle(){
        sickleMap = new HashMap<>();
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/sickle.yml"));
        yamlConfiguration.getKeys(false).forEach(s->sickleMap.put(s,new Sickle(yamlConfiguration.getConfigurationSection(s))));
    }

    private void reloadMucks(){
        muckMap = new HashMap<>();
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/muck.yml"));
        yamlConfiguration.getKeys(false).forEach(s->muckMap.put(s,new Muck(yamlConfiguration.getConfigurationSection(s))));
    }

    public static Config getInstance(){
        return config;
    }

    public Map<String, Seed> getSeedMap() {
        return seedMap;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void saveTimeInfo(TimeInfo timeInfo){
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("dayM",timeInfo.getDayM());
        yamlConfiguration.set("totalM",timeInfo.getTotalM());
        yamlConfiguration.set("weather",timeInfo.getWeather().toString());
        yamlConfiguration.set("time",timeInfo.getTime());
        try {
            yamlConfiguration.save(plugin.getDataFolder()+"/data.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TimeInfo getTimeInfo(){
        File file = new File(plugin.getDataFolder()+"/data.yml");
        return !file.exists()?new TimeInfo():new TimeInfo(YamlConfiguration.loadConfiguration(file));
    }


    public StarCore getPlugin() {
        return plugin;
    }

    public Map<String, Sickle> getSickleMap() {
        return sickleMap;
    }

    public YamlConfiguration getMessageYaml(){
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()+"/message.yml"));
    }
    public ConfigurationSection getConfig(){
        return plugin.getConfig();
    }


    public Map<String, Muck> getMuckMap() {
        return muckMap;
    }

    public Map<String, FoodData> getFoodDataMap() {
        return foodDataMap;
    }
}
