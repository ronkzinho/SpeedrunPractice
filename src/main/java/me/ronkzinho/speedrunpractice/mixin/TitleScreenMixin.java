package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.screens.QuickSettingsScreen;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private ButtonWidget quickSettingsButton;

    protected TitleScreenMixin(Text title) {
        super(title);
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void initMixin(CallbackInfo info) {
        // If auto reset mode is on, instantly switch to create world menu.
        assert client != null;
        if (SpeedrunPractice.isPlaying) {
            this.client.openScreen(new QuickSettingsScreen(this));
        } else if (!this.client.isDemo()) {
            // Add new button for starting auto resets.
            int y = this.height / 4 + 48;
            quickSettingsButton = this.addButton(SpeedrunPractice.quickSettingsButton(this.width / 2 - 124, y + 24, button -> {
                QuickSettingsScreen quickSettingsScreen = new QuickSettingsScreen(this);
                if (hasShiftDown()) {
                    quickSettingsScreen.setForced(true);
                }
                this.client.openScreen(quickSettingsScreen);
            }));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void goldCarrotMixin (MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        assert this.client != null;
        this.client.getTextureManager().bindTexture(SpeedrunPractice.BUTTON_ICON_TEXTURE);
        drawTexture(matrices, quickSettingsButton.x + 2, quickSettingsButton.y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
        if (quickSettingsButton.isHovered()) {
            drawCenteredText(matrices, textRenderer, new TranslatableText(hasShiftDown() || SpeedrunPractice.worldName == null ? "speedrun-practice.quicksettings.title" : "speedrun-practice.startpracticesession"), quickSettingsButton.x + 10, quickSettingsButton.y - 17, 16777215);
        }
    }
}
