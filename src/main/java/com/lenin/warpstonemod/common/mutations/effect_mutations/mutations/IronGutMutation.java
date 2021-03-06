package com.lenin.warpstonemod.common.mutations.effect_mutations.mutations;

import com.lenin.warpstonemod.common.mutations.MutateManager;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutation;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutations;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;
import java.util.stream.Collectors;

public class IronGutMutation extends EffectMutation {
	public IronGutMutation(int _id) {
		super(_id,
				"iron_gut",
				"487b1027-3643-41ef-b3f5-e5f71abf503f",
				Rarity.UNCOMMON);
	}

	/**This mutation makes the player immune to the negative effects of any food item
	 */

	@Override
	public void attachListeners(IEventBus bus) {
		bus.addListener(this::onItemUseFinish);
	}

	@Override
	public void attachClientListeners(IEventBus bus) {

	}

	/**The method for this mutation is to check the consumed item for any potion Effects
	 * matching the type {@link EffectType#HARMFUL}. By calling {@link LivingEntityUseItemEvent.Finish}
	 * we're detecting the effects the tick they're applied, and then removing them
	 */

	public void onItemUseFinish (LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntityLiving() instanceof PlayerEntity)
				|| !containsInstance(event.getEntityLiving())
				|| !getInstance(event.getEntityLiving()).isActive()
		) return;

		List<Effect> effects = event.getItem().getItem().getFood().getEffects()
				.stream()
				.map(Pair::getFirst)
				.map(EffectInstance::getPotion)
				.filter(effect -> effect.getEffectType() == EffectType.HARMFUL)
				.collect(Collectors.toList());

		effects.forEach(effect -> {
			event.getEntityLiving().removePotionEffect(effect);
		});
	}

	@Override
	public boolean isLegalMutation(MutateManager manager) {
		return super.isLegalMutation(manager) && !manager.containsEffect(EffectMutations.FAST_METABOLISM);
	}
}