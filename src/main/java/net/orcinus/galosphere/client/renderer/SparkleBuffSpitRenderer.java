package net.orcinus.galosphere.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.orcinus.galosphere.entities.SparkleBuffSpit;
import net.orcinus.galosphere.init.GModelLayers;

@Environment(EnvType.CLIENT)
public class SparkleBuffSpitRenderer extends GlowingCubeProjectileRenderer<SparkleBuffSpit> {
    private static final int LUMIERE_COLOR = 0xFFFFB22D;

    public SparkleBuffSpitRenderer(EntityRendererProvider.Context context) {
        super(context, GModelLayers.SPARKLE_SPIT);
    }

    @Override
    protected int getColor(SparkleBuffSpit entity) {
        return LUMIERE_COLOR;
    }
}
