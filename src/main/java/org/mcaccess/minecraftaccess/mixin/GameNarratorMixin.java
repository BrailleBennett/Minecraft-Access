package org.mcaccess.minecraftaccess.mixin;

import com.mojang.text2speech.Narrator;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.GameNarrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.mcaccess.minecraftaccess.utils.NarratorDummy;
import org.mcaccess.minecraftaccess.utils.system.ScreenReaderController;

@Slf4j
@Mixin(GameNarrator.class)
abstract class GameNarratorMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;"))
    private Narrator redirectGetNarrator() {
        if (ScreenReaderController.isInitialized()) {
            return new NarratorDummy();
        } else {
            log.error("Failed to narrate using the Minecraft Access logic. Will fallback to vanilla handling.");
            return Narrator.getNarrator();
        }
    }

    @Inject(method = "narrateMessage", at = @At("HEAD"), cancellable = true)
    private void narrateMessage(String message, boolean interrupt, CallbackInfo ci) {
        if (ScreenReaderController.isInitialized()) {
            ScreenReaderController.narrate(message, interrupt);
            ci.cancel();
        }
    }
}
