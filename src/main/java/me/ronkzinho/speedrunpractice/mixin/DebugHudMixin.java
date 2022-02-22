package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.world.PracticeWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
@Environment(EnvType.CLIENT)
public class DebugHudMixin {
    @Redirect(method="getLeftText",at=@At(value="INVOKE",target="Lnet/minecraft/client/world/ClientWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    private RegistryKey<World> resolveDebugWorldKey(ClientWorld world){
        return PracticeWorld.dimensionToVanillaWorldKey.get(world.getDimension());
    }

    @Inject(method = "getRightText", at=@At(value = "TAIL"))
    private void getLeftTextMixin(CallbackInfoReturnable<List<String>> cir){
        if(SpeedrunPractice.isPlaying){
            cir.getReturnValue().add("\n");
            cir.getReturnValue().add("SpeedRun Practice is currently active.");
        }
    }
}
