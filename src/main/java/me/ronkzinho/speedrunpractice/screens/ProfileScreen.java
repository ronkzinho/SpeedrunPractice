package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.config.ProfileConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ProfileScreen extends Screen {
    private final Screen parent;
    private final int bwidth = 250; /*b from button*/
    private final int bheight = 20;
    private final int profileIndex;
    public MinecraftServer server;
    protected int x;
    protected int y;
    protected final int spacingY = 24;
    public ProfileConfig.Profile profile;
    protected SpeedrunPractice.PracticeMode mode;
    protected Mode profileMode;
    public TextFieldWidget nameField;
    public ButtonWidget modeButton;
    public ButtonWidget slotCycle;
    public ButtonWidget done;
    public ButtonWidget cancel;
    public ButtonWidget selectWorld;
    public ButtonWidget clearWorld;
    private TextFieldWidget seed;
    private Consumer<ProfileConfig.Profile> onDone;

    public ProfileScreen setOnDone(Consumer<ProfileConfig.Profile> onDone){
        this.onDone = onDone;
        return this;
    }

    public void setServer(MinecraftServer server){
        this.setShouldCloseOnEsc(false);
        this.server = server;
        if(this.parent instanceof QuickSettingsScreen){
            ((QuickSettingsScreen) this.parent).setServer(this.server);
        }
        if(this.parent instanceof SelectProfileScreen){
            ((SelectProfileScreen) this.parent).parent.setServer(this.server);
        }
    }

    public ProfileScreen(Screen parent){
        this(parent, new ProfileConfig.Profile(null, SpeedrunPractice.getCurrentProfile().getMode(), null, null), Mode.CREATE, SpeedrunPractice.profileConfig.profiles.size());
    }

    public ProfileScreen(Screen parent, ProfileConfig.Profile profile, int profileIndex){
        this(parent, profile, Mode.EDIT, profileIndex);
    }

    public ProfileScreen(Screen parent, ProfileConfig.Profile profile, Mode mode, int profileIndex){
        super(new TranslatableText("speedrun-practice.profilesettings.title"));
        this.parent = parent;
        this.profile = profile.copy();
        this.mode = profile.getMode();
        this.profileMode = mode;
        this.profileIndex = profileIndex;
    }

    @Override
    protected void init() {
        super.init();
        if(this.server == null && this.client != null) this.server = this.client.getServer();
        this.x = (this.width / 2) - (this.bwidth / 2);
        this.y = (this.height / 2) - (this.spacingY * 3 + 13);
        this.initAll(this.textRenderer);
        this.initLogic();
        this.initWidgets();
    }

    private void initLogic() {
        this.modeButton.active = this.profile.editable || !this.profileMode.equals(Mode.EDIT);
        this.clearWorld.visible = this.profile.worldName != null;
        this.selectWorld.setWidth(getSelectWorldWidth());
        this.seed.setText(this.profile.seedText);
        this.seed.setChangedListener(s -> this.profile.seedText = s);
        if(this.profile.name != null){
            this.nameField.setText(this.profile.getDisplayName() + (this.profileMode.equals(Mode.RECREATE) ? " (" + SpeedrunPractice.profileConfig.profiles.stream().filter(profile -> profile.name.startsWith(this.profile.name)).toArray().length + ")" : ""));
        }
        this.nameField.setEditable(this.profile.editable || !this.profileMode.equals(Mode.EDIT));
        this.nameField.setChangedListener(s -> this.profile.name = s);
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    protected void initWidgets(){
//        this.children.addAll(this.all);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
//        this.children.forEach(element -> ((Drawable) element).render(matrices, mouseX, mouseY, delta));
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
        this.drawTextWithShadow(matrices, this.textRenderer, this.nameField.getMessage(), this.nameField.x, this.nameField.y - (this.spacingY / 2), -6250336);
        this.drawTextWithShadow(matrices, this.textRenderer, this.seed.getMessage(), this.seed.x, this.seed.y - (this.spacingY / 2), -6250336);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void initAll(TextRenderer textRenderer){
        this.nameField = this.addButton(new TextFieldWidget(textRenderer, x, y, bwidth, bheight, new TranslatableText("speedrun-practice.profilesettings.name")));

        this.seed = this.addButton(new TextFieldWidget(textRenderer, x, spacingY + (bheight) + y, bwidth, bheight, new TranslatableText("speedrun-practice.quickoptions.seed")));

        int finalY = (this.seed.y + spacingY + (bheight / 2));

        this.modeButton = this.addButton(new ButtonWidget(x, finalY, bwidth, bheight, this.getModeText(mode), button -> {
            if(!this.profile.editable && !this.profileMode.equals(Mode.RECREATE)) return;
            this.mode = this.mode.next();
            button.setMessage(this.getModeText(this.mode));
            this.profile.modeName = this.mode.getSimplifiedName();
        }));

        this.slotCycle = this.addButton(new ButtonWidget(x, spacingY + finalY, bwidth, bheight, this.getCurrentSlotText(this.profile.inventorySlot), button -> {
            this.profile.inventorySlot = this.profile.inventorySlot == 2 ? 0 : this.profile.inventorySlot + 1;
            button.setMessage(this.getCurrentSlotText(this.profile.inventorySlot));
        }));

        this.selectWorld = this.addButton(new ButtonWidget(x, spacingY * 2 + finalY, getSelectWorldWidth(), bheight, this.getSelectWorldText(), button -> {
            if(server != null){
                this.profile.worldName = server.getSaveProperties().getLevelName();
                this.done.active = true;
                button.setMessage(this.getSelectWorldText());
            }
            else{
                SpeedrunPractice.selectingWorldParent = this;
                this.client.openScreen(new SelectWorldScreen(parent));
            }
        }));

        this.clearWorld = this.addButton(new ButtonWidget(selectWorld.x + selectWorld.getWidth(), selectWorld.y, bheight, bheight, new LiteralText("X"), button -> {
            this.profile.worldName = null;
            initLogic();
            this.selectWorld.setMessage(this.getSelectWorldText());
        }));

        this.done = this.addButton(new ButtonWidget(x, spacingY * 3 + finalY, bwidth / 2 - 4, bheight, this.profileMode.equals(Mode.EDIT) ? ScreenTexts.DONE : new TranslatableText("speedrun-practice.profile.recreate"), button -> {
            if(SpeedrunPractice.profileConfig.profiles.stream().noneMatch(p -> EqualsBuilder.reflectionEquals(p, this.profile))){
                if(this.profileMode.equals(Mode.EDIT)){
                    SpeedrunPractice.profileConfig.profiles.set(this.profileIndex, this.profile);
                }
                else{
                    SpeedrunPractice.profileConfig.profiles.add(this.profile);
                }
                SpeedrunPractice.profileConfig.selected = profileIndex;
                try {
                    SpeedrunPractice.profileConfig.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(onDone != null) onDone.accept(this.profile);
            this.client.openScreen(parent);
        }));

        this.cancel = this.addButton(new ButtonWidget(done.x + done.getWidth() + 10, done.y, done.getWidth(), bheight, ScreenTexts.CANCEL, button -> {
            this.client.openScreen(this.parent);
        }));
    }

    private int getSelectWorldWidth() {
        return bwidth - (this.profile.worldName != null ? bheight : 0);
    }

    private Text getModeText(SpeedrunPractice.PracticeMode mode) {
        return new TranslatableText("speedrun-practice.profile.mode").append(": ").append(new TranslatableText(mode.getTranslationKey()));
    }

    private Text getCurrentSlotText(int slot){
        return new TranslatableText("speedrun-practice.currentSlot").append(": ").append("" + (slot + 1));
    }

    public void setCustomStartPracticing(ButtonWidget.PressAction onPress){
        if(!(parent instanceof QuickSettingsScreen) && !(parent instanceof SelectProfileScreen)) return;
        ((SelectProfileScreen) parent).setCustomStartPracticing(onPress);
    }

    public void setShouldCloseOnEsc(boolean closeOnEsc){
        if(!(parent instanceof QuickSettingsScreen) && !(parent instanceof SelectProfileScreen)) return;
        ((SelectProfileScreen) parent).setCloseOnEsc(closeOnEsc);
    }

    public enum Mode{
        EDIT,
        CREATE,
        RECREATE
    }

    private Text getSelectWorldText(){
        return new TranslatableText(this.profile.worldName != null ? "speedrun-practice.quickoptions.selectedworld" : server != null ? "speedrun-practice.quickoptions.selectcurrentworld" : "speedrun-practice.quickoptions.selectworld", this.profile.worldName);
    }
}
