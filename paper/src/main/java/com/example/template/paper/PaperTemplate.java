package com.example.template.paper;

import com.example.template.common.Template;
import com.example.template.common.wrapper.LoggerWrapper;
import com.example.template.common.utils.AlphabetUtils;
import com.example.template.paper.config.PaperConfig;
import com.example.template.paper.config.ReloadCommand;
import com.example.template.paper.logger.PaperLoggerWrapper;
import com.example.template.paper.scheduler.PaperLetterChooseScheduler;
import com.example.template.paper.folia.FoliaLetterChooseScheduler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;

public class PaperTemplate extends JavaPlugin implements Template<PaperTemplate> {
    private static final int BSTATS_ID = 34663;

    private static PaperTemplate instance;
    private LoggerWrapper logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = new PaperLoggerWrapper(getLogger());

        logger().info("Template enabled!");

        PaperConfig config = new PaperConfig(this);
        config.load();
        Objects.requireNonNull(getCommand("template")).setExecutor(new ReloadCommand(config));

        initLetterWeights();

        if (isFolia()) {
            FoliaLetterChooseScheduler scheduler = new FoliaLetterChooseScheduler(this);
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> scheduler.tick(), 1L, 1L);
        } else {
            Bukkit.getScheduler().runTaskTimer(this, new PaperLetterChooseScheduler(), 0L, 1L);
        }

        new Metrics(this, BSTATS_ID);

        super.onEnable();
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void initLetterWeights() {
        AlphabetUtils.computeAndSetWeights(
                Arrays.stream(Material.values())
                        .filter(m -> m.isBlock() && !m.isAir() && !m.isLegacy())
                        .toList(),
                Material::name,
                AlphabetUtils.DEFAULT_MIN_BLOCK_COUNT
        );
    }

    @Override
    public void onDisable() {
        logger().info("Template disabled!");

        super.onDisable();
    }

    @Override
    public PaperTemplate get() {
        return instance;
    }

    @Override
    public LoggerWrapper logger() {
        return logger;
    }

    public static PaperTemplate getInstance() {
        return instance;
    }
}