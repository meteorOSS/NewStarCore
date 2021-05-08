package com.meteor.starcore.commands;

import com.meteor.starcore.StarCore;
import com.meteor.starcore.data.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public abstract class Icmd {
    protected StarCore plugin;
    public Icmd(StarCore plugin){
        this.plugin = plugin;
    }
    public abstract String label();
    public abstract String getPermission();
    public abstract boolean playersOnly();
    public abstract String usage();
    public List<String> getTab(final Player p, final int i, final String[] args) {
        return Collections.emptyList();
    }
    public abstract void perform(final CommandSender p0, final String[] p1);
    public void execute(CommandSender sender, String[] args){
        if(this.playersOnly()&&!(sender instanceof Player)){
            sender.sendMessage(Config.getInstance().getMessageManager().getString("message.player-only"));
            return;
        }
        if (!hasPerm(sender)) {
            sender.sendMessage(Config.getInstance().getMessageManager().getString("message.no-perm").replace("@perm@",getPermission()));
            return;
        }
        this.perform(sender,args);
    }
    public boolean hasPerm(CommandSender sender) {
        if (this.getPermission() == null) {
            return true;
        }
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            return p.hasPermission(this.getPermission());
        }
        return true;
    }

}
