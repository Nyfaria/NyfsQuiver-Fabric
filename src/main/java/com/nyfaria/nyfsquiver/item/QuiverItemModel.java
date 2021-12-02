package com.nyfaria.nyfsquiver.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class QuiverItemModel extends HumanoidModel<LivingEntity> {
    public QuiverItemModel(ModelPart root) {
        super(root);
        this.setAllVisible(false);
        this.head.visible = true;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition head = modelPartData.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4f, -16f, -4f, 8f, 8f, 8f), PartPose.ZERO);
        head.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-5f, -9f, -5f, 10f, 1f, 10f), PartPose.ZERO);
        return LayerDefinition.create(modelData, 64, 32);
    }
}
