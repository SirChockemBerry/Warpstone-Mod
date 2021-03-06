package com.lenin.warpstonemod.common.mutations.effect_mutations.mutations;

import com.lenin.warpstonemod.common.mutations.MutateManager;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutation;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutations;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Foods;
import net.minecraft.item.Rarity;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.*;

public class HerbivoreMutation extends EffectMutation {
    public HerbivoreMutation(int _id) {
        super(_id,
                "herbivore",
                "a605523f-b225-44f1-a598-4d6dc5958337",
                Rarity.COMMON
        );
    }

    protected HerbivoreMutation (int _id, String _name, String _uuid, Rarity _rarity) {
        super(_id,
                _name,
                _uuid,
                _rarity
        );
    }

    protected static final List<Food> VEGE_FOOD = new ArrayList<>(Arrays.asList(
            Foods.APPLE,
            Foods.BAKED_POTATO,
            Foods.BEETROOT,
            Foods.BEETROOT_SOUP,
            Foods.CARROT,
            Foods.DRIED_KELP,
            Foods.ENCHANTED_GOLDEN_APPLE,
            Foods.GOLDEN_APPLE,
            Foods.GOLDEN_CARROT,
            Foods.MELON_SLICE,
            Foods.POTATO,
            Foods.SWEET_BERRIES,
            Foods.MUSHROOM_STEW
    ));

    protected static final List<Food> MEAT_FOOD = new ArrayList<>(Arrays.asList(
            Foods.BEEF,
            Foods.COOKED_BEEF,
            Foods.CHICKEN,
            Foods.COOKED_CHICKEN,
            Foods.COD,
            Foods.COOKED_COD,
            Foods.MUTTON,
            Foods.COOKED_MUTTON,
            Foods.SALMON,
            Foods.COOKED_SALMON,
            Foods.PORKCHOP,
            Foods.COOKED_PORKCHOP,
            Foods.RABBIT,
            Foods.COOKED_RABBIT,
            Foods.RABBIT_STEW,
            Foods.PUFFERFISH,
            Foods.TROPICAL_FISH
    ));

    @Override
    public void attachListeners(IEventBus bus) {
        bus.addListener(this::onItemUseFinish);
    }

    @Override
    public void attachClientListeners(IEventBus bus) {

    }

    public void onItemUseFinish (LivingEntityUseItemEvent.Finish event) {
        if (event.getEntityLiving().world.isRemote()
                || !(event.getEntityLiving() instanceof PlayerEntity)
                || !event.getItem().isFood()
                || !containsInstance(event.getEntityLiving())
                || !getInstance(event.getEntityLiving()).isActive()
        ) return;

        Food food = event.getItem().getItem().getFood();

        if (VEGE_FOOD.contains(food)) {
            float hunger = food.getHealing() * 0.25f;
            float saturation = food.getSaturation() * 0.25f;

            PlayerEntity player = (PlayerEntity)event.getEntityLiving();
            player.getFoodStats().addStats(Math.round(hunger), saturation);
        }

        if (MEAT_FOOD.contains(food)) {
            float hunger = food.getHealing() * 0.25f;
            float saturation = food.getSaturation() * 0.25f;

            PlayerEntity player = (PlayerEntity)event.getEntityLiving();
            player.getFoodStats().addStats(-Math.round(hunger), -saturation);
        }
    }

    @Override
    public boolean isLegalMutation(MutateManager manager) {
        return super.isLegalMutation(manager) && !manager.containsEffect(EffectMutations.CARNIVORE);
    }
}
