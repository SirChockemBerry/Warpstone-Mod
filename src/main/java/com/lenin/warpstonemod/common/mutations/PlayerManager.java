package com.lenin.warpstonemod.common.mutations;

import com.lenin.warpstonemod.common.Registration;
import com.lenin.warpstonemod.common.Warpstone;
import com.lenin.warpstonemod.common.items.IWarpstoneConsumable;
import com.lenin.warpstonemod.common.mutations.attribute_mutations.*;
import com.lenin.warpstonemod.common.mutations.attribute_mutations.attributes.AttributeMutationUUIDs;
import com.lenin.warpstonemod.common.mutations.attribute_mutations.WSAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {
    protected final LivingEntity parentEntity;
    protected final List<AttributeMutation> attributeMutations = new ArrayList<>();
    protected Map<ResourceLocation, Mutation> mutations = new HashMap<>();

    protected List<IAttributeSource> attributes = new ArrayList<>();

    protected CompoundNBT mutData;

    protected int instability = 0;
    protected int corruption = 0;

    public PlayerManager(LivingEntity _parentEntity){
        parentEntity = _parentEntity;

        attributeMutations.add(new AttributeMutation(new VanillaAttribute(Attributes.MAX_HEALTH, _parentEntity), this, AttributeMutationUUIDs.MAX_HEALTH_UUID));
        attributeMutations.add(new AttributeMutation(new VanillaAttribute(Attributes.ATTACK_DAMAGE, _parentEntity), this, AttributeMutationUUIDs.ATTACK_DAMAGE_UUID));
        attributeMutations.add(new AttributeMutation(new VanillaAttribute(Attributes.MOVEMENT_SPEED, _parentEntity), this, AttributeMutationUUIDs.SPEED_UUID));
        attributeMutations.add(new AttributeMutation(new VanillaAttribute(Attributes.ARMOR, _parentEntity), this, AttributeMutationUUIDs.AMOUR_UUID));
        attributeMutations.add(new AttributeMutation(new VanillaAttribute(Attributes.ARMOR_TOUGHNESS, _parentEntity), this, AttributeMutationUUIDs.ARMOUR_TOUGHNESS_UUID));
        attributeMutations.add(new AttributeMutation(getAttribute(new ResourceLocation(Warpstone.MOD_ID, "harvest_speed")), this, AttributeMutationUUIDs.MINING_SPEED_UUID));

        mutData = serialize();
    }

    protected PlayerManager () {
        parentEntity = null;
    }

    public void mutate(IWarpstoneConsumable item){
        boolean hasEffectBeenCreated = false;

        //Loop over every point of instablity and apply levels, no negatives if no instablity
        for (int i = 0; i < getInstabilityLevel() + 1; i++) {
                /*  Effect Mutation Creation    */
            if (mutations.size() < 14 && !hasEffectBeenCreated && Warpstone.getRandom().nextInt(100) > 90) {
                List<Mutation> legalMutations = Registration.EFFECT_MUTATIONS.getValues()
                        .stream()
                        .filter(mut -> !this.containsEffect(mut))
                        .filter(mut -> mut.isLegalMutation(this))
                        .collect(Collectors.toList());

                if (legalMutations.size() > 0) {
                    Mutation mut = legalMutations.get(Warpstone.getRandom().nextInt(legalMutations.size()));

                    addMutation(mut);

                    hasEffectBeenCreated = true;
                    continue;
                }

                //If no effect mutations can be added then there's no point checking this each iteration so we stop it here
                hasEffectBeenCreated = true;
            }

            List<AttributeMutation> legal = attributeMutations
                    .stream()
                    .filter(attr -> attr.canMutate(this))
                    .collect(Collectors.toList());

            int change = getCorruptionLevel() > 0 ? Warpstone.getRandom().nextInt(getCorruptionLevel()) + 1 : 1;

            if (i > 0) {
                int index = Warpstone.getRandom().nextInt(legal.size());
                legal.get(index).changeLevel(-change);
                legal.remove(index);
            }

            if (i >= 8) change = Warpstone.getRandom().nextInt(100) > 100 - (5 * (getInstabilityLevel() - getCorruptionLevel())) ? change * -1 : change;

            legal.get(Warpstone.getRandom().nextInt(legal.size()))
                    .changeLevel(change);
        }

        double witherRisk = getWitherRisk(item.getCorruptionValue());
        if (Math.random() > 1f - witherRisk) {
            int duration = Warpstone.getRandom().nextInt((int) Math.round(2400 * witherRisk));
            parentEntity.addPotionEffect(new EffectInstance(Effects.WITHER, duration));
        }

        int instabilityValue = item.getCorruptionValue() + (int) Math.round(item.getCorruptionValue() * (
                (double)getInstability() / 100) * (double)(Warpstone.getRandom().nextInt((getCorruptionLevel() + 2) * 10) / 100)
        );
        int corruptionValue = Math.round(instabilityValue * (getInstabilityLevel() /10f));

        int currentLevel = getCorruptionLevel();

        instability += instabilityValue;
        corruption += corruptionValue;
        mutData = serialize();
        MutateHelper.pushPlayerDataToClient(getUniqueId(), getMutData());

        if (currentLevel != getCorruptionLevel()) {
            parentEntity.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }
    }

    public void addMutation (Mutation mutation) {
        if (mutations.containsValue(mutation)
                || !Registration.EFFECT_MUTATIONS.containsValue(mutation)) return;

        mutation.applyMutation(this);
        mutations.put(mutation.getRegistryName(),mutation);
    }

    public void removeMutation (Mutation mutation) {
        if (!mutations.containsValue(mutation)
                || !Registration.EFFECT_MUTATIONS.containsValue(mutation)) return;

        mutation.clearMutation(this);
        mutations.remove(mutation.getRegistryName());
    }

    public void addMutationCommand (Mutation mutation) {
        if (mutations.containsKey(mutation.getRegistryName())
                || !Registration.EFFECT_MUTATIONS.containsKey(mutation.getRegistryName())) return;

        addMutation(mutation);
        mutData = serialize();
        MutateHelper.pushPlayerDataToClient(getUniqueId(), getMutData());
    }

    public void removeMutationCommand (Mutation mutation) {
        if (!mutations.containsKey(mutation.getRegistryName())
                || !Registration.EFFECT_MUTATIONS.containsKey(mutation.getRegistryName())) return;

        removeMutation(mutation);
        mutData = serialize();
        MutateHelper.pushPlayerDataToClient(getUniqueId(), getMutData());
    }

    protected CompoundNBT serialize (){
        CompoundNBT out = new CompoundNBT();
        out.putUniqueId("player", parentEntity.getUniqueID());
        out.putInt("instability", getInstability());
        out.putInt("corruption", getCorruption());

        for (AttributeMutation mut : getAttributeMutations()) {
            out.putInt(mut.getMutationType(), mut.getMutationLevel());
        }

        ListNBT serializedMutations = new ListNBT();

        for (ResourceLocation key : mutations.keySet()) {
            CompoundNBT mut = new CompoundNBT();

            mut.putString("key", key.toString());
            mut.put("mutation_data", mutations.get(key).saveData(this));

            serializedMutations.add(mut);
        }

        out.put("mutations", serializedMutations);

        return out;
    }

    public void loadFromNBT (CompoundNBT nbt) {
        int currentLevel = getCorruptionLevel();
        instability = nbt.getInt("instability");
        corruption = nbt.getInt("corruption");
        mutData = nbt;

        if (getCorruptionLevel() > currentLevel) {
            parentEntity.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        for (AttributeMutation mut : getAttributeMutations()) {
            mut.setLevel(nbt.getInt(mut.getMutationType()));
        }

        ListNBT list = (ListNBT) nbt.get("mutations");

        List<ResourceLocation> deletion = new ArrayList<>(mutations.keySet());

        if (list != null) {
            for (INBT nbt2 : list) {
                CompoundNBT compound = (CompoundNBT) nbt2;
                ResourceLocation key = new ResourceLocation(compound.getString("key"));

                if (containsEffect(key)) {
                    deletion.remove(key);
                    continue;
                }

                getEffect(key).loadData(this, compound.getCompound("mutation_data"));
                mutations.put(key, getEffect(key));
            }
        }

        for (ResourceLocation mut : deletion) {
            removeMutation(getEffect(mut));
        }
    }

    public void resetMutations (boolean death) {
        for (AttributeMutation m : attributeMutations) { m.setLevel(0); }

        for (Mutation mut : mutations.values()) {
            removeMutation(mut);
        }
        mutations.clear();

        if (!death) corruption = 0;
        instability = 0;
        mutData = serialize();

        MutateHelper.pushPlayerDataToClient(parentEntity.getUniqueID(), getMutData());
    }

    public List<AttributeMutation> getAttributeMutations (){
        return attributeMutations;
    }

    public List<ResourceLocation> getEffectMutations (){
            return new ArrayList<>(mutations.keySet());
    }

    public LivingEntity getParentEntity (){
        return this.parentEntity;
    }

    public int getInstability(){
        return instability;
    }

    public int getInstabilityLevel (){
        return (int) Math.floor((double) (instability) / 100);
    }

    public int getCorruption () {
        return corruption;
    }

    public int getCorruptionLevel (){
        int threshold = 0;

        for (int i = 0; i < 10; i++) {
            threshold += i * (125 * (i + 1)) + 750;

            if (threshold > corruption) {
                //if (i < 1) return 0;
                return i;
            }
        }

        return 0;
    }

    public int getCorruptionToNextLevel () {
        int target = getCorruptionLevel() + 1;
        int threshold = 0;

        for (int i = 0; i < target; i++) {
            threshold += i * (125 * (i + 1)) + 750;

            if (threshold > corruption) {
                return threshold;
            }
        }

        return threshold - getCorruption();
    }

    @OnlyIn(Dist.CLIENT)
    public List<ITextComponent> getCorruptionTooltips () {
        List<ITextComponent> toolTips = new ArrayList<>();
        int witherEffect = getCorruptionLevel() * 10;

        toolTips.add(new TranslationTextComponent("mutation.screen.corruption").mergeStyle(TextFormatting.WHITE));
        toolTips.add(new TranslationTextComponent("warpstone.screen.generic.level")
                .appendSibling(new StringTextComponent(" "))
                .appendSibling(new StringTextComponent(String.valueOf(getCorruptionLevel())))
                .mergeStyle(TextFormatting.WHITE));
        toolTips.add(new TranslationTextComponent("warpstone.screen.generic.total")
                .appendSibling(new StringTextComponent(" "))
                .appendSibling(new StringTextComponent(String.valueOf(getCorruption())))
                .mergeStyle(TextFormatting.WHITE));

        toolTips.add(new TranslationTextComponent("warpstone.screen.generic.next_level")
                .mergeStyle(TextFormatting.GRAY)
                .mergeStyle(TextFormatting.ITALIC)
                .appendSibling(new StringTextComponent(" "))
                .appendSibling(new StringTextComponent(String.valueOf(getCorruptionToNextLevel())).mergeStyle(TextFormatting.WHITE))
        );

        if (witherEffect > 0) {
            toolTips.add(new TranslationTextComponent("warpstone.consumable.wither_risk")
                    .appendSibling(new StringTextComponent(" "))
                    .appendSibling(new StringTextComponent("-" + witherEffect + "%").mergeStyle(TextFormatting.GREEN))
            );
        }

        return toolTips;
    }

    public List<ITextComponent> getInstabilityTooltips () {
        List<ITextComponent> toolTips = new ArrayList<>();
        TextFormatting color = getInstabilityLevel() > 5 ? TextFormatting.RED : TextFormatting.WHITE;

        int witherEffect = getInstabilityLevel() * 10 - 30;

        toolTips.add(new TranslationTextComponent("mutation.screen.instability").mergeStyle(TextFormatting.WHITE));
        toolTips.add(new TranslationTextComponent("warpstone.screen.generic.level")
                .appendSibling(new StringTextComponent(" "))
                .appendSibling(new StringTextComponent(String.valueOf(getInstabilityLevel()))
                        .mergeStyle(color)));
        toolTips.add(new TranslationTextComponent("warpstone.screen.generic.total")
                .appendSibling(new StringTextComponent(" "))
                .appendSibling(new StringTextComponent(String.valueOf(getInstability())))
                .mergeStyle(TextFormatting.WHITE));
        if (witherEffect > 0) {
            toolTips.add(new TranslationTextComponent("warpstone.consumable.wither_risk")
                    .appendSibling(new StringTextComponent(" "))
                    .appendSibling(new StringTextComponent("+" + witherEffect + "%").mergeStyle(TextFormatting.RED))
            );
        }

        return toolTips;
    }

    public UUID getUniqueId () {
        return getParentEntity().getUniqueID();
    }

    public IAttributeSource getAttribute (ResourceLocation key) {
        for (IAttributeSource attribute : attributes) {
            if (attribute.getAttributeName().equals(key)) return attribute;
        }

        IAttributeSource newAttribute;

        if (ForgeRegistries.ATTRIBUTES.containsKey(key)) newAttribute = new VanillaAttribute(ForgeRegistries.ATTRIBUTES.getValue(key), getParentEntity());
        //else if (Registry.ATTRIBUTE.containsKey(key)) newAttribute = new VanillaAttribute(Registry.ATTRIBUTE.getOrDefault(key), getParentEntity());
        else newAttribute = WSAttributes.createAttribute(key, getParentEntity());

        attributes.add(newAttribute);
        return newAttribute;
    }

    public double getWitherRisk (int corruptionValue) {
        double value =  (((double) corruptionValue / 100f) * (((double) getInstabilityLevel() / 10 - 0.3) - (double) getCorruptionLevel() / 10));

        if (value < 0) return 0;
        return value;
    }

    public CompoundNBT getMutData () {
        if (mutData == null) System.out.println("Getting the NBT from Manager returns null");
        return mutData;
    }

    public void unload() {
        saveData();

        for (Mutation mut : mutations.values()) {
            removeMutation(mut);
        }

        mutations.clear();
        attributeMutations.clear();
        MutateHelper.MANAGERS.remove(this);
    }

    public void saveData (){
        mutData = serialize();
        MutateHelper.savePlayerData(parentEntity.getUniqueID(), getMutData());
    }

    private Mutation getEffect (ResourceLocation key) {
        return Mutations.getMutation(key);
    }

    public boolean containsEffect (Mutation mut) {
        return containsEffect(mut.getRegistryName());
    }

    public boolean containsEffect (ResourceLocation key) {
        return mutations.containsKey(key);
    }
}