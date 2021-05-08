package com.meteor.starcore.util;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.starcore.data.Config;
import com.meteor.starcore.data.plant.Kettle;
import com.meteor.starcore.data.plant.PlantData;
import com.meteor.starcore.data.plant.RewardItem;
import com.meteor.starcore.data.plant.Seed;
import com.meteor.starcore.data.time.Season;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class ItemUtil {

    public static ItemStack getItem(ConfigurationSection yamlConfiguration, String path){
        ItemStack itemStack = new ItemStack(Material.valueOf(yamlConfiguration.getString(path+".material","STONE")));
        itemStack.setDurability((short)yamlConfiguration.getInt(path+".durable"));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',yamlConfiguration.getString(path+".name","null")));
        List<String> lore = new ArrayList<>();
        yamlConfiguration.getStringList(path+".lore").forEach(s->lore.add(ChatColor.translateAlternateColorCodes('&',s)));
        itemMeta.setLore(lore);
        if(itemStack.getType()==Material.SKULL_ITEM){
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            GameProfile gf = new GameProfile(UUID.randomUUID(),null);
            gf.getProperties().put("textures",new Property("textures",yamlConfiguration.getString(path+".exay")));
            try {
                Field field = itemMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(itemMeta,gf);
                itemStack.setItemMeta(skullMeta);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public static float getRandom(double d) {
        Random random = new Random();
        Boolean brandom = Boolean.valueOf(random.nextBoolean());
        double value = random.nextFloat() * d;
        return (float)(brandom.booleanValue() ? value : -value);
    }


    public static void initKettle(Player player, Kettle kettle){
        ItemStack itemStack = player.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lores = itemMeta.hasLore()?itemMeta.getLore():new ArrayList<>();
        String lore = Config.getInstance().getMessageManager().getString("format.kettle-lore");
        lore = lore.replace("@number@",kettle.getMaxUseNumber()+"")
        .replace("@max@",kettle.getMaxUseNumber()+"");
        lores.add(lore);
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("lore",lore);
        nbtItem.setInteger("number",kettle.getMaxUseNumber());
        player.setItemInHand(nbtItem.getItem());
    }

    public static boolean isKettleFlow(Player player,Kettle kettle){
        NBTItem nbtItem = new NBTItem(player.getItemInHand());
        return nbtItem.getInteger("number")>=kettle.getMaxUseNumber();
    }

    public static void fullKettle(Player player,Kettle kettle){
        ItemStack  itemStack = player.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lores = itemMeta.getLore();
        NBTItem nbtItem = new NBTItem(itemStack);
        lores.remove(nbtItem.getString("lore"));
        String lore = Config.getInstance().getMessageManager().getString("format.kettle-lore");
        lore = lore.replace("@number@",kettle.getMaxUseNumber()+"");
        lores.add(lore);
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        nbtItem = new NBTItem(itemStack);
        nbtItem.setString("lore",lore);
        nbtItem.setInteger("number",kettle.getMaxUseNumber());
        player.setItemInHand(nbtItem.getItem());
    }

    public static void takeKettleWater(Player player,Kettle kettle){
        ItemStack itemStack = player.getItemInHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lores = itemMeta.getLore();
        NBTItem nbtItem = new NBTItem(itemStack);
        int number = nbtItem.getInteger("number")-1;
        lores.remove(nbtItem.getString("lore"));
        String lore = Config.getInstance().getMessageManager().getString("format.kettle-lore");
        lore = lore.replace("@number@",number+"").replace("@max@",kettle.getMaxUseNumber()+"");
        lores.add(lore);
        itemMeta.setLore(lores);
        itemStack.setItemMeta(itemMeta);
        nbtItem = new NBTItem(itemStack);
        nbtItem.setString("lore",lore);
        nbtItem.setInteger("number",number);
        player.setItemInHand(nbtItem.getItem());
    }

    public static boolean isInitKettle(Player player){
        return new NBTItem(player.getItemInHand()).hasKey("lore");
    }

    public static boolean isWatering(Player player){
        return new NBTItem(player.getItemInHand()).getInteger("number")>0;
    }

    public static RewardItem lottery(List<RewardItem> rewardItemList){
        if(rewardItemList==null||rewardItemList.isEmpty()){
            return null;
        }
        int size = rewardItemList.size();
        double sumChance = 0d;
        for(RewardItem reward : rewardItemList){
            sumChance+=reward.getChance();
        }
        List<Double> sortOrginRates = new ArrayList<Double>(size);
        Double temp = 0d;
        for (RewardItem reward : rewardItemList){
            temp += reward.getChance();
            sortOrginRates.add(temp/sumChance);
        }
        //区块值获取物品索引
        double nextDouble = Math.random();
        sortOrginRates.add(nextDouble);
        Collections.sort(sortOrginRates);
        int num = sortOrginRates.indexOf(nextDouble);
        return rewardItemList.get(num);
    }

    public static ItemStack getMythicItem(RewardItem rewardItem){
        Optional<MythicItem> maybeItem = MythicMobs.inst().getItemManager().getItem(rewardItem.getId());
        if(maybeItem.isPresent()){
            MythicItem mythicItem = maybeItem.get();
            ItemStack itemStack = BukkitAdapter.adapt(mythicItem.generateItemStack(null,1));
            itemStack.setAmount(1);
            itemStack.setAmount(rewardItem.getAmount());
            return itemStack;
        }
        return null;
    }


    public static void checkWilt(Player player,Season currentSeason, Seed seed, PlantData plantData){
        if(!seed.getPlantSeasons().contains(currentSeason)){
            if(player!=null){
                player.sendMessage(Config.getInstance().getMessageManager().getString("message.wilt-plant"));
            }
            return;
        }
    }

    public static boolean getProa(int proa) {
        Random r = new Random();
        int n = r.nextInt(100);
        if (n < proa)
            return true;
        return false;
    }
}
