package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;

public class QuickSettingsOptions {
    public QuickSettingsScreen parent;
    public ButtonWidget mode;
    public ButtonWidget selectWorld;
    public ButtonWidget clearWorld;
    public ButtonWidget inventoryButton;
    public TextFieldWidget seed;
    public ButtonWidget options;
    public ButtonWidget cancel;
    public ButtonWidget done;

    public QuickSettingsOptions(QuickSettingsScreen parent, int width, int height, int x, int y, int spacingY, TextRenderer textRenderer) {
        this.parent = parent;

        this.mode = this.addToAll(new ButtonWidget(x, y, width, height, this.getModeText(SpeedrunPractice.practiceMode), button -> {
            SpeedrunPractice.practiceMode = SpeedrunPractice.practiceMode.next();
            button.setMessage(this.getModeText(SpeedrunPractice.practiceMode));
        }));

        this.selectWorld = this.addToAll(new ButtonWidget(x, spacingY + y, width - (SpeedrunPractice.worldName != null ? height : 0), height, new TranslatableText(SpeedrunPractice.worldName != null ? "speedrun-practice.quickoptions.selectedworld" : parent.server != null ? "speedrun-practice.quickoptions.selectcurrentworld" : "speedrun-practice.quickoptions.selectworld", SpeedrunPractice.worldName), button -> {
            if(parent.server != null){
                SpeedrunPractice.worldName = parent.server.getSaveProperties().getLevelName();
                this.done.active = true;
            }
            else{
                SpeedrunPractice.selectingWorldParent = parent;
                MinecraftClient.getInstance().openScreen(new SelectWorldScreen(parent));
            }
        }));

        this.clearWorld = this.addToAll(new ButtonWidget(selectWorld.x + selectWorld.getWidth(), selectWorld.y, height, height, new LiteralText("X"), button -> {
            SpeedrunPractice.worldName = null;
            button.visible = false;
            MinecraftClient.getInstance().openScreen(new QuickSettingsScreen(parent.parent));
        }));

        this.inventoryButton = this.addToAll(new ButtonWidget(x, selectWorld.y, width, height, new LiteralText("Inventory Management"), button -> MinecraftClient.getInstance().openScreen(new InventoryManagementScreen(parent))));

        this.seed = this.addToAll(new TextFieldWidget(textRenderer, x, spacingY * 2 + y, width, height, new TranslatableText("speedrun-practice.quickoptions.seed")));

        this.options = this.addToAll(new ButtonWidget(x, spacingY * 3 + y, width, height, new TranslatableText("speedrun-practice.options"), button -> {
            MinecraftClient.getInstance().openScreen(SpeedrunPractice.config.getScreen(parent));
        }));

        this.done = this.addToAll(new ButtonWidget( x, spacingY * 4 + y, width / 2 - 4, height, ScreenTexts.DONE, parent.customDone != null ? parent.customDone : button -> {
            if(parent.seedText != null && !parent.seedText.isEmpty()){
                SpeedrunPractice.practiceSeedText = parent.seedText;
            }
            else if(SpeedrunPractice.practiceSeedText != null){
                SpeedrunPractice.practiceSeedText = null;
            }
            if(MinecraftClient.getInstance().world != null){
                MinecraftClient.getInstance().openScreen(parent);
                MinecraftClient.getInstance().mouse.lockCursor();
                return;
            }
            SpeedrunPractice.isPlaying = true;
            MinecraftClient.getInstance().method_29970(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
            MinecraftClient.getInstance().startIntegratedServer(SpeedrunPractice.worldName);
        }));

        this.cancel = this.addToAll(new ButtonWidget(done.x + done.getWidth() + 10, done.y, width / 2 - 4, height, ScreenTexts.CANCEL, button -> {
            MinecraftClient.getInstance().openScreen((this.parent.server != null || MinecraftClient.getInstance().world != null) ? null : this.parent.parent);
        }));
    }

    public ArrayList<Element> all = new ArrayList<>();

    public <T extends AbstractButtonWidget> T addToAll(T button){
        this.all.add(button);
        return button;
    }

    private Text getModeText(SpeedrunPractice.PracticeMode mode) {
        return new TranslatableText("speedrun-practice.quickoptions.mode").append(": ").append(new TranslatableText(mode.getTranslationKey()));
    }

}
