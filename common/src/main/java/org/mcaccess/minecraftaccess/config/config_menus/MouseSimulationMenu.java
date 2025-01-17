package org.mcaccess.minecraftaccess.config.config_menus;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.mcaccess.minecraftaccess.config.Config;
import org.mcaccess.minecraftaccess.config.config_maps.MouseSimulationConfigMap;
import org.mcaccess.minecraftaccess.utils.BaseScreen;

import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
public class MouseSimulationMenu extends BaseScreen {
    public MouseSimulationMenu(String title, BaseScreen previousScreen) {
        super(title, previousScreen);
    }

    @Override
    protected void init() {
        super.init();

        MouseSimulationConfigMap initMap = MouseSimulationConfigMap.getInstance();

        ButtonWidget featureToggleButton = this.buildButtonWidget(featureToggleButtonMessage(initMap.isEnabled()),
                (button) -> {
                    MouseSimulationConfigMap map = MouseSimulationConfigMap.getInstance();
                    map.setEnabled(!map.isEnabled());
                    button.setMessage(Text.of(featureToggleButtonMessage(map.isEnabled())));
                    Config.getInstance().writeJSON();
                });
        this.addDrawableChild(featureToggleButton);

        ValueEntryMenu.ValueConfig c1 = new ValueEntryMenu.ValueConfig(() -> MouseSimulationConfigMap.getInstance().getScrollDelayInMilliseconds(),
                (v) -> MouseSimulationConfigMap.getInstance().setScrollDelayInMilliseconds(Integer.parseInt(v)),
                ValueEntryMenu.ValueType.INT);
        ButtonWidget delayButton = this.buildButtonWidget(
                I18n.translate("minecraft_access.gui.common.button.delay",
                        initMap.getScrollDelayInMilliseconds()),
                (button) -> this.client.setScreen(new ValueEntryMenu(c1, this)));
        this.addDrawableChild(delayButton);

        Function<Boolean, String> useMacMouseFix = featureToggleButtonMessageWith("minecraft_access.gui.mouse_simulation.button.toggle_mac_mouse_fix_button");
        ButtonWidget macMouseFixToggleButton = this.buildButtonWidget(useMacMouseFix.apply(initMap.getMacMouseFix()),
                (button) -> {
                    MouseSimulationConfigMap map = MouseSimulationConfigMap.getInstance();
                    map.setMacMouseFix(!map.getMacMouseFix());
                    button.setMessage(Text.of(I18n.translate(useMacMouseFix.apply(map.getMacMouseFix()))));
                    Config.getInstance().writeJSON();
                });
        this.addDrawableChild(macMouseFixToggleButton);
    }
}
