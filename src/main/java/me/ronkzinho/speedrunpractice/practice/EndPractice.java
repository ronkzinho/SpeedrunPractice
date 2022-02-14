package me.ronkzinho.speedrunpractice.practice;

import me.ronkzinho.speedrunpractice.IMinecraftServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class EndPractice extends Practice{
    @Override
    public int run(long seed) {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        ServerWorld world = null;
        //reset dragon fight data
        server.getSaveProperties().method_29037(new CompoundTag());
        ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(0);
        try {
            world = ((IMinecraftServer)server).createEndPracticeWorld(seed);
            player.setSpawnPoint(World.OVERWORLD,null,false,false);
            ServerWorld.createEndSpawnPlatform(world);
            Practice.resetPlayer(player);
            Practice.getInventory(player,"end");
            player.teleport(world,100,49,0,90,0);
            Practice.startSpeedrunIGTTimer();
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

}
