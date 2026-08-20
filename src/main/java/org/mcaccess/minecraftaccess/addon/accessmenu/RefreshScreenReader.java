package org.mcaccess.minecraftaccess.addon.accessmenu;

import org.mcaccess.minecraftaccess.api.AccessMenuFunction;
import org.mcaccess.minecraftaccess.utils.system.ScreenReaderController;

public class RefreshScreenReader implements AccessMenuFunction {
    @Override
    public void execute() {
        ScreenReaderController.refresh(true);
    }
}
