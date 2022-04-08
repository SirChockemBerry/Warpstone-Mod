package com.lenin.warpstonemod.client.gui.screens;

import com.lenin.warpstonemod.client.gui.RawTextureResource;
import com.lenin.warpstonemod.client.gui.Textures;
import com.lenin.warpstonemod.client.gui.WSElement;
import com.lenin.warpstonemod.client.gui.components.ImageComponent;
import com.lenin.warpstonemod.common.Registration;
import com.lenin.warpstonemod.common.Warpstone;
import com.lenin.warpstonemod.common.mutations.Mutation;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutation;
import com.lenin.warpstonemod.common.mutations.effect_mutations.Mutations;
import com.lenin.warpstonemod.common.mutations.evolving_mutations.EvolvingMutation;
import com.lenin.warpstonemod.common.mutations.tags.MutationTag;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CorruptedTomeScreen extends WSScreen{
    public CorruptedTomeScreen() {
        super(new TranslationTextComponent("warpstone.screen.corrupted_tome"), Textures.CORRUPTED_TOME_SCREEN, 256, 180);
    }

    @Override
    protected void init() {
        super.init();

        List<Mutation> muts = Registration.EFFECT_MUTATIONS.getValues().stream()
                .filter(mutation -> !mutation.hasTag(Warpstone.key("child")))
                .sorted(new TagComporator())
                .collect(Collectors.toList());

        for (int i = 0; i < muts.size(); i++) {
                //Casting to int always rounds down
            int row = ((int) ((float)i / 10));

            int y = getGuiTop() + 10 + (23 * row);
            int x = getGuiLeft() + 15 + (23 * (i - (10 * row)));

            Mutation mutation = muts.get(i);

            RawTextureResource texture = new RawTextureResource(mutation.getTexture(), 18, 18, 0, 0);

            WSElement.Builder builder = new WSElement.Builder(x, y, 18, 18, this);

            if (mutation instanceof EvolvingMutation) {
                EvolvingMutation evolve = (EvolvingMutation) mutation;

                //evolve.getChildNodes().get()

                WSElement treeElement = new WSElement.Builder(x, y - 18, 18, 18, this)
                        .addComponent(new ImageComponent())
                        .build();

                builder.addHoveredElement(treeElement);
            } else {
                builder.addTooltips(mutation.getToolTips().toArray(new ITextComponent[0]));
            }

            elements.add(builder.addComponent(new ImageComponent(texture)).build());
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    /**Comparator sorts mutations so negatives are first sorted by rarity with the rest coming after sorted by rarity.
     * Only needed for Tome Screen hence this isn't a part of the core Mutation Code
     */

    public static class TagComporator implements Comparator<Mutation> {

        @Override
        public int compare(Mutation o1, Mutation o2) {
            int o1Weight = 0;
            int o2Weight = 0;

            for (MutationTag tag : o1.getTags()) {
                o1Weight += tag.getWeight();
            }
            for (MutationTag tag : o2.getTags()) {
                o2Weight += tag.getWeight();
            }

            return o1Weight - o2Weight;
        }
    }
}