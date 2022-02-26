package me.ronkzinho.speedrunpractice.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.config.ProfileConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SelectProfileScreen extends Screen {
    protected final QuickSettingsScreen parent;
    private final Consumer<ProfileConfig.Profile> onSelect;
    protected ProfileListWidget profiles;
    protected int spacingY = 24;
    private ButtonWidget selectButton;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
    private ButtonWidget recreateButton;
    private TextFieldWidget searchBox;


    protected SelectProfileScreen(QuickSettingsScreen parent, Consumer<ProfileConfig.Profile> onSelect) {
        super(new TranslatableText("speedrun-practice.selectprofile.title"));
        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        super.init();
        this.initWidgets();
    }

    private void initWidgets() {
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableText("speedrun-practice.selectprofile.search"));
        this.searchBox.setChangedListener(string -> this.profiles.filter(() -> string, false));
        this.profiles = new ProfileListWidget(() -> this.searchBox.getText());
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableText("speedrun-practice.selectprofile.search"));
        this.searchBox.setChangedListener(string -> this.profiles.filter(() -> string, false));
        this.selectButton = this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 52, 150, 20, new TranslatableText("speedrun-practice.selectprofile.select"), buttonWidget -> this.profiles.getSelectedEntry().ifPresent(ProfileListWidget.ProfileEntry::select)));
        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 52, 150, 20, new TranslatableText("speedrun-practice.selectprofile.create"), buttonWidget -> this.client.openScreen(new ProfileScreen(this, new ProfileConfig.Profile(null, SpeedrunPractice.PracticeMode.END, null, null), ProfileScreen.Mode.CREATE, SpeedrunPractice.profileConfig.profiles.size()))));
        this.editButton = this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 72, 20, new TranslatableText("speedrun-practice.selectprofile.edit"), buttonWidget -> this.profiles.getSelectedEntry().ifPresent(ProfileListWidget.ProfileEntry::edit)));
        this.deleteButton = this.addButton(new ButtonWidget(this.width / 2 - 76, this.height - 28, 72, 20, new TranslatableText("speedrun-practice.selectprofile.delete"), buttonWidget -> this.profiles.getSelectedEntry().ifPresent(ProfileListWidget.ProfileEntry::delete)));
        this.recreateButton = this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 72, 20, new TranslatableText("speedrun-practice.selectprofile.recreate"), buttonWidget -> this.profiles.getSelectedEntry().ifPresent(ProfileListWidget.ProfileEntry::recreate)));
        this.addButton(new ButtonWidget(this.width / 2 + 82, this.height - 28, 72, 20, ScreenTexts.CANCEL, buttonWidget -> this.client.openScreen(this.parent)));
        this.profileSelected(null);
        this.setInitialFocus(this.searchBox);
        this.children.add(profiles);
    }

    public void profileSelected(ProfileConfig.Profile profile) {
        this.selectButton.active = profile != null;
        this.deleteButton.active = profile != null && profile.editable;
        this.editButton.active = profile != null;
        this.recreateButton.active = profile != null;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.profiles.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }


    public class ProfileListWidget extends AlwaysSelectedEntryListWidget<ProfileListWidget.ProfileEntry>{
        List<ProfileConfig.Profile> profiles = SpeedrunPractice.profileConfig.profiles;

        public ProfileListWidget(Supplier<String> searchFilter) {
            super(SelectProfileScreen.this.client, SelectProfileScreen.this.width, SelectProfileScreen.this.height, 48, SelectProfileScreen.this.height - 64, 36);
            this.filter(searchFilter, false);
        }

        @Override
        protected int getScrollbarPositionX() {
            return super.getScrollbarPositionX() + 30;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public void filter(Supplier<String> supplier, boolean load) {
            this.clearEntries();
            if (profiles == null || load) {
                this.profiles = SpeedrunPractice.profileConfig.profiles;
            }
            AtomicInteger index = new AtomicInteger();
            this.profiles.stream().filter(profile -> profile.getDisplayName().toLowerCase().startsWith(supplier.get().toLowerCase())).forEach(profile -> {
                this.addEntry(new ProfileEntry(profile, this.client, index));
                index.getAndIncrement();
            });
        }

        public Optional<ProfileEntry> getSelectedEntry() {
            return Optional.ofNullable(this.getSelected());
        }

        @Override
        public void setSelected(@Nullable ProfileEntry entry) {
            super.setSelected(entry);
            SelectProfileScreen.this.profileSelected(entry.profile);
        }

        @Override
        public int getRowTop(int index) {
            return super.getRowTop(index);
        }

        public class ProfileEntry extends EntryListWidget.Entry<ProfileEntry>{
            private final int index;
            ProfileConfig.Profile profile;
            private long time;
            private final MinecraftClient client;

            public ProfileEntry(ProfileConfig.Profile profile, MinecraftClient client, AtomicInteger index){
                this.profile = profile;
                this.client = client;
                this.index = index.get();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ProfileListWidget.this.setSelected(this);
                SelectProfileScreen.this.profileSelected(ProfileListWidget.this.getSelectedEntry().get().profile);
                if (mouseX - (double) ProfileListWidget.this.getRowLeft() <= 32.0) {
                    this.select();
                    return true;
                }
                if (Util.getMeasuringTimeMs() - this.time < 250L) {
                    this.select();
                    return true;
                }
                this.time = Util.getMeasuringTimeMs();
                return false;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                Text name = this.profile.getNameText();
                String modeandworld = this.profile.modeName + (this.profile.worldName != null ? ", world name: " + this.profile.worldName : "");
                String[] prefixes = {"seed: ", "slot: "};
                String[] suffixes = {this.profile.seedText, "" + (this.profile.inventorySlot + 1)};
                List<String> restList = new ArrayList<>();
                for (int i = 0; i < prefixes.length; i++) {
                    if(suffixes[i] != null && !(suffixes[i].isEmpty())) restList.add(prefixes[i] + suffixes[i]);
                }
                restList.set(0, restList.get(0).substring(0, 1).toUpperCase() + restList.get(0).substring(1));
                String rest = String.join(", ", restList);
                this.client.textRenderer.draw(matrices, name, (float)(x + 32 + 3), (float)(y + 1), 0xFFFFFF);
                this.client.textRenderer.draw(matrices, "Mode: " + modeandworld, (float)(x + 32 + 3), (float)(y + this.client.textRenderer.fontHeight + 3), 0x808080);
                this.client.textRenderer.draw(matrices, rest, (float)(x + 32 + 3), (float)(y + this.client.textRenderer.fontHeight + this.client.textRenderer.fontHeight + 3), 0x808080);
                this.client.getTextureManager().bindTexture(this.profile.getMode().getIconId());
                RenderSystem.enableBlend();
                DrawableHelper.drawTexture(matrices, x, y, 0.0f, 0.0f, 32, 32, 32, 32);
                RenderSystem.disableBlend();
            }

            public void select(){
                SelectProfileScreen.this.onSelect.accept(profile);
                this.client.openScreen(SelectProfileScreen.this.parent);
            }

            public void edit(){
                this.client.openScreen(new ProfileScreen(SelectProfileScreen.this, profile, this.index).setOnDone((p) -> this.profile = p));
            }

            public void delete() {
                this.client.openScreen(new ConfirmScreen((t) -> {
                    if(t){
                        SpeedrunPractice.profileConfig.profiles.remove(profile);
                        try {
                            SpeedrunPractice.profileConfig.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        this.client.openScreen(SelectProfileScreen.this.parent);
                    }
                }, new TranslatableText("speedrun-practice.selectprofile.deleteQuestion"), new TranslatableText("speedrun-practice.selectprofile.deleteWarning", this.profile.getNameText()), new TranslatableText("speedrun-practice.selectprofile.deleteButton"), ScreenTexts.CANCEL));
            }

            public void recreate() {
                this.client.openScreen(new ProfileScreen(SelectProfileScreen.this, profile, ProfileScreen.Mode.RECREATE, SpeedrunPractice.profileConfig.profiles.size()));
            }
        }
    }
}
