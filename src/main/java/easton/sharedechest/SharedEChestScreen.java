package easton.sharedechest;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SharedEChestScreen extends HandledScreen<ScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/generic_54.png");
    private static final Identifier BUTTON_TEXTURE = new Identifier("sharedechest","textures/gui/container/shared_buttons.png");
    private final List<BaseButtonWidget> buttons = new ArrayList<>();
    private BaseButtonWidget personal_button;
    private BaseButtonWidget shared_button;

    public SharedEChestScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        matrices.drawTexture(TEXTURE, i, j, 0, 0, this.backgroundWidth, 3 * 18 + 17);
        matrices.drawTexture(TEXTURE, i, j + 3 * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }

    @Override
    protected void drawForeground(DrawContext matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);

        for (BaseButtonWidget baseButtonWidget : this.buttons) {
            if (baseButtonWidget.shouldRenderTooltip()) {
                baseButtonWidget.renderTooltip(matrices, mouseX - this.x, mouseY - this.y);
                break;
            }
        }
    }

    private <T extends BaseButtonWidget> void addButton(T button) {
        this.addDrawableChild(button);
        this.buttons.add(button);
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.personal_button = new BaseButtonWidget(i + 134, j + 4, false, Text.literal("Personal Ender Chest"));
        this.personal_button.pressed = true;
        this.shared_button = new BaseButtonWidget(i + 152, j + 4, true, Text.literal("Shared Ender Chest"));
        this.addButton(this.personal_button);
        this.addButton(this.shared_button);
    }

    @Environment(EnvType.CLIENT)
    private class BaseButtonWidget extends PressableWidget {

        private boolean pressed = false;
        private final boolean shared;
        private final Text tooltip;

        protected BaseButtonWidget(int i, int j, boolean shared, Text tooltip) {
            super(i, j,  16, 12, Text.empty());
            this.shared = shared;
            this.tooltip = tooltip;
        }

        @Override
        public void onPress() {
            for (BaseButtonWidget button : SharedEChestScreen.this.buttons)
                button.pressed = false;
            this.pressed = true;
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(this.shared);
            ClientPlayNetworking.send(SharedEChest.BUTTON_PRESS_ID, buf);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);

            int i = 0;
            if (this.pressed)
                i += 18;
            int j = 14;
            if (this.shared)
                j += 14;

            context.drawTexture(SharedEChestScreen.BUTTON_TEXTURE, this.getX(), this.getY(), i, j, this.width, this.height);
        }

        public boolean shouldRenderTooltip() {
            return this.hovered;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        public void renderTooltip(DrawContext matrices, int x, int y) {
            matrices.drawTooltip(SharedEChestScreen.this.textRenderer, this.tooltip, x, y);
        }
    }

}
