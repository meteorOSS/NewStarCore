package com.meteor.starcore.data.event;

import com.meteor.starcore.data.plant.Seed;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class CollPlantEvent extends PlayerEvent {
    private static  final  HandlerList handlerList = new HandlerList();
    private Seed seed;
    Player player;
    public CollPlantEvent(Player who,Seed seed) {
        super(who);
        this.player = who;
        this.seed = seed;
    }

    public Player getOwner() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Seed getSeed() {
        return seed;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
