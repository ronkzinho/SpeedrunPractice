package me.ronkzinho.speedrunpractice.practice;

import me.ronkzinho.speedrunpractice.IMinecraftServer;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.world.PracticeWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.StructureFeature;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class PostBlindPractice extends Practice {
    @Override
    public int run(long seed) {
        int maxDist = SpeedrunPractice.config.defaultMaxDist;
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(0);
        Map<RegistryKey<DimensionType>, PracticeWorld> linkedPracticeWorld = null;
        try {
            linkedPracticeWorld = ((IMinecraftServer) server).createLinkedPracticeWorld(seed);
        } catch (IOException e) {
            return 0;
        }
        server.getCommandManager().execute(server.getCommandSource().withSilent(),"/advancement revoke @a everything");
        PracticeWorld overworld = linkedPracticeWorld.get(DimensionType.OVERWORLD_REGISTRY_KEY);
        if(SpeedrunPractice.config.postBlindSpawnChunks)
            overworld.getChunkManager().addTicket(ChunkTicketType.START,new ChunkPos(overworld.getSpawnPos()),11, Unit.INSTANCE);
        setSpawnPos(overworld,player);
        BlockPos overworldPos = this.getOverworldPos(overworld,maxDist,new Random(seed));
        createPortals(linkedPracticeWorld, player, overworld, overworldPos);
        //this needs to be a server task so the portal gets added to poi storage before the changeDimension call
        long finalSeed = seed;
        server.execute(()-> {
            player.teleport(overworld,overworldPos.getX(),overworldPos.getY(),overworldPos.getZ(),90,0);//makes sure chunks are loaded at the overworld position
            player.refreshPositionAndAngles(overworldPos,90,0);
            resetPlayer(player);
            getInventory(player, "postblind");
            if(SpeedrunPractice.config.randomisePostBlindInventory)Practice.populatePostBlindInventory(player, finalSeed);
            player.changeDimension(overworld);
            Practice.startSpeedrunIGTTimer();
        });
        return 1;
    }

    private BlockPos getOverworldPos(PracticeWorld overworld,int maxDist, Random random) {
        ChunkPos strongholdLoc = new ChunkPos(overworld.getChunkManager().getChunkGenerator().locateStructure(overworld, StructureFeature.STRONGHOLD,new BlockPos(0,0,0),100,false));
        double angle = random.nextDouble() * 2*Math.PI;
        int dist = maxDist >0 ?random.nextInt(maxDist) : 0;
        int x = strongholdLoc.getStartX()+8+(int)Math.round(Math.cos(angle) * dist);
        int z = strongholdLoc.getStartZ()+8+(int)Math.round(Math.sin(angle) * dist);
        int y = overworld.getChunk(x >> 4, z >> 4).sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15);
        if(SpeedrunPractice.config.caveSpawns) {
            y = random.nextInt(y) + 20;
        }
        return new BlockPos(x,y,z);
    }
}
