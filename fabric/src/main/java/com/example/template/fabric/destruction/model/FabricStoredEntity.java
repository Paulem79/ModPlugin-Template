package com.example.template.fabric.destruction.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import com.example.template.common.destruction.model.StoredEntity;

public record FabricStoredEntity(CompoundTag snapshot, Vec3 pos) implements StoredEntity {
}
