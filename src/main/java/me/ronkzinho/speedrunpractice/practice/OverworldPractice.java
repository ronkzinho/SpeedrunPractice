package me.ronkzinho.speedrunpractice.practice;

import me.ronkzinho.speedrunpractice.IMinecraftServer;
import me.ronkzinho.speedrunpractice.world.PracticeWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

import java.io.IOException;
import java.util.Map;

public class OverworldPractice extends Practice {
    @Override
    public int run(long seed) {
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftServer server = client.getServer();
        ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(0);
        Map<RegistryKey<DimensionType>, PracticeWorld> linkedPracticeWorld = null;
        try {
            linkedPracticeWorld = ((IMinecraftServer) server).createLinkedPracticeWorld(seed);
        } catch (IOException e) {
            return 0;
        }
        player.inventory.clear();
        server.getCommandManager().execute(server.getCommandSource().withSilent(),"/advancement revoke @a everything");
        PracticeWorld overworld = linkedPracticeWorld.get(DimensionType.OVERWORLD_REGISTRY_KEY);
        overworld.getChunkManager().addTicket(ChunkTicketType.START,new ChunkPos(overworld.getSpawnPos()),11, Unit.INSTANCE);
        setSpawnPos(overworld,player);
        player.teleport(overworld,overworld.getSpawnPos().getX(),overworld.getSpawnPos().getY(),overworld.getSpawnPos().getZ(),0,0);
        resetPlayer(player);
        getInventory(player, "overworld");
        startSpeedrunIGTTimer();
        resetScreen();
        return 1;
    }

}

