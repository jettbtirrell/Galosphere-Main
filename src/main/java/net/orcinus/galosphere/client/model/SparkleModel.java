package net.orcinus.galosphere.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.orcinus.galosphere.entities.Sparkle;

@Environment(EnvType.CLIENT)
public class SparkleModel<T extends Sparkle> extends AgeableListModel<T> {
	private static final float LIMB_SWING_SCALE = 0.5F;
	private static final float TAIL_SWING_SCALE = 0.5F;
	private static final float SIT_HEAD_TILT = 0.35F;
	private static final float HEAD_BOB_FREQUENCY = 2.0F;
	private static final float HEAD_BOB_AMOUNT = 0.15F;
	private static final float TAIL_FULL_HEALTH_PITCH = 0.1745F;
	private static final float TAIL_LOW_HEALTH_PITCH = -0.1745F;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart head;

	public SparkleModel(ModelPart root) {
		this.body = root.getChild("body");
		this.tail = root.getChild("tail");
		this.leftLeg = root.getChild("leftLeg");
		this.rightLeg = root.getChild("rightLeg");
		this.rightArm = root.getChild("rightArm");
		this.leftArm = root.getChild("leftArm");
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2.0F, -4.0F, 5.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, -1.0F));

		PartDefinition tail = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 23.0F, 1.0F));

		PartDefinition crystal1 = tail.addOrReplaceChild("crystal1", CubeListBuilder.create().texOffs(24, 4).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition crystal2 = tail.addOrReplaceChild("crystal2", CubeListBuilder.create().texOffs(24, 4).addBox(0.0F, -3.0F, 0.0F, 0.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, 0.0F, 0.0F, 2.3562F));

		PartDefinition leftLeg = partdefinition.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(-3, 5).addBox(0.0F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 22.0F, 4.5F, 0.0F, 0.0F, 0.3927F));

		PartDefinition rightLeg = partdefinition.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(-3, 5).mirror().addBox(-5.0F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 22.0F, 4.5F, 0.0F, 0.0F, -0.3927F));

		PartDefinition rightArm = partdefinition.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(-3, 5).mirror().addBox(-5.0F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, 22.0F, -3.5F, 0.0F, 0.0F, -0.3927F));

		PartDefinition leftArm = partdefinition.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(-3, 5).addBox(0.0F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 22.0F, -3.5F, 0.0F, 0.0F, 0.3927F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 21).addBox(-3.0F, -2.0F, -6.0F, 6.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(21, 5).addBox(1.0F, -2.0F, 1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(21, 5).mirror().addBox(-3.0F, -2.0F, 1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 19.0F, -5.0F));

		PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -3.5F, 0.7854F, 0.0F, 0.0F));

		PartDefinition leftWhisker = head.addOrReplaceChild("leftWhisker", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.5F, -6.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition rightWhisker = head.addOrReplaceChild("rightWhisker", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -0.5F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.0F, 0.5F, -6.0F, 0.0F, 0.3927F, 0.0F));

		return LayerDefinition.create(meshdefinition, 48, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInWaterOrBubble()) {
			this.body.yRot = Mth.cos(ageInTicks / 2.0F) / 8.0F;
			this.tail.yRot = Mth.sin(ageInTicks / 4.0F) / 3.5F;
			this.leftLeg.xRot = 0.0F;
			this.leftLeg.yRot = 0.0F;
			this.rightLeg.xRot = 0.0F;
			this.rightLeg.yRot = 0.0F;
			this.leftArm.xRot = 0.0F;
			this.leftArm.yRot = 0.0F;
			this.rightArm.xRot = 0.0F;
			this.rightArm.yRot = 0.0F;
		} else {
			this.body.yRot = 0.0F;
			float swing = limbSwing * LIMB_SWING_SCALE;
			this.leftLeg.xRot = Mth.sin(swing) * limbSwingAmount;
			this.leftLeg.yRot = Mth.cos(swing) * limbSwingAmount * 2.0F;
			this.rightLeg.xRot = -Mth.sin(swing * 1.5F) * limbSwingAmount;
			this.rightLeg.yRot = Mth.cos(swing * 1.5F) * limbSwingAmount * 2.0F;
			this.leftArm.xRot = -Mth.sin(swing * 1.5F) * limbSwingAmount;
			this.leftArm.yRot = Mth.cos(swing * 1.5F) * limbSwingAmount * 2.0F;
			this.rightArm.xRot = Mth.sin(swing) * limbSwingAmount;
			this.rightArm.yRot = Mth.cos(swing) * limbSwingAmount * 2.0F;
			this.tail.yRot = Mth.cos(swing * TAIL_SWING_SCALE) * limbSwingAmount * 0.5F;
		}
		this.tail.xRot = Mth.lerp(entity.getHealth() / entity.getMaxHealth(), TAIL_LOW_HEALTH_PITCH, TAIL_FULL_HEALTH_PITCH);
		float headBobSwing = limbSwing * LIMB_SWING_SCALE * HEAD_BOB_FREQUENCY;
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) + Mth.cos(headBobSwing) * HEAD_BOB_AMOUNT * limbSwingAmount;
		this.head.xRot = headPitch * ((float) Math.PI / 180F) + (entity.isInSittingPose() ? SIT_HEAD_TILT : 0.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(tail, leftLeg, rightLeg, leftArm, rightArm, body);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
		if (this.young) {
			poseStack.pushPose();
			float f1 = 1.0F / 2.0F;
			poseStack.scale(f1, f1, f1);
			poseStack.translate(0.0D, 1.5F, 5.0F / 24.0F);
			this.headParts().forEach((part) -> part.render(poseStack, vertexConsumer, i, j, k));
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.scale(f1, f1, f1);
			poseStack.translate(0.0D, 24.0F / 16.0F, 0.0D);
			this.bodyParts().forEach((part) -> part.render(poseStack, vertexConsumer, i, j, k));
			poseStack.popPose();
		} else {
			this.headParts().forEach((part) -> part.render(poseStack, vertexConsumer, i, j, k));
			this.bodyParts().forEach((part) -> part.render(poseStack, vertexConsumer, i, j, k));
		}
	}
}