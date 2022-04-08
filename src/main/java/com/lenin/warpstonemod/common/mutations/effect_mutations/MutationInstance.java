package com.lenin.warpstonemod.common.mutations.effect_mutations;

import com.lenin.warpstonemod.common.mutations.PlayerManager;
import net.minecraft.entity.LivingEntity;

public class MutationInstance {
	protected final PlayerManager parent;

	protected boolean active;

	public MutationInstance(PlayerManager _parent){
		parent = _parent;

		active = false;
	}

	public PlayerManager getParent (){
		return parent;
	}

	public boolean isActive () {
		return active;
	}

	public void setActive (boolean value){
		active = value;
	}
}