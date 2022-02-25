package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.practice.Practice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

import java.util.HashMap;

public class InventoryManagementScreen extends Screen {
    protected final Screen parent;
    MinecraftServer server;
    private Text message;
    public int bwidth = 250;
    int bheight = 20;
    int x;
    int y;
    public int spacingY = 24;
    protected final HashMap<String, Integer> slot = new HashMap<>();
    public SpeedrunPractice.PracticeMode profile = SpeedrunPractice.PracticeMode.END;
    public ButtonWidget profileCycle;
    public ButtonWidget slotCycle;
    public ButtonWidget saveSlot;
    public ButtonWidget setToAllCurrentModeProfiles;
    public ButtonWidget loadSlot;
    public ButtonWidget done;

    protected InventoryManagementScreen(Screen parent) {
        super(new TranslatableText("speedrun-practice.inventorymanagement.title"));
        this.parent = parent;
    }

    public void setMessage(Text message){
        this.message = message;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();
        if(this.client != null && this.client.getServer() != null) {
            this.server = this.client.getServer();
        }
        this.x = (this.width / 2) - (this.bwidth / 2);
        this.y = (this.height / 2) - (int) (spacingY * 2.5);
        for(int i = 0; i < SpeedrunPractice.PracticeMode.values().length; i++){
            SpeedrunPractice.PracticeMode practiceMode = SpeedrunPractice.PracticeMode.byId(i);
            this.slot.put(practiceMode.getSimplifiedName(), SpeedrunPractice.getCurrentProfile() != null ? SpeedrunPractice.getCurrentProfile().inventorySlot + 1 : SpeedrunPractice.config.practiceSlots.get(this.profile.getSimplifiedName()));
        };
        this.initWidgets();
        this.initLogic();
    }

    private void initLogic() {
        this.loadSlot.active = this.server != null;
        this.saveSlot.active = this.server != null;
    }

    protected void initWidgets(){
        this.profileCycle = this.addButton(new ButtonWidget(x, y, (int) (bwidth * .8) - 4, bheight, this.getModeText(this.profile), button ->{
            this.profile = this.profile.next();
            button.setMessage(this.getModeText(this.profile));
        }));

        this.slotCycle = this.addButton(new ButtonWidget(x + this.profileCycle.getWidth() + 4, this.profileCycle.y, bwidth - (this.profileCycle.getWidth() + 4), bheight, this.getCurrentSlotText(this.getCurrentSlot()), button -> {
            this.slot.replace(this.profile.getSimplifiedName(), this.getCurrentSlot() == 3 ? 1 : this.getCurrentSlot() + 1);
            button.setMessage(this.getCurrentSlotText(this.getCurrentSlot()));
        }));

        this.saveSlot = this.addButton(new ButtonWidget(x, spacingY + y, bwidth, bheight, new TranslatableText("speedrun-practice.inventorymanagement.options.save"), button -> {
            String text = Practice.saveSlot(this.getCurrentSlot(), profile.getSimplifiedName());
            this.setMessage(new TranslatableText(text != null ? text : "speedrun-practice.inventorymanagement.fail"));
        }));

        this.setToAllCurrentModeProfiles = this.addButton(new ButtonWidget(x, spacingY * 2 + y, bwidth, bheight, new TranslatableText("speedrun-practice.inventorymanagement.options.setToAllCurrentModeProfiles"), button -> {
            String text = Practice.setSlot(this.getCurrentSlot(), profile.getSimplifiedName());
            this.setMessage(new TranslatableText(text != null ? text : "speedrun-practice.inventorymanagement.fail"));
        }));

        this.loadSlot = this.addButton(new ButtonWidget(x, spacingY * 3 + y, bwidth, bheight, new TranslatableText("speedrun-practice.inventorymanagement.options.load"), button -> {
            ServerPlayerEntity player = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid());
            if(player.interactionManager.getGameMode() != GameMode.CREATIVE){
                this.setMessage(new TranslatableText("speedrun-practice.inventorymanagement.fail-reason", "You must be on creative to execute this command."));
                return;
            };
            Practice.getInventory(player, profile.getSimplifiedName(), this.getCurrentSlot() - 1);
            this.setMessage(new TranslatableText("speedrun-practice.inventorymanagement.inventory.success", this.getCurrentSlot()));
        }));

        this.done = this.addButton(new ButtonWidget(x, spacingY * 4 + y, bwidth, bheight, ScreenTexts.DONE, button -> {
            MinecraftClient.getInstance().openScreen(this.parent);
        }));
    }

    private Text getModeText(SpeedrunPractice.PracticeMode profile) {
        return new TranslatableText("speedrun-practice.profile").append(": ").append(new TranslatableText(profile.getTranslationKey()));
    }

    private Text getCurrentSlotText(int slot){
        return new TranslatableText("speedrun-practice.currentSlot").append(": ").append("" + slot);
    }

    private int getCurrentSlot(){
        return this.slot.get(this.profile.getSimplifiedName());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.children.forEach(element -> ((Drawable) element).render(matrices, mouseX, mouseY, delta));
        if(this.message != null){
            this.drawCenteredText(matrices, this.textRenderer, this.message, this.width / 2, ((AbstractButtonWidget) this.children.get(0)).y - spacingY, 0xFFFFFF);
        }
    }
}
