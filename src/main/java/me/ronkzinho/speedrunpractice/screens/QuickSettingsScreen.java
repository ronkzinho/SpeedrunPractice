package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.Arrays;
import java.util.List;

public class QuickSettingsScreen extends Screen {
    private boolean forced = false;
    public MinecraftServer server = null;
    public int spacingY = 24;
    protected ButtonWidget.PressAction customDone = null;
    public final Screen parent;
    protected QuickSettingsOptions options;
    protected String seedText;

    public QuickSettingsScreen(Screen parent, boolean forced){
        super(new LiteralText("Quick Settings"));
        this.parent = parent;
        this.forced = forced;
    }

    public void setCustomDone(ButtonWidget.PressAction onPress){
        this.customDone = onPress;
    }

    public QuickSettingsScreen(Screen parent) {
        super(new TranslatableText("speedrun-practice.quicksettings.title"));
        this.parent = parent;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();
        if(this.parent instanceof SelectWorldScreen) SpeedrunPractice.worldName = null;
        SpeedrunPractice.selectingWorldParent = null;
        this.options = new QuickSettingsOptions(this, 250, 20, (this.width / 2) - (250 / 2), (this.height / 4) + (spacingY / 5), spacingY, this.textRenderer);
        this.initWidgets();
    }

    protected void initWidgets(){
        assert this.client != null;
        if (SpeedrunPractice.worldName != null && !forced) {
            SpeedrunPractice.isPlaying = true;
            this.client.method_29970(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
            this.client.startIntegratedServer(SpeedrunPractice.worldName);
        }

        options.selectWorld.visible = this.client.world == null || this.server != null;
        options.clearWorld.visible = options.selectWorld.visible && SpeedrunPractice.worldName != null;
        options.inventoryButton.visible = !options.selectWorld.visible;

        options.seed.setText(SpeedrunPractice.practiceSeedText);
        options.seed.setChangedListener(string -> this.seedText = options.seed.getText());
        options.done.active = SpeedrunPractice.worldName != null;

        this.children.addAll(options.all);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.children.forEach(element -> ((Drawable) element).render(matrices, mouseX, mouseY, delta));
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void setForced(boolean forced){
        this.forced = forced;
    }
}
