package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion",at=@At("HEAD"))
    private void onGrantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir){
        if(SpeedrunPractice.autoSaveStater!=null) {
            SpeedrunPractice.autoSaveStater.onGrantCriterion(advancement, criterionName, this.owner.getServer());
        }
    }
}
