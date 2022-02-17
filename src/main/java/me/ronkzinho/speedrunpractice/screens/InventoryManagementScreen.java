package me.ronkzinho.speedrunpractice.screens;

import me.ronkzinho.speedrunpractice.SpeedrunPractice;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class InventoryManagementScreen extends Screen {
    protected final Screen parent;
    private InventoryManagementOptions options;
    private Text message;
    public int spacingY = 24;

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
        this.options = new InventoryManagementOptions(this, 250, 20, (this.width / 2) - (250 / 2), (this.height / 4) + (spacingY / 5), spacingY, this.textRenderer);
        for(int i = 0; i < SpeedrunPractice.PracticeMode.values().length; i++){
            SpeedrunPractice.PracticeMode practiceMode = SpeedrunPractice.PracticeMode.byId(i);
            options.slot.put(practiceMode.getSimplifiedName(), SpeedrunPractice.config.practiceSlots.get(practiceMode.getSimplifiedName()) + 1);
        };
        options.init();
        this.initWidgets();
    }

    protected void initWidgets(){
        this.children.addAll(options.all);
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
