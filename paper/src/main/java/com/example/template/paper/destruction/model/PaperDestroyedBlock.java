package com.example.template.paper.destruction.model;

import com.example.template.common.destruction.model.DestroyedBlock;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public record PaperDestroyedBlock(Location pos, BlockData state, ItemStack[] contents) implements DestroyedBlock {
}
