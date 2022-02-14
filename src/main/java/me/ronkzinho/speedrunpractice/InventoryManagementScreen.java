package me.ronkzinho.speedrunpractice;

import me.ronkzinho.speedrunpractice.practice.Practice;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.CyclingOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class InventoryManagementScreen extends Screen {
    private Screen parent;
    private ButtonListWidget list;
    private SpeedrunPractice.PracticeMode profile = SpeedrunPractice.practiceMode;
    private int slot = 1;

    protected InventoryManagementScreen(Screen parent) {
        super(new TranslatableText("speedrun-practice.inventorymanagement.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int width = 310;
        int x = (this.width / 2) - (width / 2);
        this.list = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSingleOptionEntry(new CyclingOption("speedrun-practice.inventorymanagement.options.profile", (gameOptions, integer) -> this.profile = this.profile.next(), (gameOptions, cyclingOption) -> {
            TranslatableText translatableText = new TranslatableText(this.profile.getTranslationKey());

            return cyclingOption.getDisplayPrefix().append(translatableText);
        }));
        this.list.addSingleOptionEntry(new CyclingOption("speedrun-practice.inventorymanagement.options.currentSlot", (gameOptions, integer) -> {
            if(slot == 3){
                slot = 1;
            }
            else{
                slot = Integer.remainderUnsigned(slot + integer, 4);
            }
        }, (gameOptions, cyclingOption) -> cyclingOption.getDisplayPrefix().append(new LiteralText("" + slot))));

        this.addButton(new ButtonWidget(x, 85, width, 20, new TranslatableText("speedrun-practice.inventorymanagement.options.save"), button -> {
            Practice.saveSlot(this.slot, profile.getSimplifiedName());
        }));
        this.addButton(new ButtonWidget(x, 110, width, 20, new TranslatableText("speedrun-practice.inventorymanagement.options.select"), button -> {
            Practice.setSlot(this.slot, profile.getSimplifiedName());
        }));
        this.addButton(new ButtonWidget(x, 135, width, 20, ScreenTexts.DONE, button -> {
            this.client.openScreen(null);
            this.client.mouse.lockCursor();
        }));
        this.children.add(list);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
