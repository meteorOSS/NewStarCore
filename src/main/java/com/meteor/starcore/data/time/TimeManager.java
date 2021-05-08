package com.meteor.starcore.data.time;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.meteor.starcore.StarCore;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.data.plant.Seed;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;

public class TimeManager implements PluginMessageListener {
    StarCore plugin;
    TimeInfo timeInfo;
    public TimeManager(StarCore plugin){
        this.plugin = plugin;
        this.registerChannel();
        if(plugin.getConfig().getBoolean("setting.master")){
            timeInfo = Config.getInstance().getTimeInfo();
            Bukkit.getScheduler().runTaskTimer(plugin,()->{
                timeInfo.addM();
                setWorld();
                if(timeInfo.getWeather()!=Weather.SUNNY){
                    //灌溉所有植物
                    plugin.getPlantManager().getPlantDataMap().values().forEach(plantData -> {
                        if(plantData.isCanWater()){
                            Seed seed = Config.getInstance().getSeedMap().get(plantData.getSeedKey());
                            if(seed!=null)
                                plantData.waterPlant(seed);
                        }
                    });
                }
                if(Bukkit.getOnlinePlayers().size()>0)
                    sendTimeInfo(timeInfo);
            },0,20L);
        }else{
            timeInfo = new TimeInfo();
        }
    }
    private void registerChannel(){
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin,"BungeeCord");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,"BungeeCord",this);
    }


    private void setWorld(){
        Bukkit.getWorlds().forEach(world -> {
            if(world.getEnvironment()== World.Environment.NETHER||world.getEnvironment()== World.Environment.THE_END)
                return;
            long time = (timeInfo.getHour()*1000)+(timeInfo.getMinute()*1)+18000;
            world.setTime(time);
            if(timeInfo.getWeather()!=Weather.SUNNY){
                world.setStorm(true);
                world.setWeatherDuration(40);
            }else{
                world.setStorm(false);
            }
        });
    }

    private void sendTimeInfo(TimeInfo timeInfo){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("time");
        byte[] bytes = SerializationUtils.serialize(timeInfo);
        out.writeShort(bytes.length);
        out.write(bytes);
        Iterables.getFirst(Bukkit.getOnlinePlayers(), null).sendPluginMessage(plugin,"BungeeCord",out.toByteArray());
    }




    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(!plugin.getConfig().getBoolean("setting.master")){
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            String subchannel = in.readUTF();
            if(subchannel.equalsIgnoreCase("time")){
                ObjectInputStream ois = null;
                try {
                    short len = in.readShort();
                    byte[] msgbytes = new byte[len];
                    in.readFully(msgbytes);
                    ByteArrayInputStream bais = new ByteArrayInputStream(msgbytes);
                    ois = new ObjectInputStream(bais);
                    this.timeInfo = (TimeInfo)ois.readObject();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public TimeInfo getTimeInfo() {
        return timeInfo;
    }

    public void saveTime(){
        Config.getInstance().saveTimeInfo(timeInfo);
    }
}
