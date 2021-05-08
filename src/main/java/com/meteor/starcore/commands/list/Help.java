package com.meteor.starcore.commands.list;

import com.meteor.starcore.StarCore;
import com.meteor.starcore.commands.Icmd;
import com.meteor.starcore.data.Config;
import org.bukkit.command.CommandSender;

public class Help extends Icmd {
    public Help(StarCore plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "star.help";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public String usage() {
        return "查看帮助";
    }

    @Override
    public void perform(CommandSender p0, String[] p1) {
        Config.getInstance().getMessageManager().getStringList("message.help.player").forEach(s->p0.sendMessage(s));
        if(p0.isOp())
            Config.getInstance().getMessageManager().getStringList("message.help.admin").forEach(s->p0.sendMessage(s));
        return;
    }
}
