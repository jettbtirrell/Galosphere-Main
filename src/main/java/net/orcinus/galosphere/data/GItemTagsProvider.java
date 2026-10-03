package net.orcinus.galosphere.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.orcinus.galosphere.compat.init.CItemTags;
import net.orcinus.galosphere.init.GBlocks;
import net.orcinus.galosphere.init.GItemTags;
import net.orcinus.galosphere.init.GItems;

import java.util.concurrent.CompletableFuture;

public class GItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    public GItemTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture, new GBlockTagsProvider(output, completableFuture));
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        this.getOrCreateTagBuilder(CItemTags.SILVER_BLOCKS).add(GBlocks.SILVER_BLOCK.asItem());
        this.getOrCreateTagBuilder(CItemTags.SILVER_INGOTS).add(GItems.SILVER_INGOT);
        this.getOrCreateTagBuilder(CItemTags.SILVER_NUGGETS).add(GItems.SILVER_NUGGET);
        this.getOrCreateTagBuilder(CItemTags.SILVER_ORES).add(GBlocks.SILVER_ORE.asItem(), GBlocks.DEEPSLATE_SILVER_ORE.asItem());
        this.getOrCreateTagBuilder(GItemTags.BOMB_BOUNCY_MODIFIERS).add(Items.SLIME_BALL);
        this.getOrCreateTagBuilder(GItemTags.BOMB_DURATION_MODIFIERS).add(Items.STRING);
        this.getOrCreateTagBuilder(GItemTags.BOMB_EXPLOSION_MODIFIERS).add(Items.GUNPOWDER);
        this.getOrCreateTagBuilder(GItemTags.NON_SINKABLES_HORSE_ARMORS).add(GItems.STERLING_HORSE_ARMOR, Items.LEATHER_HORSE_ARMOR);
        this.getOrCreateTagBuilder(GItemTags.SPARKLE_TEMPT_ITEMS).add(Items.GLOW_LICHEN);
        this.getOrCreateTagBuilder(GItemTags.SPARKLE_BREED_ITEMS).add(GItems.ALLURITE_SHARD, GItems.LUMIERE_SHARD);
        this.getOrCreateTagBuilder(GItemTags.SPARKLE_ANY_TEMPT_ITEMS).addOptionalTag(GItemTags.SPARKLE_TEMPT_ITEMS).addOptionalTag(GItemTags.SPARKLE_BREED_ITEMS);
        this.getOrCreateTagBuilder(GItemTags.SPECTRE_TEMPT_ITEMS).add(GBlocks.LICHEN_SHELF.asItem());
        this.getOrCreateTagBuilder(GItemTags.SALTBOUND_TABLET_ENCHANTABLE).add(GItems.SALTBOUND_TABLET);
        this.getOrCreateTagBuilder(GItemTags.STONEFISH_REPELLENT).addOptionalTag(ItemTags.CLUSTER_MAX_HARVESTABLES);
        this.getOrCreateTagBuilder(ItemTags.FREEZE_IMMUNE_WEARABLES).add(GItems.STERLING_HORSE_ARMOR, GItems.STERLING_HELMET, GItems.STERLING_CHESTPLATE, GItems.STERLING_LEGGINGS, GItems.STERLING_BOOTS);
        this.getOrCreateTagBuilder(ItemTags.HEAD_ARMOR).add(GItems.STERLING_HELMET);
        this.getOrCreateTagBuilder(ItemTags.CHEST_ARMOR).add(GItems.STERLING_CHESTPLATE);
        this.getOrCreateTagBuilder(ItemTags.LEG_ARMOR).add(GItems.STERLING_LEGGINGS);
        this.getOrCreateTagBuilder(ItemTags.FOOT_ARMOR).add(GItems.STERLING_BOOTS);
        this.getOrCreateTagBuilder(ConventionalItemTags.FOODS).add(GItems.LICHEN_CORDYCEPS, GItems.GOLDEN_LICHEN_CORDYCEPS, GItems.SALTED_JERKY, GItems.PRESERVED_FLESH);
    }

}
