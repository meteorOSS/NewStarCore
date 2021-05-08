package com.meteor.starcore;

import com.meteor.starcore.data.time.Season;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PAPIhook extends PlaceholderExpansion {
    StarCore plugin;
    public PAPIhook(StarCore plugin){
        this.plugin = plugin;
    }
    @Override
    public String getIdentifier() {
        return "star";
    }

    @Override
    public String getAuthor() {
        return "meteor";
    }

    @Override
    public String getVersion() {
        return "bzd";
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if(params.equalsIgnoreCase("season")){
            return plugin.getTimeManager().getTimeInfo().getSeason().getName();
        }else if(params.equalsIgnoreCase("weather")){
            return plugin.getTimeManager().getTimeInfo().getWeather().getName();
        }else if(params.equalsIgnoreCase("hour")){
            return plugin.getTimeManager().getTimeInfo().getHour()+"";
        }else if(params.equalsIgnoreCase("month")){
            return String.valueOf(plugin.getTimeManager().getTimeInfo().getMonth()+1);
        }else if(params.equalsIgnoreCase("minute")){
            return String.valueOf(plugin.getTimeManager().getTimeInfo().getMinute());
        }else if(params.equalsIgnoreCase("daym")){
            return String.valueOf(plugin.getTimeManager().getTimeInfo().getDayOfMonth()+1);
        }else if(params.equalsIgnoreCase("days")){
            return String.valueOf(plugin.getTimeManager().getTimeInfo().getDayOfSeason()+1);
        }else if(params.equalsIgnoreCase("lastday")){
            return String.valueOf(plugin.getTimeManager().getTimeInfo().getNextSeasonLastDay()+1);
        }else if(params.equalsIgnoreCase("nextseason")){
            Season season = plugin.getTimeManager().getTimeInfo().getSeason();
            season = season==Season.SPRING?Season.SUMMER:season==Season.SUMMER?Season.AUTUMN:season==Season.AUTUMN?Season.WINTER:Season.SPRING;
            return season.getName();
        }
        return "";
    }
}
