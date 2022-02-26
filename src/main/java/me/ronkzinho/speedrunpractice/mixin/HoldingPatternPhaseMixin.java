package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mixin(HoldingPatternPhase.class)
public class HoldingPatternPhaseMixin {
    @ModifyArgs(method = "method_6841", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;findPath(IILnet/minecraft/entity/ai/pathing/PathNode;)Lnet/minecraft/entity/ai/pathing/Path;"))
    public void changeNodeMixin(Args args){
        if(!SpeedrunPractice.isPlaying) return;
        if(args.get(2) != null) return;
        int originalFrom = args.get(0);
        int originalTo = args.get(1);
        int from = SpeedrunPractice.config.dragonType.node != null ? SpeedrunPractice.config.dragonType.node : originalFrom;
        Integer to = SpeedrunPractice.nodes.get(Arrays.stream(SpeedrunPractice.DragonType.values()).filter(dragonType -> Objects.equals(dragonType.node, SpeedrunPractice.config.nodePosition.equals(SpeedrunPractice.NodePosition.BOTH) ? null : from)).findFirst().orElseGet(() -> SpeedrunPractice.config.dragonType)).get(SpeedrunPractice.config.nodePosition.ordinal());
        args.set(0, from);
        args.set(1, to != null ? to : originalTo);
    }
}
