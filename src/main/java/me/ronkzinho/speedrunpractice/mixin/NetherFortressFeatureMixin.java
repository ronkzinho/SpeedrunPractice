package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.NetherFortressFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherFortressFeature.class)
public class NetherFortressFeatureMixin {
    @Inject(method = "shouldStartAt(Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/world/gen/ChunkRandom;IILnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/gen/feature/DefaultFeatureConfig;)Z",cancellable = true,at=@At("HEAD"))
    private void shouldStartAt(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, ChunkRandom chunkRandom, int i, int j, Biome biome, ChunkPos chunkPos, DefaultFeatureConfig defaultFeatureConfig, CallbackInfoReturnable<Boolean> cir){
        if(!SpeedrunPractice.isPlaying) return;
        if(SpeedrunPractice.config.bastionRarity==60){
            cir.setReturnValue(chunkRandom.nextInt(5)<2);
        }else {
            cir.setReturnValue(chunkRandom.nextInt(100) < SpeedrunPractice.config.bastionRarity);
        }
    }
}
