package com.meteor.starcore.data;

import com.meteor.meteorlib.nbt.NBTItem;
import com.meteor.meteorlib.util.EffectUtil;
import com.meteor.meteorpoints.MeteorPoints;
import com.meteor.meteorpoints.data.*;
import com.meteor.meteortown.MeteorTown;
import com.meteor.meteortown.data.town.Town;
import com.meteor.meteortown.flag.FlagManager;
import com.meteor.starcore.AbstractListener;
import com.meteor.starcore.StarCore;
import com.meteor.starcore.data.event.CollPlantEvent;
import com.meteor.starcore.data.event.PlantSeedEvent;
import com.meteor.starcore.data.event.WaterPlantEvent;
import com.meteor.starcore.data.plant.*;
import com.meteor.starcore.data.time.Season;
import com.meteor.starcore.hook.town.flag.PlantFlag;
import com.meteor.starcore.util.ItemUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PlantManager extends AbstractListener<StarCore> {
    StarCore plugin;
    Map<UUID, PlantData> plantDataMap;
    PointsManager pointsManager;
    Map<String,PlayerPlantData> playerPlantDataMap;
    boolean hookTown;
    public PlantManager(StarCore plugin) {
        super(plugin);
        this.plugin = plugin;
        plantDataMap = new HashMap<>();
        loadData();
        pointsManager = MeteorPoints.Instance.getPointsManager();
        playerPlantDataMap = new HashMap<>();
        if(Bukkit.getPluginManager().getPlugin("MeteorTown")!=null){
            hookTown = true;
            FlagManager.registerFlag(new PlantFlag());
            plugin.getLogger().info("已hook城镇");
        }
    }

    public int getPlayerPlantAmount(String player){
        AtomicInteger i = new AtomicInteger();
        plantDataMap.values().forEach(plantData -> {
            i.set(plantData.getOwner().equalsIgnoreCase(player)&&plantData.getLocation().getBlock()!=null&&plantData.getLocation().getBlock().getType()!=Material.AIR&&!plantData.isWilt(plugin.getTimeManager().getTimeInfo().getSeason())?
                    i.get()+1:i.get());
        });
        return i.get();
    }

    public PlayerPlantData getPlayerData(String player){
        if(!playerPlantDataMap.containsKey(player)){
            File f = new File(plugin.getDataFolder()+"/pdata/"+player+".yml");
            PlayerPlantData playerPlantData = f.exists()?new PlayerPlantData(YamlConfiguration.loadConfiguration(f)):new PlayerPlantData(player,getPlayerPlantAmount(player));
            playerPlantData.setAmount(getPlayerPlantAmount(player));
            playerPlantDataMap.put(player,playerPlantData);
        }
        return playerPlantDataMap.get(player);
    }

    private void packBlock(UUID uuid,Block block){
        block.setMetadata("isPlantBlock",new FixedMetadataValue(plugin,true));
        block.setMetadata("uuid",new FixedMetadataValue(plugin,uuid.toString()));
    }

    public void loadData(){
        long current = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin,()->{
            File file = new File(plugin.getDataFolder()+"/plantData.yml");
            if(file.exists()){
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                yamlConfiguration.getKeys(false).forEach(key->{
                    PlantData plantData = new PlantData(yamlConfiguration.getConfigurationSection(key));
                    plantDataMap.put(UUID.fromString(key),plantData);
                    Bukkit.getScheduler().runTask(plugin,()->{
                        Block block = plantData.getLocation().getBlock();
                        if(block!=null&&block.getType()!=Material.AIR&&block.getType()==Material.SKULL){
                            block.setMetadata("isPlantBlock",new FixedMetadataValue(plugin,true));
                            block.setMetadata("uuid",new FixedMetadataValue(plugin,key));
                        }
                    });
                });
            }
            long lastTime = (System.currentTimeMillis()-current);
            plugin.getLogger().info("载入"+plantDataMap.values().size()+"条种植数据,消耗"+lastTime+"毫秒");
        });

    }



    public void saveData(){
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        plantDataMap.forEach((k,v)->{
            if(v.getLocation().getBlock()!=null&&v.getLocation().getBlock().getType()!=Material.AIR&&!v.isHarvest())
                v.saveData(yamlConfiguration,k);
        });
        try {
            yamlConfiguration.save(new File(plugin.getDataFolder()+"/plantData.yml"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        playerPlantDataMap.values().forEach(playerPlantData->{
            try {
                playerPlantData.toYaml().save(new File(plugin.getDataFolder()+"/pdata/"+playerPlantData.getPlayerName()+".yml"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }


    public void autoSaveData(){
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        plantDataMap.forEach((k,v)->{
            if(v.getLocation().getBlock()!=null&&v.getLocation().getBlock().getType()!=Material.AIR&&!v.isWilt(plugin.getTimeManager().getTimeInfo().getSeason())&&!v.isHarvest())
                v.saveData(yamlConfiguration,k);
        });
        try {
            yamlConfiguration.save(new File(plugin.getDataFolder()+"/backup/"+System.currentTimeMillis()+".yml"));
            plugin.getLogger().info("已备份数据至"+plugin.getDataFolder()+"/backup/"+System.currentTimeMillis()+".yml");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }




    public boolean isCheckPlant(Player p){
        double playerx = p.getLocation().getX();
        double playery = p.getLocation().getY();
        double playerz = p.getLocation().getZ();
        for(int blockx = (int) (playerx-3); blockx<playerx+3; blockx++){
            for(int blocky = (int) (playery-3); blocky<playery+3; blocky++){
                for(int blockz = (int) (playerz-3); blockz<playerz+3; blockz++){
                    if((diff(blockx, (int) playerx)^2+diff(blocky, (int) playery)^2+diff(blockz, (int) playerz)^2)<900){
                        //this block is within.
                        try {
                            Block block = new Location(p.getWorld(),blockx,blocky,blockz).getBlock();
                            if(block.getType()!=Material.AIR&&block.hasMetadata("isPlantBlock")){
                                PlantData plantData = plantDataMap.get(UUID.fromString(block.getMetadata("uuid").get(0).asString()));
                                if(plantData!=null&&plantData.isMellow(Config.getInstance().getSeedMap().get(plantData.getSeedKey()))){
                                    Location loc = block.getLocation();
                                    String efftype = Config.getInstance().getConfig().getString("plant-mellow.effect.type");
                                    EffectUtil.playEffect(p, efftype,(float)loc.getX()+ItemUtil.getRandom(0.22D),(float)loc.getY()+ItemUtil.getRandom(0.3D),(float)loc.getZ()+ItemUtil.getRandom(0.23D),0F,0F,0F,1,Config.getInstance().getConfig().getInt("plant-mellow.effect.amount"));
                                }
                            }
                        }catch (ArrayIndexOutOfBoundsException e){

                        }
                    }
                }
            }
        }
        return true;
    }

    public int diff(int blockx,int playerx){
        return blockx-playerx;
    }

    public Map<UUID, PlantData> getPlantDataMap() {
        return plantDataMap;
    }

    @EventHandler
    void onMove(PlayerMoveEvent moveEvent){
        isCheckPlant(moveEvent.getPlayer());
    }




    @EventHandler
    void onWatering(PlayerInteractEvent interactEvent){
        if(interactEvent.getHand()!=EquipmentSlot.HAND)
            return;
        if(interactEvent.hasItem()&&interactEvent.hasBlock()&&interactEvent.getAction()== Action.RIGHT_CLICK_BLOCK){
            Block block = interactEvent.getClickedBlock();
            ItemStack itemStack = interactEvent.getItem();
            NBTItem nbtItem = new NBTItem(itemStack);
            Player p = interactEvent.getPlayer();
            Kettle kettle = Config.getInstance().getKettleMap().get(nbtItem.getString("kettle"));
            if(block.hasMetadata("isPlantBlock")&&nbtItem.hasKey("isKettle")&&kettle!=null){
                UUID uuid = UUID.fromString(block.getMetadata("uuid").get(0).asString());
                PlantData plantData = plantDataMap.get(uuid);
                Seed seed = Config.getInstance().getSeedMap().get(plantData.getSeedKey());
                if(!ItemUtil.isInitKettle(p))
                    ItemUtil.initKettle(p,kettle);
                if(ItemUtil.isWatering(p)){
                    PlayerData playerData = pointsManager.getPlayerDataMap().get(p.getName());
                    Skill skill = pointsManager.getSkillTypeSkillMap().get(SkillType.PLANT);
                    SkillData skillData = playerData.getSkillDataMap().get(SkillType.PLANT);

                    if(pointsManager.isDay(playerData,skill.getExp(),SkillType.PLANT)){
                        if(!pointsManager.isTake(playerData,skill.getTakeBody())){
                            p.sendMessage(Config.getInstance().getMessageManager().getString("message.no-body"));
                            return;
                        }
                        if(skillData.getTotalDay()<skillData.getLimit()){
                            pointsManager.levelUp(playerData,skillData,skill);
                        }
                    }
                    if(plantData.isCanWater()){
                        if(isPlantWilt(plantData,p,block)){
                            return;
                        }
                        ItemUtil.takeKettleWater(p,kettle);
                        boolean isNext = plantData.waterPlant(seed);
                        packEffect(p,"water-plant.effect",block.getLocation());
                        Bukkit.getPluginManager().callEvent(new WaterPlantEvent(p));
                        if(isNext){
                            //浇水完成..进入新阶段或成熟
                            if(plantData.isMellow(seed)){
                                p.sendMessage(Config.getInstance().getMessageManager().getString("message.mellow")
                                        .replace("@seed@",seed.getName()));
                            }else{
                                p.sendMessage(Config.getInstance().getMessageManager().getString("message.next-stage")
                                .replace("@stage@",plantData.getStage()+"")
                                        .replace("@seed@",seed.getName())
                                .replace("@max@",seed.getMellowStage()+""));
                            }
                        }else{
                            //浇水完成,未进入新阶段
                            StageData stageData = seed.getStageDataMap().get(plantData.getStage());
                            p.sendMessage(Config.getInstance().getMessageManager().getString("message.next-day")
                                    .replace("@seed@",seed.getName())
                            .replace("@day@",stageData.getDay()-plantData.getDay()+""));
                        }
                    }else{
                        //当日已经浇水过了..
                        p.sendMessage(Config.getInstance().getMessageManager().getString("message.last-cd")
                        .replace("@seed@",seed.getName()));
                    }
                }else{
                    //没有足够的水
                    p.sendMessage(Config.getInstance().getMessageManager().getString("message.no-water"));
                }
            }
        }
    }


    public boolean isPassTown(Block block,Player p){
        Town town = MeteorTown.Instance.getTownManager().getTown(block.getLocation());
        if(town==null)
            return true;
        if(town.isPermEnable("plant", false)||town.getOwnerUUID().equals(p.getUniqueId())||town.isHasPerm(p.getName(),"plant",false)){
            return true;
        }else{
            return false;
        }
    }

    @EventHandler
    void onHarvest(PlayerInteractEvent interactEvent){
        if(interactEvent.getHand()!=EquipmentSlot.HAND)
            return;
        if(interactEvent.hasItem()&&interactEvent.hasBlock()){
            Block block = interactEvent.getClickedBlock();
            ItemStack itemStack = interactEvent.getItem();
            Player p = interactEvent.getPlayer();
            if(block.hasMetadata("isPlantBlock")){
                UUID uuid = UUID.fromString(block.getMetadata("uuid").get(0).asString());
                NBTItem nbtItem = new NBTItem(itemStack);
                if(plantDataMap.containsKey(uuid)&&nbtItem.hasKey("isSickle")){
                    if(hookTown&&!isPassTown(block,p)){
                        interactEvent.setCancelled(true);
                        p.sendMessage(Config.getInstance().getMessageManager().getString("message.town-perm"));
                        return;
                    }
                    Sickle sickle = Config.getInstance().getSickleMap().get(nbtItem.getString("sickle"));
                    PlantData plantData = plantDataMap.get(uuid);
                    if(isPlantWilt(plantData,p,block))
                        return;
                    Seed seed = Config.getInstance().getSeedMap().get(plantData.getSeedKey());
                    if(plantData.isMellow(seed)){


                        PlayerData playerData = pointsManager.getPlayerDataMap().get(p.getName());
                        Skill skill = pointsManager.getSkillTypeSkillMap().get(SkillType.PLANT);
                        SkillData skillData = playerData.getSkillDataMap().get(SkillType.PLANT);

                        if(pointsManager.isDay(playerData,skill.getExp(),SkillType.PLANT)){
                            if(!pointsManager.isTake(playerData,skill.getTakeBody())){
                                p.sendMessage(Config.getInstance().getMessageManager().getString("message.no-body"));
                                return;
                            }
                            if(skillData.getTotalDay()<skillData.getLimit()){
                                pointsManager.levelUp(playerData,skillData,skill);
                            }
                        }


                        plantData.getLocation().getBlock().setType(Material.AIR);
                        PlayerPlantData playerPlantData = getPlayerData(p.getName());
                        playerPlantData.takeAmount();
                        Fruit fruit = Config.getInstance().getFruitMap().get(seed.getFruit());
                        RewardItem rewardItem = ItemUtil.lottery(seed.getRewardList());
                        int amount = rewardItem.getAmount();
                        Bukkit.getPluginManager().callEvent(new CollPlantEvent(p,seed));

                        RewardItem sickleReward = ItemUtil.lottery(sickle.getRewardItems());
                        amount+=sickleReward!=null?sickleReward.getAmount():0;

                        ItemStack fruitItem = fruit.toItemStack(FruitQuality.lottery(0,0)).clone();
                        fruitItem.setAmount(amount);
                        Location loc = plantData.getLocation();
                        String efftype = Config.getInstance().getConfig().getString("sickle-reward.effect.type");
                        EffectUtil.playEffect(p, efftype,(float)loc.getX()+ItemUtil.getRandom(0.2D),(float)loc.getY()+ItemUtil.getRandom(0.2D),(float)loc.getZ()+ItemUtil.getRandom(0.2D),0F,0F,0F,1,Config.getInstance().getConfig().getInt("sickle-reward.effect.amount"));
                        p.playSound(p.getLocation(), Sound.valueOf(Config.getInstance().getConfig().getString("sickle-reward.effect.sound")),2F,2F);
                        if(sickleReward!=null){
                            p.sendMessage(Config.getInstance().getMessageManager().getString("message.sickle-reward")
                            .replace("@reward@",sickleReward.getName())
                            .replace("@add@",sickleReward.getAmount()+"")
                            .replace("@amount@",amount+"")
                            .replace("@seed@",seed.getName()));
                        }else{
                            EffectUtil.playEffect(p,"SLIME",2F,2F,2F,2F,2F,2F,5,35);
                            p.sendMessage(Config.getInstance().getMessageManager().getString("message.mellow-plant")
                            .replace("@seed@",seed.getName())
                            .replace("@amount@",amount+""));
                        }
                        p.getInventory().addItem(fruitItem);
                        plantData.setHarvest(true);
                        plantDataMap.remove(uuid);
                    }else{
                        StageData stageData = seed.getStageDataMap().get(plantData.getStage());
                        p.sendMessage(Config.getInstance().getMessageManager().getString("message.last-next")
                        .replace("@seed@",seed.getName())
                                .replace("@stage@",plantData.getStage()+"")
                                .replace("@max@",seed.getMellowStage()+"")
                        .replace("@day@",stageData.getDay()-plantData.getDay()+""));
                    }
                }
            }
        }
    }

    private void packEffect(Player p,String path,Location loc){
        String efftype = Config.getInstance().getConfig().getString(path+".type");
        EffectUtil.playEffect(p, efftype,(float)loc.getX()+ItemUtil.getRandom(0.2D),(float)loc.getY()+ItemUtil.getRandom(0.2D),(float)loc.getZ()+ItemUtil.getRandom(0.2D),0F,0F,0F,1,Config.getInstance().getConfig().getInt(path+".amount"));
        p.playSound(p.getLocation(), Sound.valueOf(Config.getInstance().getConfig().getString(path+".sound")),2F,2F);
    }


    @EventHandler
    void onBreak(BlockBreakEvent breakEvent){
        Block block = breakEvent.getBlock();
        Location loc = block.getLocation();
        loc = new Location(loc.getWorld(),loc.getX(),loc.getY()+1,loc.getZ());
        Block plant = loc.getBlock();
        if((loc.getBlock().getType()!=Material.AIR)&&plant!=null&&plant.hasMetadata("isPlantBlock")){
            breakEvent.setCancelled(true);
        }
    }


    @EventHandler
    void onBreakPlant(BlockBreakEvent breakEvent){
        Block block = breakEvent.getBlock();
        if(block!=null&&block.hasMetadata("isPlantBlock")){
            UUID uuid = UUID.fromString(block.getMetadata("uuid").get(0).asString());
            Player p = breakEvent.getPlayer();
            if(plantDataMap.containsKey(uuid)){
                breakEvent.setDropItems(false);
                PlantData plantData = plantDataMap.get(uuid);
                packEffect(p,"break-plant.effect",block.getLocation());
                PlayerPlantData playerPlantData = getPlayerData(p.getName());
                playerPlantData.takeAmount();
                String title = Config.getInstance().getMessageManager().getString("message.break-seed.title");
                String subtitle = Config.getInstance().getMessageManager().getString("message.break-seed.subtitle");
                p.sendTitle(title.replace("@seed@",plantData.getSeedKey()),subtitle.replace("@seed@",plantData.getSeedKey()));
            }
        }
    }

    @EventHandler
    void onPlant(BlockPlaceEvent placeEvent){
        if(placeEvent.getHand()!= EquipmentSlot.HAND){
            return;
        }
        ItemStack itemStack = placeEvent.getItemInHand();
        NBTItem nbtItem = new NBTItem(itemStack);
        Player p = placeEvent.getPlayer();
        if(nbtItem.hasKey("isSeedItem")){
            Block block = placeEvent.getBlock();
            Location downBlockLocation = block.getLocation();
            downBlockLocation = new Location(downBlockLocation.getWorld(),downBlockLocation.getX(),
                    downBlockLocation.getY()-1,downBlockLocation.getZ());
            if(downBlockLocation.getBlock().getType()!=Material.SOIL){
                placeEvent.setCancelled(true);
                p.sendMessage(Config.getInstance().getMessageManager().getString("message.no-dirt"));
                return;
            }
            Season season = plugin.getTimeManager().getTimeInfo().getSeason();
            Seed seed = Config.getInstance().getSeedMap().get(nbtItem.getString("seed"));
            if(seed==null){
                placeEvent.setCancelled(true);
                p.sendMessage(Config.getInstance().getMessageManager().getString("message.no-seed"));
                return;
            }else if(!seed.getPlantSeasons().contains(season)){
                placeEvent.setCancelled(true);
                p.sendMessage(Config.getInstance().getMessageManager().getString("message.deny-season").replace("@season@",season.getName()));
                return;
            }
            PlantData plantData = new PlantData(seed.getKey(),block.getLocation(),season,p.getName());
            PlayerPlantData playerPlantData = getPlayerData(p.getName());
            if(playerPlantData.getAmount()>=playerPlantData.getLimit()){
                placeEvent.setCancelled(true);
                p.sendMessage(Config.getInstance().getMessageManager().getString("message.plant-limit")
                .replace("@plant@",String.valueOf(playerPlantData.getAmount()))
                .replace("@limit@",String.valueOf(playerPlantData.getLimit())));
                return;
            }
            playerPlantData.setAmount(playerPlantData.getAmount()+1);
            packEffect(p,"plant.effect",block.getLocation());
            UUID uuid = UUID.randomUUID();
            plantDataMap.put(uuid,plantData);
            Bukkit.getPluginManager().callEvent(new PlantSeedEvent(p,seed));
            packBlock(uuid,placeEvent.getBlock());
            String title = Config.getInstance().getMessageManager().getString("message.plant-seed.title");
            String subtitle = Config.getInstance().getMessageManager().getString("message.plant-seed.subtitle");
            p.sendTitle(title.replace("@seed@",seed.getName()),subtitle.replace("@seed@",seed.getName()));
        }
    }


    private boolean isPlantWilt(PlantData plantData,Player p,Block block){
        if(plantData.isWilt(plugin.getTimeManager().getTimeInfo().getSeason())){
            block.setType(Material.AIR);
            packEffect(p,"wilt-plant.effect",block.getLocation());
            PlayerPlantData playerPlantData = getPlayerData(p.getName());
            playerPlantData.takeAmount();
            p.sendMessage(Config.getInstance().getMessageManager().getString("message.plant-wilt")
                    .replace("@seed@",plantData.getSeedKey()));
            return true;
        }
        return false;
    }

    @EventHandler
    void onMuck(PlayerInteractEvent interactEvent){
        if(interactEvent.getHand()!=EquipmentSlot.HAND)
            return;
        if(interactEvent.hasBlock()&&interactEvent.hasItem()){
            Block block = interactEvent.getClickedBlock();
            ItemStack itemStack = interactEvent.getItem();
            NBTItem nbtItem = new NBTItem(itemStack);
            if(interactEvent.getAction()==Action.RIGHT_CLICK_BLOCK&&block.hasMetadata("isPlantBlock")
            &&nbtItem.hasKey("isMuck")){
                UUID uuid = UUID.fromString(block.getMetadata("uuid").get(0).asString());
                PlantData plantData = plantDataMap.get(uuid);
                Player p = interactEvent.getPlayer();
                if(isPlantWilt(plantData,p,block))
                    return;
                Muck muck = Config.getInstance().getMuckMap().get(nbtItem.getString("muck"));
                if(plantData!=null&&muck!=null){
                    Seed seed = Config.getInstance().getSeedMap().get(plantData.getSeedKey());
                    if(plantData.isMellow(seed)){
                        p.sendMessage(Config.getInstance().getMessageManager().getString("message.mellow")
                                .replace("@seed@",seed.getName()));
                        return;
                    }
                    boolean speed = plantData.speedPlant(muck.getSpeedDay(),seed);
                    itemStack.setAmount(itemStack.getAmount()-1);
                    packEffect(p,"use-muck.effect",block.getLocation());
                    if(speed){
                        if(plantData.isMellow(seed)){
                            p.sendMessage(Config.getInstance().getMessageManager().getString("message.mellow")
                                    .replace("@seed@",seed.getName()));
                        }else{
                            p.sendMessage(Config.getInstance().getMessageManager().getString("message.muck-seed")
                                    .replace("@stage@",plantData.getStage()+"")
                                    .replace("@seed@",seed.getName())
                                    .replace("@max@",seed.getMellowStage()+""));
                        }
                    }else{
                        StageData stageData = seed.getStageDataMap().get(plantData.getStage());
                        p.sendMessage(Config.getInstance().getMessageManager().getString("message.muck-day")
                                .replace("@seed@",seed.getName())
                                .replace("@day@",stageData.getDay()-plantData.getDay()+""));
                    }
                }
            }
        }
    }


    @EventHandler
    void onQuitSaveData(PlayerQuitEvent quitEvent){
        String name = quitEvent.getPlayer().getName();
        if(playerPlantDataMap.containsKey(name)){
            try {
                getPlayerData(name).toYaml().save(new File(plugin.getDataFolder()+"/pdata/"+name+".yml"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @EventHandler
    void onEatFruit(PlayerInteractEvent interactEvent){
        if(interactEvent.getHand()!=EquipmentSlot.HAND)
            return;
        if(interactEvent.hasItem()&&interactEvent.getAction()==Action.RIGHT_CLICK_AIR){
            ItemStack itemStack = interactEvent.getItem();
            NBTItem nbtItem = new NBTItem(itemStack);
            Player p = interactEvent.getPlayer();
            if(nbtItem.hasKey("isFruit")){
                String key = nbtItem.getString("fruit");
                Fruit fruit = Config.getInstance().getFruitMap().get(key);
                boolean isOp = p.isOp();
                if(fruit!=null){
                    p.setOp(true);
                    fruit.getCmds().forEach(cmd-> p.performCommand(cmd.replace("@p@",p.getName())));
                    itemStack.setAmount(itemStack.getAmount()-1);
                    if(!isOp)
                        p.setOp(false);
                }
            }
        }
    }

    public void refreWaterMode(){
        plantDataMap.values().forEach(plantData -> plantData.setWaterPlant(false));
    }


    public boolean isWater(Location location){
        double playerx = location.getX();
        double playery = location.getY();
        double playerz = location.getZ();
        for(int blockx = (int) (playerx-3); blockx<playerx+3; blockx++){
            for(int blocky = (int) (playery-3); blocky<playery+3; blocky++){
                for(int blockz = (int) (playerz-3); blockz<playerz+3; blockz++){
                    if((diff(blockx, (int) playerx)^2+diff(blocky, (int) playery)^2+diff(blockz, (int) playerz)^2)<900){
                        //this block is within.
                        try {
                            Block block = new Location(location.getWorld(),blockx,blocky,blockz).getBlock();
                            if(block.getType()==Material.STATIONARY_WATER||block.getType()==Material.WATER_BUCKET||block.getType()==Material.WATER_LILY){
                                return true;
                            }
                        }catch (ArrayIndexOutOfBoundsException e){

                        }
                    }
                }
            }
        }
        return false;
    }



    private boolean isFoodSur(int pro){
        Random r = new Random();
        int n = r.nextInt(100);
        if (n < pro)
            return true;
        return false;
    }

    @EventHandler
    void onBreakFood(BlockBreakEvent breakEvent){
        if(plugin.getConfig().getBoolean("food.enable",true)){
            if(isFoodSur(plugin.getConfig().getInt("food.proa"))){
                Block block = breakEvent.getBlock();
                FoodData foodData = Config.getInstance().getFoodDataMap().get(block.getType().toString());
                int data = Integer.valueOf(block.getData()+"");
                Player p = breakEvent.getPlayer();
                if(foodData!=null){
                    List<RewardItem> rewardItems = foodData.getRewardList(data,plugin.getTimeManager().getTimeInfo().getSeason());
                    if(rewardItems!=null){
                        breakEvent.setDropItems(false);
                        PlayerData playerData = pointsManager.getPlayerDataMap().get(p.getName());
                        Skill skill = pointsManager.getSkillTypeSkillMap().get(SkillType.FOOD);
                        SkillData skillData = playerData.getSkillDataMap().get(SkillType.FOOD);

                        if(pointsManager.isDay(playerData,skill.getExp(),SkillType.FOOD)){
                            if(skillData.getTotalDay()<skillData.getLimit()&&pointsManager.isTake(playerData,skill.getTakeBody())){
                                pointsManager.levelUp(playerData,skillData,skill);
                            }else{
                                return;
                            }
                        }

                        RewardItem rewardItem = ItemUtil.lottery(rewardItems);

                        Fruit fruit = Config.getInstance().getFruitMap().get(rewardItem.getId());

                        ItemStack itemStack = fruit.toItemStack(FruitQuality.lottery(0,0)).clone();
                        itemStack.setAmount(rewardItem.getAmount());
                        p.getInventory().addItem(itemStack);
                        String title = Config.getInstance().getMessageManager().getString("message.food.title");
                        String subtitle = Config.getInstance().getMessageManager().getString("message.food.subtitle");

                        p.sendTitle(title.replace("@food@",rewardItem.getName()),subtitle.replace("@food@",rewardItem.getName()));
                    }
                }
            }
        }
    }



    @EventHandler
    void noPlaceFruit(BlockPlaceEvent placeEvent){
        ItemStack itemStack = placeEvent.getItemInHand();
        if(itemStack!=null&&(new NBTItem(itemStack)).hasKey("isFruit")){
            placeEvent.getPlayer().sendMessage(Config.getInstance().getMessageManager().getString("message.right-air"));
            placeEvent.setCancelled(true);
        }
    }

    @EventHandler
    void onInputWater(PlayerInteractEvent interactEvent){
        if(interactEvent.getHand()!=EquipmentSlot.HAND)
            return;
        if(interactEvent.hasItem()&&interactEvent.getAction()==Action.RIGHT_CLICK_BLOCK){
            Block block = interactEvent.getClickedBlock();
            Player p = interactEvent.getPlayer();
            if(p.isSneaking()){
                ItemStack itemStack = interactEvent.getItem();
                NBTItem nbtItem = new NBTItem(itemStack);
                Location loc = block.getLocation();
                packEffect(p,"input-water.effect",block.getLocation());
                if(nbtItem.hasKey("isKettle")){
                    boolean isWater = isWater(loc);
                    if(isWater){
                        String efftype = Config.getInstance().getConfig().getString("input-water.effect.type");
                        EffectUtil.playEffect(p, efftype,(float)loc.getX()+ItemUtil.getRandom(0.2D),(float)loc.getY()+ItemUtil.getRandom(0.2D),(float)loc.getZ()+ItemUtil.getRandom(0.2D),0F,0F,0F,1,Config.getInstance().getConfig().getInt("input-water.effect.amount"));
                        p.playSound(p.getLocation(), Sound.valueOf(Config.getInstance().getConfig().getString("input-water.effect.sound")),2F,2F);
                        Kettle kettle = Config.getInstance().getKettleMap().get(nbtItem.getString("kettle"));
                        p.setItemInHand(kettle.toItemStack());
                        ItemUtil.initKettle(p,kettle);
                        p.sendMessage(Config.getInstance().getMessageManager().getString("message.input-water"));
                    }
                }

            }
        }
    }
}
