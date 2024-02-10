package easton.sharedechest.mixin;

import easton.sharedechest.SharedEChest;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "stop", at = @At("HEAD"))
    private void saveSharedInv(boolean bl, CallbackInfo ci) throws IOException {
        SharedEChest.saveSharedInv((MinecraftServer)(Object)this);
    }

    @Inject(method = "startServer", at = @At("RETURN"))
    private static <S extends MinecraftServer> void loadInv(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir) throws IOException {
        SharedEChest.loadSharedInv(cir.getReturnValue());
    }

}
