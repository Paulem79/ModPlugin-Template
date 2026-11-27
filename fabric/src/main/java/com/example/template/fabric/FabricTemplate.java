package com.example.template.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import com.example.template.common.Template;
import com.example.template.common.wrapper.LoggerWrapper;
import com.example.template.common.utils.AlphabetUtils;
import com.example.template.fabric.logger.FabricLoggerWrapper;
import com.example.template.fabric.scheduler.FabricLetterChooseScheduler;
import org.slf4j.LoggerFactory;

import java.util.stream.StreamSupport;

public class FabricTemplate implements ModInitializer, Template<FabricTemplate> {
    public static final String MOD_ID = "template";

    private static FabricTemplate instance;
    private final LoggerWrapper loggerWrapper = new FabricLoggerWrapper(LoggerFactory.getLogger(MOD_ID));
    private final FabricLetterChooseScheduler letterChooseScheduler = new FabricLetterChooseScheduler();

    @Override
    public void onInitialize() {
        instance = this;
        logger().info("Initializing Template");

        initLetterWeights();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            letterChooseScheduler.tick(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            logger().info("Template disabled!");
        });
    }

    private void initLetterWeights() {
        AlphabetUtils.computeAndSetWeights(
                StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
                        .filter(block -> block != Blocks.AIR)
                        .map(block -> BuiltInRegistries.BLOCK.getKey(block).getPath())
                        .toList(),
                path -> path,
                AlphabetUtils.DEFAULT_MIN_BLOCK_COUNT
        );
    }

    @Override
    public FabricTemplate get() {
        return this;
    }

    @Override
    public LoggerWrapper logger() {
        return loggerWrapper;
    }

    public static FabricTemplate getInstance() {
        return instance;
    }
}
