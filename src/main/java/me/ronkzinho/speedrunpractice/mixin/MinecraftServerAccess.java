package me.ronkzinho.speedrunpractice.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccess {
    @Invoker("shouldKeepTicking")
    boolean invokeShouldKeepTicking();
    @Accessor
    LevelStorage.Session getSession();
}
