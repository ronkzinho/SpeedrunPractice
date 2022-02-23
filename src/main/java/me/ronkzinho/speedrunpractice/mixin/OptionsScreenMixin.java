package me.ronkzinho.speedrunpractice.mixin;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.screens.QuickSettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    @Shadow @Final private Screen parent;
    ButtonWidget quickSettingsButton;

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method= "init", at=@At("TAIL"))
    public void addConfigButton(CallbackInfo ci){
        ButtonWidget options = this.addButton(new ButtonWidget(this.width/2 + 5,this.height / 6 + 138,150,20, new TranslatableText("speedrun-practice.options"),(buttonWidget)->{
            MinecraftClient.getInstance().openScreen(SpeedrunPractice.config.getScreen(this.parent));
        }));

        options.visible = SpeedrunPractice.isPlaying || this.client.world == null;
        if (SpeedrunPractice.isPlaying) {
            // Get menu.stop_resets text or set to default
            Text text;
            if (Language.getInstance().get("menu.stop_practicing").equals("menu.stop_practicing")) {
                text = new LiteralText("Stop Practicing & Quit");
            } else {
                text = new TranslatableText("menu.stop_practicing");
            }

            //Add button to disable the auto reset and quit
            this.addButton(new ButtonWidget(0, this.height - 20, 125, 20, text, (buttonWidget) -> {
                SpeedrunPractice.isPlaying = false;
                buttonWidget.active = false;
                this.client.world.disconnect();
                this.client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
                this.client.openScreen(new TitleScreen());
            }));

            quickSettingsButton = this.addButton(SpeedrunPractice.quickSettingsButton(this.width / 2 - 180, this.height / 6 + 42, button -> {
                this.client.openScreen(new QuickSettingsScreen(this, this.client.getServer()));
            }));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void goldCarrotMixin (MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        assert this.client != null;
        if(quickSettingsButton != null && quickSettingsButton.visible) {
            this.client.getTextureManager().bindTexture(SpeedrunPractice.BUTTON_ICON_TEXTURE);
            drawTexture(matrices, quickSettingsButton.x + 2, quickSettingsButton.y + 2, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    }
}
