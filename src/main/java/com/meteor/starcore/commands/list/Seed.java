package com.meteor.starcore.commands.list;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.starcore.StarCore;
import com.meteor.starcore.commands.Icmd;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Seed extends Icmd {
    public Seed(StarCore plugin) {
        super(plugin);
    }

    @Override
    public String label() {
        return "seed";
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
        return "给予植物种子";
    }

    @Override
    public List<String> getTab(Player p, int i, String[] args) {
        switch (i){
            case 1:
                return Arrays.asList("get","give","list");
            case 2:
                if(args[1].equalsIgnoreCase("get")){
                    return new ArrayList<>(Config.getInstance().getSeedMap().keySet());
                }else if(args[1].equalsIgnoreCase("give")){
                    List<String> ps = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(player -> ps.add(player.getName()));
                    return ps;
                }else {
                    return null;
                }
            case 3:
                if(args[1].equalsIgnoreCase("get")){
                    return Arrays.asList("1","10");
                }else if(args[1].equalsIgnoreCase("give")){
                    return new ArrayList<>(Config.getInstance().getSeedMap().keySet());
                }else{
                    return null;
                }
            case 4:
                if(args[1].equalsIgnoreCase("give")){
                    return Arrays.asList("1","10");
                }
                return null;
            default:
                return null;
        }
    }


    private ItemStack toItemStack(com.meteor.starcore.data.plant.Seed seed){
        ItemStack metaItem = seed.getMetaItem();
        ItemStack item = ItemUtil.getItem(Config.getInstance().getMessageYaml(),"format.seed");
        ItemStack itemStack = metaItem.getType()== Material.STONE?item.clone():metaItem.clone();

        List<String> lore = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<seed.getPlantSeasons().size();i++){
            stringBuilder.append(seed.getPlantSeasons().get(i).getName()+
                    (i!=seed.getPlantSeasons().size()-1?",":""));
        }
        item.getItemMeta().getLore().forEach(string ->{
            if(string.equalsIgnoreCase("@present@")){
                metaItem.getItemMeta().getLore().forEach(l->lore.add(ChatColor.translateAlternateColorCodes('&',l)));
                return;
            }
            string = string.replace("@day@",seed.getTotalDay()+"")
                    .replace("@season@",stringBuilder.toString());
            lore.add(ChatColor.translateAlternateColorCodes('&',string));
        });

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(item.getItemMeta().getDisplayName().replace("@seed@",seed.getName()));
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setBoolean("isSeedItem",true);
        nbtItem.setString("seed",seed.getKey());

        return nbtItem.getItem();
    }



    @Override
    public void perform(CommandSender p0, String[] p1) {
        com.meteor.starcore.data.plant.Seed seed = null;
        ItemStack itemStack = null;
        try {
            switch (p1[1]){
                case "get":
                    seed = Config.getInstance().getSeedMap().get(p1[2]);
                    itemStack = toItemStack(seed);
                    itemStack.setAmount(Integer.valueOf(p1[3]));
                    ((Player)p0).getInventory().addItem(itemStack);
                    p0.sendMessage(Config.getInstance().getMessageManager().getString("message.get").replace("@item@",seed.getName()).replace("@amount@",p1[3]));
                    return;
                case "give":
                    Player p = Bukkit.getPlayerExact(p1[2]);
                    if(p==null){
                        p0.sendMessage(Config.getInstance().getMessageManager().getString("message.no-online"));
                        return;
                    }
                    seed = Config.getInstance().getSeedMap().get(p1[3]);
                    itemStack = toItemStack(seed);
                    itemStack.setAmount(Integer.valueOf(p1[4]));
                    p.getInventory().addItem(itemStack);
                    p0.sendMessage(Config.getInstance().getMessageManager().getString("message.give").replace("@item@",seed.getName()).replace("@amount@",p1[4])
                    .replace("@p@",p.getName()));
                    return;
                case "list":
                    StringBuilder stringBuilder = new StringBuilder();
                    Config.getInstance().getSeedMap().forEach((k,v)->{
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
