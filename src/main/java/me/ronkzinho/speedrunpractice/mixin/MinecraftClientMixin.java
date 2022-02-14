package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.QuickSettingsScreen;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract LevelStorage getLevelStorage();

    @Shadow public abstract void openScreen(@Nullable Screen screen);

    @Inject(method = "startIntegratedServer(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void startIntegratedServerMixin(String worldName, CallbackInfo ci) {
        if(SpeedrunPractice.isSelectingWorld){
            this.selectWorld(worldName);
            ci.cancel();
        }
    }

    public void selectWorld(String worldName){
        SpeedrunPractice.worldName = worldName;
        QuickSettingsScreen quickSettingsScreen = new QuickSettingsScreen(new TitleScreen());
        quickSettingsScreen.setForced(true);
        this.openScreen(quickSettingsScreen);
    }

    @Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
    public void openScreenMixin(Screen screen, CallbackInfo ci){
        if(SpeedrunPractice.isPlaying && screen instanceof TitleScreen){
            ci.cancel();
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), cancellable = true)
    public void disconnectMixin(Screen screen, CallbackInfo ci){
        if(SpeedrunPractice.isPlaying && (screen instanceof SaveLevelScreen || screen instanceof MultiplayerScreen)){
            ci.cancel();
        }
    }
}
