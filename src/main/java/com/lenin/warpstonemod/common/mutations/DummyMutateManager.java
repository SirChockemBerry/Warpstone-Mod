package com.lenin.warpstonemod.common.mutations;

import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;

public class DummyMutateManager extends MutateManager{

	public DummyMutateManager() {
		super(null);
	}

	@Override
	public void mutate() { }

	@Override
	protected EffectMutation getRandomEffectMut() {
		System.out.println("Dummy Manager is being Called!");
		return null;
	}

	@Override
	protected CompoundNBT serialize() {
		System.out.println("Dummy Manager is being Called!");
		return null;
	}

	@Override
	public void loadFromNBT(CompoundNBT nbt) { System.out.println("Dummy Manager is being Called!"); }

	@Override
	public void resetMutations() { System.out.println("Dummy Manager is being Called!"); }

	@Override
	public List<Mutation> getMutations() {
		System.out.println("Dummy Manager is being Called!");
		return attributeMutations;
	}

	@Override
	public List<Mutation> getAttributeMutations() {
		System.out.println("Dummy Manager is being Called!");
		return attributeMutations;
	}

	@Override
	public List<EffectMutation> getEffectMutations() {
		System.out.println("Dummy Manager is being Called!");
		return effectMutations;
	}

	@Override
	public LivingEntity getParentEntity() {
		System.out.println("Dummy Manager is being Called!");
		if (Minecraft.getInstance().world.isRemote()) {
			return Minecraft.getInstance().player;
		}
		else {
			return null;
		}
	}

	@Override
	public int getInstability() {
		System.out.println("Dummy Manager is being Called!");
		return 0;
	}

	@Override
	public CompoundNBT getMutData() {
		System.out.println("Dummy Manager is being Called!");
		return null;
	}
}