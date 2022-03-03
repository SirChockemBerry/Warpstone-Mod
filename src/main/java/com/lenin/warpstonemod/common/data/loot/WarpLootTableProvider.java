package com.lenin.warpstonemod.common.data.loot;

import com.google.common.collect.ImmutableList;
import com.lenin.warpstonemod.common.Registration;
import com.lenin.warpstonemod.common.blocks.WarpBlocks;
import com.lenin.warpstonemod.common.items.WarpItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WarpLootTableProvider extends LootTableProvider {

    public WarpLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(
                Pair.of(WarpBlockLootTables::new, LootParameterSets.BLOCK)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationTracker) {
        map.forEach((resLocation, lootTable) -> LootTableManager.validateLootTable(validationTracker, resLocation, lootTable));
    }

    public static class WarpBlockLootTables extends BlockLootTables {

        @Override
        protected void addTables (){
            registerDropSelfLootTable(WarpBlocks.WARPSTONE_BLOCK);

            registerLootTable(WarpBlocks.WARPSTONE_ORE, (warpstoneOre) -> droppingWithSilkTouch(warpstoneOre,
                    withExplosionDecay(warpstoneOre, ItemLootEntry.builder(WarpItems.WARPSTONE_SHARD)
                            .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 3.0F)))
                            .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks(){
            return new ArrayList<>(Registration.BLOCKS);
        }
    }
}