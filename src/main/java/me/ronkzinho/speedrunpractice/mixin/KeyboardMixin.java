package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.world.PracticeWorld;
import net.minecraft.client.Keyboard;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Redirect(method="processF3",at=@At(value="INVOKE",target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    private RegistryKey<World> resolveF3AndCWorld(World world){
        if(SpeedrunPractice.config.calcMode && SpeedrunPractice.isPlaying) {
            return PracticeWorld.dimensionToVanillaWorldKey.get(world.getDimension());
        }else{
            return world.getRegistryKey();
        }
    }
}
