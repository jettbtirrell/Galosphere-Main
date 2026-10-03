package net.orcinus.galosphere.client.renderer.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.orcinus.galosphere.Galosphere;
import net.orcinus.galosphere.entities.Sparkle;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class SparkleCollarLayer extends RenderLayer<Sparkle, EntityModel<Sparkle>> {
    private static final Map<Sparkle.BirthType, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), (map) -> {
        for (Sparkle.BirthType type : Sparkle.BirthType.values()) {
            map.put(type, Galosphere.id(String.format("textures/entity/sparkle/%s_collar.png", type.getName())));
        }
    });

    public SparkleCollarLayer(RenderLayerParent<Sparkle, EntityModel<Sparkle>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Sparkle entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isTame() && !entity.isInvisible()) {
            ResourceLocation texture = TEXTURE_BY_TYPE.get(entity.getVariant());
            renderColoredCutoutModel(this.getParentModel(), texture, poseStack, buffer, packedLight, entity, -1);
        }
    }
}
