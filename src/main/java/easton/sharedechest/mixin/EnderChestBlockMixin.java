package easton.sharedechest.mixin;

import easton.sharedechest.SharedEnderChestHandler;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EnderChestBlock.class)
public class EnderChestBlockMixin {

    @ModifyArg(
            method = "onUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;openHandledScreen(Lnet/minecraft/screen/NamedScreenHandlerFactory;)Ljava/util/OptionalInt;"),
            index = 0
    )
    private NamedScreenHandlerFactory useShareHandler(NamedScreenHandlerFactory factory) {
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            return new SharedEnderChestHandler(syncId, playerInventory, ((GenericContainerScreenHandler)factory.createMenu(3, playerInventory, player)).getInventory());
        }, Text.translatable("container.enderchest"));
    }

}
