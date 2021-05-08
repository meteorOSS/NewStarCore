package com.meteor.starcore.commands.list;

import com.meteor.starcore.StarCore;
import com.meteor.starcore.commands.Icmd;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.data.PlayerPlantData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Ref extends Icmd {
    public Ref(StarCore plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "ref";
    }

    @Override
    public String getPermission() {
        return "star.admin";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public String usage() {
        return "刷新玩家种植数量";
    }

    @Override
    public List<String> getTab(Player p, int i, String[] args) {
        List<String> ps = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player->ps.add(player.getName()));
        return ps;
    }

    @Override
    public void perform(CommandSender p0, String[] p1) {
        Player player = Bukkit.getPlayerExact(p1[1]);
        if(player!=null){
            PlayerPlantData playerPlantData = plugin.getPlantManager().getPlayerData(player.getName());
            playerPlantData.setAmount(plugin.getPlantManager().getPlayerPlantAmount(player.getName()));
            p0.sendMessage(Config.getInstance().getMessageManager().getString("message.ref-sur"));
        }else{
            p0.sendMessage(Config.getInstance().getMessageManager().getString("message.no-online"));
        }
    }
}
