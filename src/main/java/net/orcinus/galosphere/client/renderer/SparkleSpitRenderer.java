package net.orcinus.galosphere.client.renderer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.orcinus.galosphere.Galosphere;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.entities.SparkleSpit;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class SparkleSpitRenderer extends ArrowRenderer<SparkleSpit> {
    private static final Map<Sparkle.BirthType, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), (map) -> {
        for (Sparkle.BirthType type : Sparkle.BirthType.values()) {
            map.put(type, Galosphere.id(String.format("textures/entity/projectiles/%s_spit.png", type.getName())));
        }
    });

    public SparkleSpitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SparkleSpit entity) {
        return TEXTURE_BY_TYPE.get(entity.getBirthType());
    }

    @Override
    public void render(SparkleSpit entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, LightTexture.FULL_BRIGHT);
    }
}
