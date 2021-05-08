package com.meteor.starcore;

import net.minecraft.server.v1_12_R1.EnumParticle;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class AbstractListener<P extends Plugin> implements Listener {
    Plugin plugin;
    public AbstractListener(P plugin){
        this.plugin = plugin;
    }
    public void register(){
        this.plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }
    public void unRegister(){
        HandlerList.unregisterAll((Listener)this);
    }

    public Plugin getPlugin() {
        return plugin;
    }
}