package net.orcinus.galosphere.client.renderer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.orcinus.galosphere.Galosphere;
import net.orcinus.galosphere.entities.Sparkle;
import net.orcinus.galosphere.entities.SparkleSpit;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class SparkleSpitRenderer extends EntityRenderer<SparkleSpit> {
    private static final Map<Sparkle.BirthType, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), (map) -> {
        for (Sparkle.BirthType type : Sparkle.BirthType.values()) {
            map.put(type, Galosphere.id(String.format("textures/entity/projectiles/%s_spit.png", type.getName())));
        }
    });
    private final LlamaSpitModel<SparkleSpit> model;

    public SparkleSpitRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new LlamaSpitModel<>(context.bakeLayer(ModelLayers.LLAMA_SPIT));
    }

    @Override
    public void render(SparkleSpit entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.15F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SparkleSpit entity) {
        return TEXTURE_BY_TYPE.get(entity.getBirthType());
    }
}
