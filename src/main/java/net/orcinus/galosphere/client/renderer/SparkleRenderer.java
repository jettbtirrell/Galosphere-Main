package net.orcinus.galosphere.client.renderer;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.orcinus.galosphere.Galosphere;
import net.orcinus.galosphere.client.model.SparkleModel;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.init.GModelLayers;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class SparkleRenderer extends MobRenderer<Sparkle, EntityModel<Sparkle>> {
    private static final Map<Sparkle.BirthType, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), (map) -> {
        for (Sparkle.BirthType type : Sparkle.BirthType.values()) {
            map.put(type, Galosphere.id(String.format("textures/entity/sparkle/%s_sparkle.png", type.getName())));
        }
    });
    private static final ResourceLocation TEXTURE = Galosphere.id("textures/entity/sparkle/sparkle.png");

    public SparkleRenderer(EntityRendererProvider.Context context) {
        super(context, new SparkleModel<>(context.bakeLayer(GModelLayers.SPARKLE)), 0.4F);
        // Collar layer (client/renderer/layer/SparkleCollarLayer) is disabled until real
        // collar textures exist — it currently has nothing but a fallback missing-texture
        // to render, which draws as a full-body checkerboard duplicate instead of a collar.
    }

    @Override
    public ResourceLocation getTextureLocation(Sparkle entity) {
        return entity.hasCrystal() ? TEXTURE_BY_TYPE.get(entity.getVariant()) : TEXTURE;
    }

}
