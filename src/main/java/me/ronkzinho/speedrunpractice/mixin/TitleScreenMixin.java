package me.ronkzinho.speedrunpractice.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.screens.QuickSettingsScreen;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Objects;


@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private ButtonWidget quickSettingsButton;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void initMixin(CallbackInfo info) {
        assert client != null;
        if (SpeedrunPractice.isPlaying) {
            this.client.openScreen(new QuickSettingsScreen(this));
        } else if (!this.client.isDemo()) {
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
            Text text = new TranslatableText(hasShiftDown() && Objects.requireNonNull(SpeedrunPractice.getCurrentProfile()).worldName != null ? "speedrun-practice.startpracticesession" : "speedrun-practice.quicksettings.title", SpeedrunPractice.getCurrentProfile() != null ? I18n.translate(SpeedrunPractice.getCurrentProfile().getMode().getTranslationKey()) : "");
            int x = quickSettingsButton.x + 10;
            int y = quickSettingsButton.y - 17;
            int textWidth = this.textRenderer.getWidth(text);
            fill(matrices, x - ((textWidth / 2) + 3), y - 3 , x + (textWidth / 2 + 3), y + ((this.textRenderer.fontHeight) + 3), BackgroundHelper.ColorMixer.getArgb((40 * 255), 0, 0, 0));
            drawCenteredText(matrices, textRenderer, text, x, y, 16777215);
        }
    }
}
