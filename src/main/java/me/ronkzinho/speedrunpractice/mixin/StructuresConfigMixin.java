package me.ronkzinho.speedrunpractice.mixin;

import com.google.common.collect.Maps;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

import static net.minecraft.world.gen.chunk.StructuresConfig.DEFAULT_STRUCTURES;

@Mixin(StructuresConfig.class)
public class StructuresConfigMixin {
    @Redirect(method = "<init>(Z)V",at=@At(value="FIELD",target ="Lnet/minecraft/world/gen/chunk/StructuresConfig;structures:Ljava/util/Map;",opcode = Opcodes.PUTFIELD))
    private void modifyStructures(StructuresConfig sc, Map<StructureFeature<?>, StructureConfig> structures){
        if(!SpeedrunPractice.isPlaying){
            ((StructuresConfigAccess)sc).setStructures(Maps.newHashMap(DEFAULT_STRUCTURES));
            return;
        }
        ((StructuresConfigAccess)sc).setStructures(SpeedrunPractice.overworldStructures);
    }
}
