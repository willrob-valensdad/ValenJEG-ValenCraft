package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** Range slider on the 26.3 slider, which stores a 0-1 value. */
public abstract class ExtendedSlider extends AbstractSliderButton {
    private final double minValue;
    private final double maxValue;
    private final double stepSize;

    public ExtendedSlider(int x, int y, int width, int height, Component prefix, Component suffix,
                          double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        super(x, y, width, height, prefix, fraction(currentValue, minValue, maxValue));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize;
    }

    public double getValue() {
        double span = this.maxValue - this.minValue;
        double raw = this.minValue + this.value * span;
        if (this.stepSize > 0.0) {
            raw = this.minValue + Math.round((raw - this.minValue) / this.stepSize) * this.stepSize;
        }
        return Mth.clamp(raw, this.minValue, this.maxValue);
    }

    private static double fraction(double current, double min, double max) {
        if (max <= min) return 0.0;
        return Mth.clamp((current - min) / (max - min), 0.0, 1.0);
    }
}
