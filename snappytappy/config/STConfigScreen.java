package me.cutebow.snappytappy.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class STConfigScreen extends Screen {
    private final Screen parent;

    private STConfigScreen(Screen parent) {
        super(Text.literal("Snappy Tappy"));
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new STConfigScreen(parent);
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 10;

        ButtonWidget toggle = ButtonWidget.builder(toggleLabel(), b -> {
            STConfig.INSTANCE.enabled = !STConfig.INSTANCE.enabled;
            STConfig.save();
            b.setMessage(toggleLabel());
        }).dimensions(cx - 100, y, 200, 20).build();

        ButtonWidget done = ButtonWidget.builder(Text.literal("Done"), b -> {
            if (this.client != null) this.client.setScreen(parent);
        }).dimensions(cx - 100, y + 30, 200, 20).build();

        addDrawableChild(toggle);
        addDrawableChild(done);
    }

    private Text toggleLabel() {
        return Text.literal("Enabled: " + (STConfig.INSTANCE.enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
}
