package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "disconnect*", at = @At("HEAD"), cancellable = true)
    public void disconnectMixin(CallbackInfo ci){
        if(SpeedrunPractice.isPlaying){
            ci.cancel();
            this.client.openScreen(null);
            this.client.mouse.lockCursor();
            this.client.submit(() -> {
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        client.getServer().submit(SpeedrunPractice::practice);
                    }
                };
                thread.start();
            });
        }
    }
}
