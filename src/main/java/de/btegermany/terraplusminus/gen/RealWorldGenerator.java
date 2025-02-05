package de.btegermany.terraplusminus.gen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import de.btegermany.terraplusminus.Terraplusminus;
import de.btegermany.terraplusminus.gen.tree.TreePopulator;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.ChunkDataLoader;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.substitutes.BlockState;
import net.buildtheearth.terraminusminus.substitutes.BukkitBindings;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class RealWorldGenerator extends ChunkGenerator {
    private Location spawnLocation = null;

    EarthGeneratorSettings settings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
    ChunkDataLoader loader;
    public LoadingCache<ChunkPos, CompletableFuture<CachedChunkData>> cache = null;
    private CustomBiomeProvider customBiomeProvider;
    String houses;
    String streets;
    String paths;
    String surface;
    int xOffset;
    int yOffset;
    int zOffset;

    public RealWorldGenerator() {
        this.loader = new ChunkDataLoader(settings);
        this.customBiomeProvider = new CustomBiomeProvider();
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(5L, TimeUnit.MINUTES)
                .softValues()
                .build(new ChunkDataLoader(this.settings));
        this.houses = Terraplusminus.config.getString("building_outlines_material");
        this.streets = Terraplusminus.config.getString("road_material");
        this.paths = Terraplusminus.config.getString("path_material");
        this.surface = Terraplusminus.config.getString("surface_material");
        this.xOffset = Terraplusminus.config.getInt("terrain_offset.x");
        this.yOffset = Terraplusminus.config.getInt("terrain_offset.y");
        this.zOffset = Terraplusminus.config.getInt("terrain_offset.z");
    }


    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkData chunkData) {

    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return this.customBiomeProvider;
    }

    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        CompletableFuture<CachedChunkData> future = this.cache.getUnchecked(new ChunkPos(chunkX - (xOffset / 16), chunkZ - (zOffset / 16)));
        //pollAsyncCubePopulator(chunkX,chunkZ);
        generateSurface(worldInfo, future, chunkData, yOffset);
                /* TRY TO INCREASE GENERATING PERFORMANCE

                int minChunkSurface = chunkData.getMinHeight();
                chunkData.setRegion(0, minY,0,16,minChunkSurface,16, Material.STONE);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = minChunkSurface; y < Math.min(maxY, groundY+move); y++) chunkData.setBlock(x, y, z, Material.STONE);
                    }
                }

                 */

/*
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                    int move = Terraplusminus.config.getInt("terrain_offset");
                        int groundY = 200;
                        chunkData.setBlock(x, groundY, z, Material.GRASS_BLOCK);
                        for (int y = minY; y < Math.min(maxY, groundY+move); y++) chunkData.setBlock(x, y, z, Material.STONE);
                    }
                }

 */

        //   });
    }
/*
    public CompletableFuture<Void> getChunkAsync(World world, int x, int z) {
        return CompletableFuture.allOf(PaperLib.getChunkAtAsync(world, x, z));
    }

    private ChunkPos getChunkPos(World world,int chunkX, int chunkZ) throws ExecutionException, InterruptedException {
        CompletableFuture<Chunk> chunk = PaperLib.getChunkAtAsync(world, chunkX, chunkZ);
        chunk.thenAccept(marked -> {Bukkit.getServer().getWorld(String.valueOf(world)).setChunkForceLoaded(chunkX, chunkZ, true); });

        ChunkPos pos = new ChunkPos(chunk.get().getX(),chunk.get().getZ());
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(5L, TimeUnit.MINUTES)
                .softValues()
                .build(new ChunkDataLoader(this.settings));
        CompletableFuture<CachedChunkData> future = this.cache.get(pos);
        return pos;
    }*/

    private void generateSurface(@NotNull WorldInfo worldInfo, CompletableFuture<CachedChunkData> future, @NotNull ChunkData chunkData, int yOffset) {
        final int minY = worldInfo.getMinHeight();
        final int maxY = worldInfo.getMaxHeight();
        Material material = Material.getMaterial(surface);

        try {

            CachedChunkData terraData = future.get(5L, TimeUnit.MINUTES);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    int groundY = terraData.groundHeight(x, z);
                    int waterY = terraData.waterHeight(x, z);
                    BlockState state = terraData.surfaceBlock(x, z);

                    // Sets block on mountains over 1700m to stone
                    int randomizer = (int) Math.floor(Math.random() * (1700 - 1695 + 1) + 1695);
                    if (groundY >= randomizer) {
                        material = Material.STONE;
                    }
                    //--------------------------------------------------------

                    //Generates sand in deserts and snow on mountains
                    switch ((int) customBiomeProvider.getBiome()) {
                        case 4:
                            material = Material.SAND;
                            break;
                        case 28, 29, 30:
                            material = Material.SNOW_BLOCK;
                            break;
                    }

                    //Generates stone under all surfaces
                    for (int y = minY; y < Math.min(maxY, groundY + yOffset); y++)
                        chunkData.setBlock(x, y, z, Material.STONE);


                    //Genrates terrain with block states
                    if (groundY + yOffset < maxY) {
                        if (state != null) {
                            BlockData blockData = BukkitBindings.getAsBlockData(state);
                            if (blockData != null) {
                                //System.out.println(state.getBlock().toString());
                                switch (state.getBlock().toString()) {
                                    case "minecraft:gray_concrete":
                                        chunkData.setBlock(x, groundY + yOffset, z, Material.getMaterial(streets));
                                        break;
                                    case "minecraft:dirt_path":
                                        chunkData.setBlock(x, groundY + yOffset, z, Material.getMaterial(paths));
                                        break;
                                    case "minecraft:bricks":
                                        chunkData.setBlock(x, groundY + yOffset, z, Material.getMaterial(houses));
                                        break;
                                    default:
                                        chunkData.setBlock(x, groundY + yOffset, z, BukkitBindings.getAsBlockData(state));
                                        break;
                                }
                            } else {
                                chunkData.setBlock(x, groundY + yOffset, z, material);
                            }
                        } else {
                            chunkData.setBlock(x, groundY + yOffset, z, material);
                        }

                    }
                    for (int y = groundY + yOffset + 1; y <= Math.min(maxY, waterY + yOffset); y++) {
                        chunkData.setBlock(x, y, z, Material.WATER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pollAsyncCubePopulator(int chunkX, int chunkZ) {
        //ensure that all columns required for population are ready to go
        // checking all neighbors here improves performance when checking if a cube can be generated
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                this.cache.getUnchecked(new ChunkPos(chunkX + dx, chunkZ + dz));
            }
        }
    }

    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.ChunkData chunkData) {
        // no bedrock, because bedrock bad
    }

    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.ChunkData chunkData) {
        // no caves, because caves scary
    }


    public int getBaseHeight(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull HeightMap heightMap) {
        throw new UnsupportedOperationException("Not implemented");
    }


    @NotNull
    @Deprecated
    public ChunkGenerator.ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.BiomeGrid biome) {

        throw new UnsupportedOperationException("Custom generator " + getClass().getName() + " is missing required method generateChunkData");
    }


    public boolean canSpawn(@NotNull World world, int x, int z) {
        Block highest = world.getBlockAt(x, world.getHighestBlockYAt(x, z), z);

        switch (world.getEnvironment()) {
            case NETHER:
                return true;
            case THE_END:
                return highest.getType() != Material.AIR && highest.getType() != Material.WATER && highest.getType() != Material.LAVA;
            case NORMAL:
            default:
                return highest.getType() == Material.SAND || highest.getType() == Material.GRAVEL;
        }
    }

    @NotNull
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return Arrays.asList(new TreePopulator(customBiomeProvider));
    }

    @Nullable
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        if (spawnLocation == null)
            spawnLocation = new Location(world, 3517417, 58, -5288234);
        return spawnLocation;
    }

    @Deprecated
    public boolean isParallelCapable() {
        return false;
    }


    public boolean shouldGenerateNoise() {
        return false;
    }


    public boolean shouldGenerateSurface() {
        return false;
    }


    public boolean shouldGenerateBedrock() {
        return false;
    }


    public boolean shouldGenerateCaves() {
        return false;
    }


    public boolean shouldGenerateDecorations() {
        return false;
    }


    public boolean shouldGenerateMobs() {
        return false;
    }

    public boolean shouldGenerateStructures() {
        return false;
    }


    /*@NotNull
    public ChunkGenerator.ChunkData createVanillaChunkData(@NotNull World world, int x, int z) {
        var chunk = Bukkit.getServer().createChunkData(world);

        Field maxHeightField = null;
        try {
            maxHeightField = chunk.getClass().getDeclaredField("maxHeight");
            maxHeightField.setAccessible(true);
            maxHeightField.set(chunk, 2032);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return chunk;

    }*/
    // Paper


}
