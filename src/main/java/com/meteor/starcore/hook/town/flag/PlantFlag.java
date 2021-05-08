package com.meteor.starcore.hook.town.flag;

import com.meteor.meteortown.flag.FlagManager;
import com.meteor.meteortown.flag.IFlag;
import com.meteor.meteortown.flag.IconItemType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class PlantFlag implements Listener, IFlag {
    @Override
    public String getName() {
        return "plant";
    }

    @Override
    public boolean getDefaultSetting() {
        return false;
    }

    @Override
    public boolean isMemberPer() {
        return true;
    }

    @Override
    public boolean isAgainstPlayer() {
        return true;
    }

    @Override
    public ItemStack getIconItem(boolean b) {
        return b ? FlagManager.flagItem.getIconItem("plant", IconItemType.ENABLE):FlagManager.flagItem.getIconItem("plant",IconItemType.DISABLE);
    }
}
