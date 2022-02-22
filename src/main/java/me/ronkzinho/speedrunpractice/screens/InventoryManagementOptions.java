package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import me.ronkzinho.speedrunpractice.practice.Practice;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.CyclingOption;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.HashMap;

public class InventoryManagementOptions {
    public InventoryManagementScreen parent;
    public int width;
    int height;
    int x;
    int y;
    int spacingY;
    protected final HashMap<String, Integer> slot = new HashMap<>();
    public SpeedrunPractice.PracticeMode profile = SpeedrunPractice.PracticeMode.END;
    public ButtonWidget profileCycle;
    public ButtonWidget slotCycle;
    public ButtonWidget saveSlot;
    public ButtonWidget setSlot;
    public ButtonWidget loadSlot;
    public ButtonWidget done;

    public InventoryManagementOptions(InventoryManagementScreen parent, int width, int height, int x, int y, int spacingY, TextRenderer textRenderer) {
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.spacingY = spacingY;
    }

    public void init(){
        this.profileCycle = this.addToAll(new ButtonWidget(x, y, (int) (width * .8) - 4, height, this.getModeText(this.profile), button ->{
            this.profile = this.profile.next();
            button.setMessage(this.getModeText(this.profile));
        }));

        this.slotCycle = this.addToAll(new ButtonWidget(x + this.profileCycle.getWidth() + 4, this.profileCycle.y, width - (this.profileCycle.getWidth() + 4), height, this.getCurrentSlotText(this.getCurrentSlot()), button -> {
            this.slot.replace(this.profile.getSimplifiedName(), this.getCurrentSlot() == 3 ? 1 : this.getCurrentSlot() + 1);
            button.setMessage(this.getCurrentSlotText(this.getCurrentSlot()));
        }));

        this.saveSlot = this.addToAll(new ButtonWidget(x, spacingY + y, width, height, new TranslatableText("speedrun-practice.inventorymanagement.options.save"), button -> {
            String text = Practice.saveSlot(this.getCurrentSlot(), profile.getSimplifiedName());
            parent.setMessage(new TranslatableText(text != null ? text : "speedrun-practice.inventorymanagement.fail"));
        }));

        this.setSlot = this.addToAll(new ButtonWidget(x, spacingY * 2 + y, width, height, new TranslatableText("speedrun-practice.inventorymanagement.options.selectToAll"), button -> {
            String text = Practice.setSlot(this.getCurrentSlot(), profile.getSimplifiedName());
            parent.setMessage(new TranslatableText(text != null ? text : "speedrun-practice.inventorymanagement.fail"));
        }));

        this.loadSlot = this.addToAll(new ButtonWidget(x, spacingY * 3 + y, width, height, new TranslatableText("speedrun-practice.inventorymanagement.options.load"), button -> {
            ServerPlayerEntity player = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid());
            if(player.interactionManager.getGameMode() != GameMode.CREATIVE){
                parent.setMessage(new TranslatableText("speedrun-practice.inventorymanagement.fail-reason", "You must be on creative to execute this command."));
                return;
            };
            Practice.getInventory(player, profile.getSimplifiedName(), this.getCurrentSlot() - 1);
            parent.setMessage(new TranslatableText("speedrun-practice.inventorymanagement.inventory.success", this.getCurrentSlot()));
        }));

        this.done = this.addToAll(new ButtonWidget(x, spacingY * (parent.server != null ? 4 : 3) + y, width, height, ScreenTexts.DONE, button -> {
            MinecraftClient.getInstance().openScreen(parent.parent);
        }));
    }

    public ArrayList<Element> all = new ArrayList<>();

    public <T extends AbstractButtonWidget> T addToAll(T button){
        this.all.add(button);
        return button;
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
}
