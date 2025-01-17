package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.IMinecraftServer;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.config.ProfileConfig;
import me.ronkzinho.speedrunpractice.practice.Practice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static me.ronkzinho.speedrunpractice.SpeedrunPractice.MOD_ID;

public class QuickSettingsScreen extends Screen {
    int bwidth = 250;
    int bheight = 20;
    int x;
    int y;
    public int spacingY = 24;
    private boolean forced = false;
    protected ButtonWidget.PressAction customStartPracticing = null;
    protected ProfileConfig.Profile profile = SpeedrunPractice.getCurrentProfile();
    public Screen parent;
    public ButtonWidget profileCycle;
    public ButtonWidget inventoryButton;
    public ButtonWidget options;
    public ButtonWidget cancel;
    public ButtonWidget startPracticing;
    public boolean closeOnEsc = true;
    @Nullable
    private Integer selected = SpeedrunPractice.profileConfig.selected;
    private ButtonWidget deselectProfile;
    private ButtonWidget editProfile;
    private MinecraftServer server;

    public QuickSettingsScreen(Screen parent){
        this(parent, null);
    }

    public QuickSettingsScreen(Screen parent, MinecraftServer server) {
        super(new TranslatableText("speedrun-practice.quicksettings.title"));
        this.parent = parent;
        this.server = server;
        this.init();
    }

    public void setCustomStartPracticing(ButtonWidget.PressAction onPress){
        this.customStartPracticing = onPress;
    }

    @Override
    protected void init() {
        super.init();
        if(this.parent instanceof SelectWorldScreen) profile.worldName = null;
        this.x = (this.width / 2) - (250 / 2);
        this.y =  (this.height / 2) - (spacingY * 2);
        this.server = this.client != null ? this.client.getServer() : null;
        this.initAll();
        this.initLogic();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.children.forEach(element -> ((Drawable) element).render(matrices, mouseX, mouseY, delta));
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public int getProfileWidth(){
        return bwidth - (selected != null ? bheight * 2 : 0);
    }

    public void initAll(){
        this.profileCycle = this.addButton(new ButtonWidget(x, y, getProfileWidth(), bheight, this.getProfileText(this.profile), button -> this.client.openScreen(new SelectProfileScreen(this, (p) -> {
            this.profile = p;
            this.selected = SpeedrunPractice.profileConfig.profiles.indexOf(p);
            button.setMessage(this.getProfileText(this.profile));
        }))));

        this.editProfile = this.addButton(new TexturedButtonWidget(profileCycle.x + profileCycle.getWidth(), profileCycle.y, bheight, bheight, 0, 0, 20, new Identifier(MOD_ID, "textures/gui/editbutton.png"), 32, 64, button -> {
            this.client.openScreen(new ProfileScreen(this, this.profile, ProfileScreen.Mode.EDIT, Objects.requireNonNull(this.selected)).setOnDone((p) -> this.profile = p));
        }, new TranslatableText("")));

        this.deselectProfile = this.addButton(new ButtonWidget(editProfile.x + editProfile.getWidth(), editProfile.y, bheight, bheight, new LiteralText("X"), button -> {
            this.profile = null;
            this.selected = null;
            SpeedrunPractice.profileConfig.selected = null;
            this.initLogic();
        }));

        this.inventoryButton = this.addButton(new ButtonWidget(x, spacingY + y, bwidth, bheight, new TranslatableText("speedrun-practice.inventorymanagement"), button -> this.client.openScreen(new InventoryManagementScreen(this))));

        this.options = this.addButton(new ButtonWidget(x, spacingY * 2 + y, bwidth, bheight, new TranslatableText("speedrun-practice.options"), button -> {
            this.client.openScreen(SpeedrunPractice.config.getScreen(this));
        }));

        this.startPracticing = this.addButton(new ButtonWidget( x, spacingY * 3 + y, bwidth / 2 - 4, bheight, this.client != null && this.client.world != null ? ScreenTexts.DONE : new TranslatableText("speedrun-practice.startpracticing"), customStartPracticing != null ? customStartPracticing : button -> {
            SpeedrunPractice.profileConfig.selected = selected;

            if(this.client.world != null){
                this.client.openScreen(parent);
                this.client.mouse.lockCursor();
                return;
            }

            if(this.server != null) Practice.creatingPracticeWorld(client);
            this.startPracticeSession();
        }));

        this.cancel = this.addButton(new ButtonWidget(startPracticing.x + startPracticing.getWidth() + 10, startPracticing.y, bwidth / 2 - 4, bheight, ScreenTexts.CANCEL, button -> {
            if(this.selected == null) SpeedrunPractice.isPlaying = false;
            this.client.openScreen(this.parent);
        }));
    }

    protected void initLogic(){
        if(this.client == null) return;
        if (this.profile != null && this.profile.worldName != null && forced) {
           this.startPracticeSession();
        }

        profileCycle.setWidth(getProfileWidth());
        profileCycle.setMessage(this.getProfileText(this.profile));
        deselectProfile.visible = selected != null;
        editProfile.visible = selected != null;
        startPracticing.active = profile != null && profile.worldName != null && (this.server == null || ((IMinecraftServer) server).getSession().getDirectoryName().equals(profile.worldName));
    }

    private void startPracticeSession() {
        try {
            LevelStorage levelStorage = this.client.getLevelStorage();
            levelStorage.getLevelList();
            if(levelStorage.levelExists(this.profile.worldName)){
                SpeedrunPractice.isPlaying = true;
                this.client.method_29970(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
                this.client.startIntegratedServer(this.profile.worldName);
            }
            else{
                this.noWorld();
            }
        } catch (LevelStorageException ignored) {
            this.noWorld();
        }
    }

    private void noWorld() {
        this.forced = false;
        ProfileScreen profileScreen = new ProfileScreen(this, this.profile, ProfileScreen.Mode.EDIT, SpeedrunPractice.profileConfig.profiles.indexOf(this.profile));
        this.client.openScreen(profileScreen);
        profileScreen.setInitialFocus(profileScreen.selectWorld);
    }

    private Text getProfileText(ProfileConfig.Profile profile) {
        return profile != null ? new TranslatableText("speedrun-practice.profile").append(": ").append(profile.getNameText()) : new TranslatableText("speedrun-practice.selectprofile");
    }

    public void setForced(boolean forced){
        this.forced = forced;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.closeOnEsc;
    }

    public void setCloseOnEsc(boolean closeOnEsc) {
        this.closeOnEsc = closeOnEsc;
    }

    public void setInitialParent(Screen screen) {
        this.parent = screen;
    }
}
