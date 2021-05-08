package com.meteor.starcore.data.plant;

import com.meteor.meteorlib.util.TimeUtil;
import com.meteor.starcore.StarCore;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.data.time.Season;
import com.meteor.starcore.data.time.TimeInfo;
import javafx.stage.Stage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class PlantData {

    String seedKey;
    boolean waterPlant;
    int stage;
    int day;
    Location location;
    Season season;
    boolean wilt;
    String owner;
    boolean harvest;


    public PlantData(String seedKey,Location location,Season season,String owner){
        this.seedKey = seedKey;
        this.location = location;
        this.season = season;
        this.stage = 1;
        this.owner = owner;
        this.day = 0;
        this.harvest = false;

    }

    public PlantData(ConfigurationSection configurationSection){
        this.seedKey = configurationSection.getString("seedKey");
        this.waterPlant = configurationSection.getBoolean("water-plant");
        this.stage = configurationSection.getInt("stage");
        this.day = configurationSection.getInt("day");
        this.wilt = configurationSection.getBoolean("wilt");
        this.owner = configurationSection.getString("owner");
        this.location = (Location)configurationSection.get("location");
        this.season = configurationSection.getString("season")!=null?Season.valueOf(configurationSection.getString("season")):Season.SPRING;
        this.harvest = configurationSection.getBoolean("harvest",false);

    }

    public void saveData(YamlConfiguration yamlConfiguration,UUID uuid){
        ConfigurationSection configurationSection = yamlConfiguration.createSection(uuid.toString());
        configurationSection.set("seedKey",seedKey);
        configurationSection.set("water-plant",waterPlant);
        configurationSection.set("stage",stage);
        configurationSection.set("day",day);
        configurationSection.set("wilt",wilt);
        configurationSection.set("owner",owner);
        configurationSection.set("location",location);
        configurationSection.set("season",season.toString());
        configurationSection.set("harvest",harvest);

    }

    //是否可以再次浇水
    public boolean isCanWater(){
        return !waterPlant;
    }


    public void setWaterPlant(boolean waterPlant) {
        this.waterPlant = waterPlant;
    }

    //进入下一阶段or成熟 返回true
    public boolean waterPlant(Seed seed){
        Player player = null;
        StageData stageData = seed.getStageDataMap().get(this.stage);
        this.waterPlant = true;
        this.day+=1;
        if(day>=stageData.getDay()){
            if(stage==seed.getMellowStage()){
                this.day = stageData.getDay();
                return true;
            }
            this.stage++;
            this.day = 0;
            return true;
        }
        return false;
    }

    //植物是否成熟
    public boolean isMellow(Seed seed){
        return stage>=seed.getMellowStage() &&day>=seed.getStageDataMap().get(seed.getMellowStage()).getDay();
    }


    //距离下一阶段还有几天
    public int getNextStageLastDay(Seed seed){
        StageData stageData = seed.getStageDataMap().get(stage);
        return stageData.getDay()-this.day;
    }

    //加速植物生长
    //进入下一阶段返回true
    public boolean speedPlant(int day,Seed seed){
        if(isMellow(seed))
            return true;
        StageData stageData = seed.getStageDataMap().get(stage);
        int lastNextStageDay = (stageData.getDay()-this.day);
        if(day>lastNextStageDay&&stage!=seed.getMellowStage()){
            stage+=1;
            this.day=0;
            int lastSpeedDay = day-lastNextStageDay;
            if(lastSpeedDay>0)
                speedPlant(lastSpeedDay,seed);
            return true;
        }else{
            int newDay = (this.day+day);
            if(newDay>=stageData.getDay()){
                if(stage==seed.getMellowStage()){
                    this.day = stageData.getDay();
                    return true;
                }
                stage++;
                this.day=0;
                return true;
            }else{
                this.day = newDay;
                return false;
            }
        }
    }



    public String getSeedKey() {
        return seedKey;
    }

    public int getStage() {
        return stage;
    }

    public int getDay() {
        return day;
    }


    public boolean isHarvest() {
        return harvest;
    }

    public void setHarvest(boolean harvest) {
        this.harvest = harvest;
    }

    public Location getLocation() {
        return location;
    }

    public Season getSeason() {
        return season;
    }

    public boolean isWilt(Season season) {
        if(wilt)
            return true;
        Seed seed = Config.getInstance().getSeedMap().get(this.seedKey);
        if(!seed.getPlantSeasons().contains(season))
            this.wilt = true;
        return this.wilt;

    }

    public String getOwner() {
        return owner;
    }

    public void setWilt(boolean wilt) {
        this.wilt = wilt;
    }
}
