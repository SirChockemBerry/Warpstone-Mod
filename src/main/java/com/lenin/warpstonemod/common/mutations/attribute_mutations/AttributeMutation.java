package com.lenin.warpstonemod.common.mutations.attribute_mutations;

import com.lenin.warpstonemod.common.Warpstone;
import com.lenin.warpstonemod.common.mutations.PlayerManager;
import com.lenin.warpstonemod.common.mutations.tags.MutationTag;
import com.lenin.warpstonemod.common.mutations.tags.MutationTags;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

public class AttributeMutation{
	protected final IAttributeSource attributeSource;
	protected final String name;
	protected final UUID uuid;
	protected int mutationLevel;
	protected final PlayerManager manager;

	protected final MutationTag tag;

	public AttributeMutation(IAttributeSource _attributeSource, PlayerManager _manager) {
		uuid = UUID.randomUUID();
		this.attributeSource = _attributeSource;
		name = _attributeSource.getAttributeName().getPath();
		mutationLevel = 0;
		manager = _manager;

		tag = MutationTags.getTag(Warpstone.key(name));
	}

	protected void addModifier () {
		clearMutation();

		attributeSource.applyModifier(
				new AttributeModifier(
						uuid,
						name,
						(double)mutationLevel / 100,
						AttributeModifier.Operation.MULTIPLY_TOTAL));
	}

	public void setLevel (int value) {
		mutationLevel = value;

		int maxPos = Math.min((manager.getCorruptionLevel() + 1) * 10, 50);
		int maxNeg = Math.max((manager.getCorruptionLevel() + 1) * -5, -25);

		if (mutationLevel > maxPos) mutationLevel = maxPos;
		if (mutationLevel < maxNeg) mutationLevel = maxNeg;

		addModifier();
	}

	public void changeLevel (int value){
		setLevel(mutationLevel + value);
	}

	public boolean canMutate (PlayerManager manager) {
		return mutationLevel < Math.min((manager.getCorruptionLevel() + 1) * 10, 50) || mutationLevel > Math.max((manager.getCorruptionLevel() + 1) * -5, -25);
	}

	public void clearMutation() {
		attributeSource.removeModifier(uuid);
	}

	public TranslationTextComponent getMutationName() {
		return new TranslationTextComponent("attribute." + name);
	}

	public String getMutationType() {
		return name;
	}

	public int getMutationLevel (){
		return mutationLevel;
	}
}