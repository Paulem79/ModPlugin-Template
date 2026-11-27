package com.example.template.paper.destruction.model;

import com.example.template.common.destruction.model.StoredEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntitySnapshot;

public record PaperStoredEntity(EntitySnapshot snapshot, Location pos) implements StoredEntity {
}
