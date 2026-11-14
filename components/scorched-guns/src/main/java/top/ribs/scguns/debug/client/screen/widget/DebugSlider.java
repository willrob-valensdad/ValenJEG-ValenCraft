package top.ribs.scguns.debug.client.screen.widget;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import top.ribs.scguns.debug.IDebugWidget;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class DebugSlider extends ExtendedSlider implements IDebugWidget {
    private final Consumer<Double> callback;

    public DebugSlider(double minValue, double maxValue, double currentValue, double stepSize, int precision, Consumer<Double> callback) {
        super(0, 0, 0, 14, Component.empty(), Component.empty(), minValue, maxValue, currentValue, stepSize, precision, true);
        this.callback = callback;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(net.minecraft.network.chat.Component.literal(Double.toString(this.getValue())));
    }

    @Override
    protected void applyValue() {
        this.callback.accept(this.getValue());
    }
}
