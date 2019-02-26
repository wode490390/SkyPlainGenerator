package cn.wode490390.nukkit.skyplaingenerator;

import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.wode490390.nukkit.skyplain.SkyPlainGenerator;
import cn.wode490390.nukkit.skyplain.SkyPlainOldGenerator;

public class SPGenerator extends PluginBase {

    public boolean biome = true;

    private static SPGenerator instance;

    public static SPGenerator getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        String t = "biome";
        try {
            this.biome = this.getConfig().getBoolean(t);
        } catch (Exception e) {
            this.logLoadException(t);
        }
        boolean old = true;
        t = "old";
        try {
            old = this.getConfig().getBoolean(t);
        } catch (Exception e) {
            this.logLoadException(t);
        }
        if (old) {
            Generator.addGenerator(SkyPlainOldGenerator.class, "old", Generator.TYPE_OLD);
        }
        Generator.addGenerator(SkyPlainGenerator.class, "normal", Generator.TYPE_INFINITE);
        Generator.addGenerator(SkyPlainGenerator.class, "default", Generator.TYPE_INFINITE);
    }

    private void logLoadException(String text) {
        this.getLogger().alert("An error occurred while reading the configuration '" + text + "'. Use the default value.");
    }
}
