package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(HoldingPatternPhase.class)
public class HoldingPatternPhaseMixin {
    @ModifyArgs(method = "method_6841", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;findPath(IILnet/minecraft/entity/ai/pathing/PathNode;)Lnet/minecraft/entity/ai/pathing/Path;"))
    public void changeNodeMixin(Args args){
        if(!SpeedrunPractice.isPlaying) return;
        if(args.get(2) != null) return;
        int originalFrom = args.get(0);
        int originalTo = args.get(1);
        int from = SpeedrunPractice.config.dragonType.node != null ? SpeedrunPractice.config.dragonType.node : originalFrom;
        Integer to = SpeedrunPractice.nodes.get(SpeedrunPractice.config.dragonType).get(SpeedrunPractice.config.nodePosition.ordinal());
        to = to != null ? to : originalTo;
        args.set(0, from);
        args.set(1, to);
    }
}
