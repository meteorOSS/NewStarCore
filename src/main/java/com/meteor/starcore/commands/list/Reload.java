package com.meteor.starcore.commands.list;

import com.meteor.starcore.StarCore;
import com.meteor.starcore.commands.Icmd;
import com.meteor.starcore.data.Config;
import org.bukkit.command.CommandSender;

public class Reload extends Icmd {
    public Reload(StarCore plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "reload";
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
        return "重载配置文件";
    }

    @Override
    public void perform(CommandSender p0, String[] p1) {
        Config.getInstance().reload();
        p0.sendMessage(Config.getInstance().getMessageManager().getString("message.reload"));
        return;
    }
}
