package com.meteor.starcore.data.event;

import com.meteor.starcore.data.plant.Seed;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlantSeedEvent extends PlayerEvent {
    private static  final  HandlerList handlerList = new HandlerList();

    private Seed seed;

    public PlantSeedEvent(Player who, Seed seed) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Seed getSeed() {
        return seed;
    }
}
