package cn.wode490390.nukkit.skyplaingen;

import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.wode490390.nukkit.skyplaingen.generator.SkyPlainGenerator;
import cn.wode490390.nukkit.skyplaingen.generator.SkyPlainOldGenerator;
import cn.wode490390.nukkit.skyplaingen.util.MetricsLite;

public class SkyPlainGeneratorPlugin extends PluginBase {

    private static SkyPlainGeneratorPlugin instance;

    public boolean biome = true;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 8975);
        } catch (Throwable ignore) {

        }
        this.saveDefaultConfig();
        Config config = this.getConfig();

        String key = "biome";
        try {
            this.biome = config.getBoolean(key, this.biome);
        } catch (Exception e) {
            this.logLoadException(key, e);
        }

        boolean old = true;
        key = "old";
        try {
            old = config.getBoolean(key, old);
        } catch (Exception e) {
            this.logLoadException(key, e);
        }

        Generator.addGenerator(SkyPlainGenerator.class, "normal", Generator.TYPE_INFINITE);
        Generator.addGenerator(SkyPlainGenerator.class, "default", Generator.TYPE_INFINITE);
        if (old) {
            Generator.addGenerator(SkyPlainOldGenerator.class, "old", Generator.TYPE_OLD);
        }
    }

    private void logLoadException(String key, Throwable t) {
        this.getLogger().alert("An error occurred while reading the configuration '" + key + "'. Use the default value.");
    }

    public static SkyPlainGeneratorPlugin getInstance() {
        return instance;
    }
}
