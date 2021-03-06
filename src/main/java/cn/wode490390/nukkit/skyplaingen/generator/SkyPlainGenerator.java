package cn.wode490390.nukkit.skyplaingen.generator;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStone;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeSelector;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.level.generator.noise.vanilla.f.NoiseGeneratorOctavesF;
import cn.nukkit.level.generator.noise.vanilla.f.NoiseGeneratorPerlinF;
import cn.nukkit.level.generator.object.ore.OreType;
import cn.nukkit.level.generator.populator.impl.PopulatorGroundCover;
import cn.nukkit.level.generator.populator.impl.PopulatorOre;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.MathHelper;
import cn.nukkit.math.NukkitRandom;
import cn.wode490390.nukkit.skyplaingen.SkyPlainGeneratorPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SkyPlainGenerator extends Normal {

    protected static final float[] biomeWeights = new float[25];

    static {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                biomeWeights[i + 2 + (j + 2) * 5] = (float) (10f / Math.sqrt((i * i + j * j) + 0.2f));
            }
        }
    }

    protected ChunkManager level;
    protected NukkitRandom nukkitRandom;
    protected long localSeed1;
    protected long localSeed2;
    protected BiomeSelector selector;
    protected final List<Populator> populators = new ArrayList<>();
    protected final List<Populator> generationPopulators = new ArrayList<>();
    protected final ThreadLocal<float[]> depthRegion = ThreadLocal.withInitial(() -> null);
    protected final ThreadLocal<float[]> mainNoiseRegion = ThreadLocal.withInitial(() -> null);
    protected final ThreadLocal<float[]> minLimitRegion = ThreadLocal.withInitial(() -> null);
    protected final ThreadLocal<float[]> maxLimitRegion = ThreadLocal.withInitial(() -> null);
    protected final ThreadLocal<float[]> heightMap = ThreadLocal.withInitial(() -> new float[825]);
    protected NoiseGeneratorOctavesF minLimitPerlinNoise;
    protected NoiseGeneratorOctavesF maxLimitPerlinNoise;
    protected NoiseGeneratorOctavesF mainPerlinNoise;
    protected NoiseGeneratorPerlinF surfaceNoise;

    public SkyPlainGenerator() {
        super();
    }

    public SkyPlainGenerator(Map<String, Object> options) {
        super();
    }

    @Override
    public String getName() {
        return "skyplain";
    }

    @Override
    public ChunkManager getChunkManager() {
        return this.level;
    }

    @Override
    public void init(ChunkManager level, NukkitRandom random) {
        this.level = level;
        this.nukkitRandom = random;
        this.nukkitRandom.setSeed(this.level.getSeed());
        Random localRandom = ThreadLocalRandom.current();
        this.localSeed1 = localRandom.nextLong();
        this.localSeed2 = localRandom.nextLong();
        this.nukkitRandom.setSeed(this.level.getSeed());
        this.selector = new BiomeSelector(this.nukkitRandom);

        this.minLimitPerlinNoise = new NoiseGeneratorOctavesF(random, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctavesF(random, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctavesF(random, 8);
        this.surfaceNoise = new NoiseGeneratorPerlinF(random, 4);
        this.scaleNoise = new NoiseGeneratorOctavesF(random, 10);
        this.depthNoise = new NoiseGeneratorOctavesF(random, 16);

        PopulatorGroundCover cover = new PopulatorGroundCover();
        this.generationPopulators.add(cover);

        PopulatorOre ores = new PopulatorOre(STONE, new OreType[]{
                new OreType(Block.get(COAL_ORE), 20, 17, 0, 64),
                new OreType(Block.get(IRON_ORE), 20, 9, 0, 64),
                new OreType(Block.get(REDSTONE_ORE), 8, 8, 0, 64),
                new OreType(Block.get(LAPIS_ORE), 1, 7, 0, 64),
                new OreType(Block.get(GOLD_ORE), 2, 9, 0, 64),
                new OreType(Block.get(DIAMOND_ORE), 1, 8, 0, 64),
                new OreType(Block.get(DIRT), 10, 33, 0, 64),
                new OreType(Block.get(GRAVEL), 8, 33, 0, 64),
                new OreType(Block.get(STONE, BlockStone.GRANITE), 10, 33, 0, 64),
                new OreType(Block.get(STONE, BlockStone.DIORITE), 10, 33, 0, 64),
                new OreType(Block.get(STONE, BlockStone.ANDESITE), 10, 33, 0, 64)
        });
        this.populators.add(ores);
    }

    @Override
    public void generateChunk(int chunkX, int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        this.nukkitRandom.setSeed(chunkX * this.localSeed1 ^ chunkZ * this.localSeed2 ^ this.level.getSeed());

        BaseFullChunk chunk = this.level.getChunk(chunkX, chunkZ);

        float[] depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion.get(), chunkX * 4, chunkZ * 4, 5, 5, 200f, 200f, 0.5f);
        this.depthRegion.set(depthRegion);
        float[] mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion.get(), chunkX * 4, 0, chunkZ * 4, 5, 33, 5, 684.412f / 60f, 684.412f / 160f, 684.412f / 60f);
        this.mainNoiseRegion.set(mainNoiseRegion);
        float[] minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion.get(), chunkX * 4, 0, chunkZ * 4, 5, 33, 5, 684.412f, 684.412f, 684.412f);
        this.minLimitRegion.set(minLimitRegion);
        float[] maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion.get(), chunkX * 4, 0, chunkZ * 4, 5, 33, 5, 684.412f, 684.412f, 684.412f);
        this.maxLimitRegion.set(maxLimitRegion);
        float[] heightMap = this.heightMap.get();

        int horizCounter = 0;
        int vertCounter = 0;
        for (int xSeg = 0; xSeg < 5; ++xSeg) {
            for (int zSeg = 0; zSeg < 5; ++zSeg) {
                float heightVariationSum = 0;
                float baseHeightSum = 0;
                float biomeWeightSum = 0;
                Biome biome = this.pickBiome(baseX + (xSeg * 4), baseZ + (zSeg * 4));

                for (int xSmooth = -2; xSmooth <= 2; ++xSmooth) {
                    for (int zSmooth = -2; zSmooth <= 2; ++zSmooth) {
                        Biome biome1 = this.pickBiome(baseX + (xSeg * 4) + xSmooth, baseZ + (zSeg * 4) + zSmooth);
                        float baseHeight = biome1.getBaseHeight();
                        float heightVariation = biome1.getHeightVariation();
                        float scaledWeight = biomeWeights[xSmooth + 2 + (zSmooth + 2) * 5] / (baseHeight + 2f);
                        if (biome1.getBaseHeight() > biome.getBaseHeight()) {
                            scaledWeight /= 2f;
                        }
                        heightVariationSum += heightVariation * scaledWeight;
                        baseHeightSum += baseHeight * scaledWeight;
                        biomeWeightSum += scaledWeight;
                    }
                }

                heightVariationSum /= biomeWeightSum;
                baseHeightSum /= biomeWeightSum;
                heightVariationSum = heightVariationSum * 0.9f + 0.1f;
                baseHeightSum = (baseHeightSum * 4f - 1f) / 8f;
                float depthNoise = depthRegion[vertCounter] / 8000f;

                if (depthNoise < 0) {
                    depthNoise = -depthNoise * 0.3f;
                }

                depthNoise = depthNoise * 3f - 2f;

                if (depthNoise < 0) {
                    depthNoise /= 2f;

                    if (depthNoise < -1f) {
                        depthNoise = -1f;
                    }

                    depthNoise /= 1.4f;
                    depthNoise /= 2f;
                } else {
                    if (depthNoise > 1f) {
                        depthNoise = 1f;
                    }

                    depthNoise /= 8f;
                }

                ++vertCounter;
                float baseHeightClone = baseHeightSum;
                float heightVariationClone = heightVariationSum;
                baseHeightClone += depthNoise * 0.2f;
                baseHeightClone = baseHeightClone * 8.5f / 8f;
                float baseHeightFactor = 8.5f + baseHeightClone * 4f;

                for (int ySeg = 0; ySeg < 33; ++ySeg) {
                    float baseScale = (ySeg - baseHeightFactor) * 12f * 128f / 256f / heightVariationClone;

                    if (baseScale < 0) {
                        baseScale *= 4f;
                    }

                    float minScaled = minLimitRegion[horizCounter] / 512f;
                    float maxScaled = maxLimitRegion[horizCounter] / 512f;
                    float noiseScaled = (mainNoiseRegion[horizCounter] / 10f + 1f) / 2f;
                    float clamp = MathHelper.denormalizeClamp(minScaled, maxScaled, noiseScaled) - baseScale;

                    if (ySeg > 29) {
                        float yScaled = (ySeg - 29) / 3f;
                        clamp = clamp * (1f - yScaled) + -10f * yScaled;
                    }

                    heightMap[horizCounter] = clamp;
                    ++horizCounter;
                }
            }
        }

        for (int xSeg = 0; xSeg < 4; ++xSeg) {
            int xScale = xSeg * 5;
            int xScaleEnd = (xSeg + 1) * 5;

            for (int zSeg = 0; zSeg < 4; ++zSeg) {
                int zScale1 = (xScale + zSeg) * 33;
                int zScaleEnd1 = (xScale + zSeg + 1) * 33;
                int zScale2 = (xScaleEnd + zSeg) * 33;
                int zScaleEnd2 = (xScaleEnd + zSeg + 1) * 33;

                for (int ySeg = 0; ySeg < 32; ++ySeg) {
                    double height1 = heightMap[zScale1 + ySeg];
                    double height2 = heightMap[zScaleEnd1 + ySeg];
                    double height3 = heightMap[zScale2 + ySeg];
                    double height4 = heightMap[zScaleEnd2 + ySeg];
                    double height5 = (heightMap[zScale1 + ySeg + 1] - height1) * 0.125f;
                    double height6 = (heightMap[zScaleEnd1 + ySeg + 1] - height2) * 0.125f;
                    double height7 = (heightMap[zScale2 + ySeg + 1] - height3) * 0.125f;
                    double height8 = (heightMap[zScaleEnd2 + ySeg + 1] - height4) * 0.125f;

                    for (int yIn = 0; yIn < 8; ++yIn) {
                        double baseIncr = height1;
                        double baseIncr2 = height2;
                        double scaleY = (height3 - height1) * 0.25f;
                        double scaleY2 = (height4 - height2) * 0.25f;

                        for (int zIn = 0; zIn < 4; ++zIn) {
                            double scaleZ = (baseIncr2 - baseIncr) * 0.25f;
                            double scaleZ2 = baseIncr - scaleZ;

                            for (int xIn = 0; xIn < 4; ++xIn) {
                                if ((scaleZ2 += scaleZ) > 0) {
                                    //AIR
                                } else {
                                    int height = ySeg * 8 + yIn;
                                    if (height < seaHeight - 4) {
                                        chunk.setBlockId(xSeg * 4 + zIn, ySeg * 8 + yIn, zSeg * 4 + xIn, STONE);
                                    } else if (height < seaHeight) {
                                        chunk.setBlockId(xSeg * 4 + zIn, ySeg * 8 + yIn, zSeg * 4 + xIn, DIRT);
                                    } else if (height == seaHeight) {
                                        chunk.setBlockId(xSeg * 4 + zIn, ySeg * 8 + yIn, zSeg * 4 + xIn, GRASS);
                                    }
                                }
                            }
                            baseIncr += scaleY;
                            baseIncr2 += scaleY2;
                        }

                        height1 += height5;
                        height2 += height6;
                        height3 += height7;
                        height4 += height8;
                    }
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (SkyPlainGeneratorPlugin.getInstance().biome) {
                    chunk.setBiome(x, z, this.selector.pickBiome(baseX | x, baseZ | z));
                } else {
                    chunk.setBiome(x, z, EnumBiome.PLAINS.biome);
                }
            }
        }

        this.generationPopulators.forEach(populator -> populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk));
    }

    @Override
    public void populateChunk(int chunkX, int chunkZ) {
        BaseFullChunk chunk = this.level.getChunk(chunkX, chunkZ);
        this.nukkitRandom.setSeed(0xdeadbeef ^ (chunkX << 8) ^ chunkZ ^ this.level.getSeed());
        this.populators.forEach(populator -> populator.populate(this.level, chunkX, chunkZ, this.nukkitRandom, chunk));
        Biome.getBiome(chunk.getBiomeId(7, 7)).populateChunk(this.level, chunkX, chunkZ, this.nukkitRandom);
    }

    @Override
    public Biome pickBiome(int x, int z) {
        return this.selector.pickBiome(x, z);
    }
}
