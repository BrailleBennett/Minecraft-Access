package org.mcaccess.minecraftaccess.utils.system;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import org.mcaccess.prism.Backend;
import org.mcaccess.prism.Context;
import org.mcaccess.prism.Prism;
import org.mcaccess.prism.PrismException;

@Slf4j
public final class ScreenReaderController {
    private static Context context;
    private static Backend activeBackend;

    private ScreenReaderController() {
    }

    /**
     * Initializes PRISM context and acquires the best available backend.
     */
    public static void initialize() {
        if (!Prism.isAvailable()) {
            log.error("PRISM native libraries are not available on this system");
            return;
        }

        try {
            if (context == null || context.isClosed()) {
                context = Prism.createContext();
            }

            activeBackend = context.createBest();
            log.info("PRISM initialized with backend: {}", activeBackend.getName());
        } catch (PrismException e) {
            log.error("Failed to initialize PRISM screen reader", e);
        }
    }

    /**
     * Check whether PRISM has an active, open backend.
     */
    public static boolean isInitialized() {
        return context != null && !context.isClosed() && activeBackend != null && !activeBackend.isClosed();
    }

    /**
     * Re-acquires the best available screen reader backend (e.g. if NVDA was started after launch).
     *
     * @param closeOpenedScreen whether to close the player's container screen
     */
    public static void refresh(boolean closeOpenedScreen) {
        log.info("Refreshing PRISM screen reader");

        if (activeBackend != null) {
            try {
                activeBackend.close();
            } catch (Exception e) {
                log.warn("Error while closing previous backend during refresh", e);
            } finally {
                activeBackend = null;
            }
        }

        if (context == null || context.isClosed()) {
            initialize();
        } else {
            try {
                activeBackend = context.createBest();
                log.info("PRISM backend switched to: {}", activeBackend.getName());
            } catch (PrismException e) {
                log.warn("Could not acquire a PRISM backend during refresh", e);
            }
        }

        if (closeOpenedScreen && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.clientSideCloseContainer();
        }

        narrate(I18n.get("minecraft_access.access_menu.screen_reader_refreshed"), true);
    }

    /**
     * Speaks the given text through the active PRISM backend.
     */
    public static void narrate(String text, boolean interrupt) {
        if (!isInitialized()) {
            log.warn("Cannot narrate; PRISM is not initialized");
            return;
        }

        String cleanedText = formatNarration(text);
        Minecraft client = Minecraft.getInstance();
        if (cleanedText.isBlank() || (client != null && client.getWindow() != null && !client.isWindowActive())) {
            log.warn("The narration of string \"{}\" with interrupt={} was suppressed", text, interrupt);
            return;
        }

        try {
            activeBackend.speak(cleanedText, interrupt);
        } catch (PrismException e) {
            log.error("PRISM failed to speak narration text. Attempting refresh", e);
            refresh(false);
        }
    }

    /**
     * Shuts down PRISM and releases native resources.
     */
    public static void close() {
        if (activeBackend != null) {
            try {
                activeBackend.close();
            } catch (Exception e) {
                log.warn("Error closing PRISM backend", e);
            } finally {
                activeBackend = null;
            }
        }

        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                log.warn("Error closing PRISM context", e);
            } finally {
                context = null;
            }
        }
    }

    /**
     * Removes Minecraft formatting codes (§0-§f, §k-§r).
     */
    public static String formatNarration(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("§.", "");
    }
}
