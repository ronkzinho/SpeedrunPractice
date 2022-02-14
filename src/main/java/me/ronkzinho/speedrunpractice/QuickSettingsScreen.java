package me.ronkzinho.speedrunpractice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.options.CyclingOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;

import java.util.OptionalLong;


public class QuickSettingsScreen extends Screen {
    private boolean forced = false;
    private final Screen parent;
    private TextFieldWidget seedTextField;
    private String seedText;
    private ButtonListWidget list;
    private boolean inGame = false;

    public QuickSettingsScreen(Screen parent, boolean forced, boolean inGame){
        super(new LiteralText("Quick Settings"));
        this.parent = parent;
        this.forced = forced;
        this.inGame = inGame;
    }

    public QuickSettingsScreen(Screen parent) {
        super(new LiteralText("Quick Settings"));
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
        this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        super.init();
        this.initWidgets();
    }

    protected void initWidgets(){
        assert this.client != null;
        if(this.parent instanceof SelectWorldScreen) SpeedrunPractice.worldName = null;
        int width = 310;
        int x = (this.width / 2) - (width / 2);
        SpeedrunPractice.isSelectingWorld = false;
        if (SpeedrunPractice.worldName != null && !forced) {
            SpeedrunPractice.isPlaying = true;
            this.client.method_29970(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
            this.client.startIntegratedServer(SpeedrunPractice.worldName);
        }
        this.list.addSingleOptionEntry(new CyclingOption("speedrun-practice.quickoptions.mode", ((gameOptions, integer) ->
                SpeedrunPractice.practiceMode = SpeedrunPractice.practiceMode.next()
        ), (gameOptions, cyclingOption) -> {
            TranslatableText translatableText = new TranslatableText(SpeedrunPractice.practiceMode.getTranslationKey());
            return cyclingOption.getDisplayPrefix().append(translatableText);
        }));
        ButtonWidget selectWorld = this.addButton(new ButtonWidget(x, 60, width - (SpeedrunPractice.worldName != null ? 20 : 0), 20, new LiteralText(SpeedrunPractice.worldName != null ? "Selected world: " + SpeedrunPractice.worldName : "Select world"), button -> {
            SpeedrunPractice.isSelectingWorld = true;
            this.client.method_29970(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
            this.client.openScreen(new SelectWorldScreen(this));
        }));
        selectWorld.visible = !inGame;
        ButtonWidget clearWorld = this.addButton(new ButtonWidget(selectWorld.x + selectWorld.getWidth(), selectWorld.y, 20, 20, new LiteralText("X"), button -> {
            SpeedrunPractice.worldName = null;
            button.visible = false;
            this.client.openScreen(new QuickSettingsScreen(this.parent));
        }));
        clearWorld.visible = selectWorld.visible && SpeedrunPractice.worldName != null;
        ButtonWidget inventoryButton = this.addButton(new ButtonWidget(x, 60, width, 20, new LiteralText("Inventory Management"), button -> this.client.openScreen(new InventoryManagementScreen(this))));
        inventoryButton.visible = this.inGame;
        this.seedTextField = this.addChild(new TextFieldWidget(this.textRenderer, x, 85, width, 20, new TranslatableText("speedrun-practice.quickoptions.seed")));
        this.seedTextField.setText(SpeedrunPractice.practiceSeedText);
        this.seedTextField.setChangedListener(string -> {
            this.seedText = this.seedTextField.getText();
        });

        ButtonWidget done = this.addButton(new ButtonWidget(x, 110, width, 20, ScreenTexts.DONE, button -> {
            if(this.seedText != null && !this.seedText.isEmpty()){
                SpeedrunPractice.practiceSeedText = this.seedText;
            }
            else if(SpeedrunPractice.practiceSeedText != null){
                SpeedrunPractice.practiceSeedText = null;
            }
            if(this.inGame){
                this.client.openScreen(parent);
                this.client.mouse.lockCursor();
                return;
            }
            SpeedrunPractice.isPlaying = true;
            this.client.method_29970(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
            this.client.startIntegratedServer(SpeedrunPractice.worldName);
        }));
        done.active = SpeedrunPractice.worldName != null;
        this.children.add(list);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        this.seedTextField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void setForced(boolean forced){
        this.forced = forced;
    }
}
