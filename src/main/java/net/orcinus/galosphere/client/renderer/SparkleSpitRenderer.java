package net.orcinus.galosphere.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.orcinus.galosphere.entities.SparkleSpit;
import net.orcinus.galosphere.init.GModelLayers;

@Environment(EnvType.CLIENT)
public class SparkleSpitRenderer extends GlowingCubeProjectileRenderer<SparkleSpit> {
    private static final int ALLURITE_COLOR = 0xFF55E5FF;

    public SparkleSpitRenderer(EntityRendererProvider.Context context) {
        super(context, GModelLayers.SPARKLE_SPIT);
    }

    @Override
    protected int getColor(SparkleSpit entity) {
        return ALLURITE_COLOR;
    }
}
