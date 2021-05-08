package com.meteor.starcore.data.time;

import com.meteor.starcore.data.Config;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Serializable;

public class TimeInfo implements Serializable {

    //分钟: 游戏内20tick

    //本日分钟数
    long dayM;
    //本年分钟数
    long totalM;
    //总分钟
    long time;
    Weather weather;
    public TimeInfo(){
        super();
    }

    public TimeInfo(YamlConfiguration yamlConfiguration){
        this.dayM = yamlConfiguration.getLong("dayM");
        this.totalM = yamlConfiguration.getLong("totalM");
        this.weather = Weather.valueOf(yamlConfiguration.getString("weather"));
        this.time = yamlConfiguration.getLong("time");
    }
    public long getDayM() {
        return dayM;
    }

    public long getTotalM() {
        return totalM;
    }

    private void sendTitle(String path){
        Bukkit.getOnlinePlayers().forEach(player -> {
            String title = Config.getInstance().getMessageManager().getString(path.toLowerCase()+".title");
            String subtitle = Config.getInstance().getMessageManager().getString(path.toLowerCase()+".subtitle");
            player.sendTitle(title,subtitle);
        });
    }

    public void addM(){
        dayM = dayM+1==1440L?1:dayM+1;
        time = time+1;
        if(dayM==1){
            weather = Weather.lootteryWeather(getSeason());
            if(weather!=Weather.SUNNY){
                sendTitle("message.weather."+weather.toString());
            }
        }
        totalM = totalM+1L==2419200?1L:totalM+1L;

        //日出
        if(getHour()==7&&getMinute()==30){
            sendTitle("message.sunrise");
            Config.getInstance().getPlugin().getPlantManager().refreWaterMode();
        }else if(getHour()==19&&getMinute()==30){
            //日落
            sendTitle("message.sunset");
        }
    }


    public int getHour(){
        return (int)Math.ceil(dayM/60);
    }
    public int getMinute(){
        return (int)dayM%60;
    }

    public Weather getWeather() {
        return weather;
    }

    public int getMonth(){
        int month = (int)Math.floor(totalM/201600L);
        return month;
    }

    public Season getSeason(){
        int month = getMonth();
        Season season = month>8?Season.WINTER:month>5?Season.AUTUMN:month>2?Season.SUMMER:Season.SPRING;
        return season;
    }

    public int getDayOfMonth(){
        int month = getMonth();
        long temp = month*201600L;
        return month==0?(int)Math.floor(totalM/1440L):(int) Math.floor((totalM-temp)/1440L);
    }

    public int getDayOfSeason(){
        int season = ((int)Math.floor(totalM/604800L));
        return (int) (season==0?Math.floor(totalM/1440L):Math.floor((totalM-season*604800L)/1440L));
    }

    public int getNextSeasonLastDay(){
        int season = ((int)Math.floor(totalM/604800L))+1;
        return (int)(season==0?Math.floor((604800L-totalM)/1440L):Math.floor(((season*604800L)-totalM)/1440L));
    }

    public long getTime() {
        return time;
    }
}
