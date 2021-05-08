package com.meteor.starcore.commands.list;

import com.meteor.starcore.StarCore;
import com.meteor.starcore.commands.Icmd;
import com.meteor.starcore.data.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sickle extends Icmd {
    public Sickle(StarCore plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "sickle";
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
        return "给予玩家镰刀";
    }

    @Override
    public List<String> getTab(Player p, int i, String[] args) {
        switch (i){
            case 1:
                return Arrays.asList("get","give","list");
            case 2:
                if(args[1].equalsIgnoreCase("get")){
                    return new ArrayList<>(Config.getInstance().getSickleMap().keySet());
                }else if(args[1].equalsIgnoreCase("give")){
                    List<String> ps = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(player -> ps.add(player.getName()));
                    return ps;
                }else {
                    return null;
                }
            case 3:
                if(args[1].equalsIgnoreCase("get")){
                    return Arrays.asList("1");
                }else if(args[1].equalsIgnoreCase("give")){
                    return new ArrayList<>(Config.getInstance().getSickleMap().keySet());
                }else{
                    return null;
                }
            case 4:
                if(args[1].equalsIgnoreCase("give")){
                    return Arrays.asList("1");
                }
                return null;
            default:
                return null;
        }
    }

    @Override
    public void perform(CommandSender p0, String[] p1) {
        com.meteor.starcore.data.plant.Sickle sickle = null;
        ItemStack itemStack = null;
        try {
            switch (p1[1]){
                case "get":
                    sickle = Config.getInstance().getSickleMap().get(p1[2]);
                    itemStack = sickle.toItemStack();
                    itemStack.setAmount(Integer.valueOf(p1[3]));
                    ((Player)p0).getInventory().addItem(itemStack);
                    p0.sendMessage(Config.getInstance().getMessageManager().getString("message.get").replace("@item@",sickle.getName()).replace("@amount@",p1[3]));
                    return;
                case "give":
                    Player p = Bukkit.getPlayerExact(p1[2]);
                    if(p==null){
                        p0.sendMessage(Config.getInstance().getMessageManager().getString("message.no-online"));
                        return;
                    }
                    sickle = Config.getInstance().getSickleMap().get(p1[3]);
                    itemStack = sickle.toItemStack();
                    itemStack.setAmount(Integer.valueOf(p1[4]));
                    p.getInventory().addItem(itemStack);
                    p0.sendMessage(Config.getInstance().getMessageManager().getString("message.give").replace("@item@",sickle.getName()).replace("@amount@",p1[4])
                            .replace("@p@",p.getName()));
                    return;
                case "list":
                    StringBuilder stringBuilder = new StringBuilder();
                    Config.getInstance().getSickleMap().forEach((k,v)->{
                        String s = ChatColor.translateAlternateColorCodes('&',v.getKey()+"&f("+v.getName()+"&f)");
                        stringBuilder.append(s+",");
                    });
                    String message = stringBuilder.toString();
                    message = message.substring(0,message.length()-1);
                    p0.sendMessage(message);
                    return;
                default:
                    p0.sendMessage(Config.getInstance().getMessageManager().getString("message.error"));
                    return;
            }
        }catch (Exception e){
            e.printStackTrace();
            p0.sendMessage(Config.getInstance().getMessageManager().getString("message.error"));
        }
    }
}
