package com.lenin.warpstonemod.common.items;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class WarpstoneItemGroup extends ItemGroup {
    public WarpstoneItemGroup (String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(WarpItems.WARPSTONE_SHARD.get());
    }
}