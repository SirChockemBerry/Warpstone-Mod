package com.lenin.warpstonemod.client.gui.screens;

import com.lenin.warpstonemod.client.gui.RawTextureResource;
import com.lenin.warpstonemod.client.gui.Textures;
import com.lenin.warpstonemod.client.gui.WSElement;
import com.lenin.warpstonemod.client.gui.components.ImageComponent;
import com.lenin.warpstonemod.common.Registration;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutation;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutations;
import com.lenin.warpstonemod.common.mutations.tags.MutationTag;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CorruptedTomeScreen extends WSScreen{
    public CorruptedTomeScreen() {
        super(new TranslationTextComponent("warpstone.screen.corrupted_tome"), Textures.CORRUPTED_TOME_SCREEN, 256, 180);
    }

    @Override
    protected void init() {
        super.init();

        List<EffectMutation> muts = new ArrayList<>(Registration.EFFECT_MUTATIONS.getValues());

        muts.sort(new TagComporator());

        for (int i = 0; i < muts.size(); i++) {
                //Casting to int always rounds down
            int row = ((int) ((float)i / 10));

            int y = getGuiTop() + 10 + (23 * row);
            int x = getGuiLeft() + 15 + (23 * (i - (10 * row)));
            /*if (i >= 13) {
                y += 23;
                x = getGuiLeft() + 10 + (23 * (i - 13));
            }*/

            elements.add(new WSElement.Builder(x, y, 18, 18, this)
                    .addComponent(new ImageComponent(
                            new RawTextureResource(EffectMutations.getMutation(muts.get(i)).getTexture(), 18, 18, 0, 0)))
                    .addTooltips(EffectMutations.getMutation(muts.get(i)).getToolTips().toArray(new ITextComponent[0]))
                    .build()
            );
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public static class TagComporator implements Comparator<EffectMutation> {

        @Override
        public int compare(EffectMutation o1, EffectMutation o2) {
            int o1Weight = 1;
            int o2Weight = 1;

           for (MutationTag tag : o1.tags) {
               int tagWeight = getTagWeight(tag.getResource().getPath());
               if (tagWeight > o1Weight) o1Weight =  tagWeight;
           }

            for (MutationTag tag : o2.tags) {
                int tagWeight = getTagWeight(tag.getResource().getPath());
                if (tagWeight > o2Weight) o2Weight =  tagWeight;
            }

            return o1Weight - o2Weight;
        }

        private int getTagWeight (String key){
            switch (key){
                case "negative":
                    return 1;
                case "uncommon":
                    return 3;
                case "rare":
                    return 4;
                default:
                    return 2;
            }
        }
    }
}