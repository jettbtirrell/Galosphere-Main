package net.orcinus.galosphere.init;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.orcinus.galosphere.Galosphere;

public class GModelLayers {

    public static final ModelLayerLocation SPARKLE = register("sparkle");
    public static final ModelLayerLocation SPARKLE_SPIT = register("sparkle_spit");
    public static final ModelLayerLocation SPECTRE = register("spectre");
    public static final ModelLayerLocation SPECTERPILLAR = register("specterpillar");
    public static final ModelLayerLocation STERLING_HELMET = register("sterling_helmet");
    public static final ModelLayerLocation GILDED_BEADS = register("gilded_beads");
    public static final ModelLayerLocation BERSERKER = register("berserker");
    public static final ModelLayerLocation PINK_SALT_PILLAR = register("pink_salt_pillar");
    public static final ModelLayerLocation PRESERVED = register("preserved");
    public static final ModelLayerLocation STONEFISH = register("stonefish");

    private static ModelLayerLocation register(String name) {
        return new ModelLayerLocation(Galosphere.id(name), "main");
    }

}
