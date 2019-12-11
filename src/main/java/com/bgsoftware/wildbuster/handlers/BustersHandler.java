package com.bgsoftware.wildbuster.handlers;

import com.bgsoftware.wildbuster.api.handlers.BustersManager;
import com.bgsoftware.wildbuster.api.objects.BlockData;
import com.bgsoftware.wildbuster.api.objects.ChunkBuster;
import com.bgsoftware.wildbuster.api.objects.PlayerBuster;
import com.bgsoftware.wildbuster.objects.WBlockData;
import com.bgsoftware.wildbuster.objects.WChunkBuster;
import com.bgsoftware.wildbuster.objects.WPlayerBuster;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BustersHandler implements BustersManager {

    private final Map<String, ChunkBuster> chunkBusters = new HashMap<>();
    private final Map<ChunkPosition, PlayerBuster> playerBusters = new HashMap<>();
    private final Map<UUID, PlayerBuster> notifyBusters = new HashMap<>();

    @Override
    public ChunkBuster getChunkBuster(String name){
        return chunkBusters.get(name.toLowerCase());
    }

    @Override
    public ChunkBuster getChunkBuster(ItemStack item){
        return chunkBusters.values().stream().filter(chunkBuster -> chunkBuster.getBusterItem().isSimilar(item)).findFirst().orElse(null);
    }

    @Override
    public List<ChunkBuster> getChunkBusters(){
        return new ArrayList<>(chunkBusters.values());
    }

    @Override
    public PlayerBuster getPlayerBuster(Chunk chunk){
        return playerBusters.get(new ChunkPosition(chunk));
    }

    @Override
    public boolean isChunkBusted(Chunk chunk){
        return getPlayerBuster(chunk) != null;
    }

    @Override
    public List<PlayerBuster> getPlayerBusters(){
        return new ArrayList<>(playerBusters.values());
    }

    @Override
    public List<PlayerBuster> getPlayerBusters(OfflinePlayer player){
        return playerBusters.values().stream()
                .filter(buster -> buster.getUniqueID().equals(player.getUniqueId()))
                .collect(Collectors.toList());
    }

    @Override
    public ChunkBuster createChunkBuster(String name, int radius, ItemStack busterItem) {
        ChunkBuster chunkBuster = new WChunkBuster(name, radius, busterItem);
        chunkBusters.put(name, chunkBuster);
        return chunkBuster;
    }

    @Override
    public void removeChunkBusters() {
        chunkBusters.clear();
    }

    @Override
    public PlayerBuster createPlayerBuster(Player player, Location placedLocation, ChunkBuster buster) {
        PlayerBuster playerBuster = new WPlayerBuster(player, placedLocation, buster);
        playerBuster.getChunks().forEach(chunk -> playerBusters.put(new ChunkPosition(chunk), playerBuster));
        return playerBuster;
    }

    @Override
    public PlayerBuster loadPlayerBuster(String busterName, UUID uuid, World world, boolean cancelStatus, boolean notifyStatus, int currentLevel, List<Chunk> chunksList, List<BlockData> removedBlocks) {
        PlayerBuster playerBuster = new WPlayerBuster(busterName, uuid, world, cancelStatus, notifyStatus, currentLevel, chunksList, removedBlocks);
        chunksList.forEach(chunk -> playerBusters.put(new ChunkPosition(chunk), playerBuster));
        return playerBuster;
    }

    @Override
    public void removePlayerBuster(PlayerBuster buster){
        buster.getChunks().forEach(chunk -> playerBusters.remove(new ChunkPosition(chunk)));
        notifyBusters.remove(buster.getUniqueID());
    }

    @Override
    public void setNotifyBuster(PlayerBuster buster){
        notifyBusters.put(buster.getUniqueID(), buster);
    }

    @Override
    public PlayerBuster getNotifyBuster(UUID uuid){
        return notifyBusters.get(uuid);
    }

    @Override
    public BlockData getBlockData(Block block) {
        return new WBlockData(block);
    }

    private static final class ChunkPosition{

        private String world;
        private int x, z;

        ChunkPosition(Chunk chunk){
            this.world = chunk.getWorld().getName();
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x &&
                    z == that.z &&
                    Objects.equals(world, that.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, z);
        }
    }

}
