package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.world.PracticeWorld;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private List<ServerPlayerEntity> players;

    @ModifyVariable(method="respawnPlayer",at=@At(value="STORE"),ordinal =1)
    private ServerWorld modifySpawnWorld(ServerWorld serverWorld, ServerPlayerEntity player,boolean alive){
        ServerWorld serverWorld1 = player.getServerWorld();
        if(serverWorld1 instanceof PracticeWorld && serverWorld ==server.getOverworld()){
            return server.getWorld(((PracticeWorld) serverWorld1).associatedWorlds.get(World.OVERWORLD));
        }else{
            return serverWorld;
        }
    }

    @Inject(method = "onPlayerConnect",at=@At("TAIL"))
    private void showWelcomeMessage(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        if(SpeedrunPractice.selectingWorldParent != null){
            MinecraftClient.getInstance().submit(() -> {
                SpeedrunPractice.selectingWorldParent.setForced(true);
                SpeedrunPractice.selectingWorldParent.setCustomDone(button -> {
                    SpeedrunPractice.isPlaying = true;
                    welcome(player);
                });
                SpeedrunPractice.selectingWorldParent.server = this.server;
                MinecraftClient.getInstance().openScreen(SpeedrunPractice.selectingWorldParent);
            });
        }
        if(!SpeedrunPractice.isPlaying) return;
        welcome(player);
    }

    public void welcome(ServerPlayerEntity player) {
        try{
            if(!SpeedrunPractice.welcomeShown)
                SpeedrunPractice.sendWelcomeMessage(player);
            SpeedrunPractice.welcomeShown = true;
            this.server.submit(SpeedrunPractice::practice);
        }catch(Exception ignored){}
    }
}
