package aufgabe1.view;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class JLabeledFloatSlider extends JLabeledSlider {
    private final float scale;

    private JLabeledFloatSlider(float min, float max, float scale) {
        super((int) Math.floor(min * scale), (int) Math.ceil(max * scale));
        this.scale = scale;
    }

    /**
     * @return the slider value, always between min and max (inclusive)
     */
    public float getFloatValue() {
        return getValue() / scale;
    }

    /**
     * @param value must be between min and max (inclusive)
     */
    public void setFloatValue(float value) {
        super.setValue(Math.round(value * scale));
    }

    public static class Builder {
        @NotNull
        private String label = "";
        private float min = 0;
        private float max = 1;
        private float scale = 100;
        private float value;
        @NotNull
        private Consumer<Float> setter = value -> {};

        @NotNull
        public Builder label(@NotNull String label) {
            this.label = label;
            return this;
        }

        @NotNull
        public Builder binding(float defaultValue, @NotNull Consumer<Float> setter) {
            this.value = defaultValue;
            this.setter = setter;
            return this;
        }

        // Max must be > than min
        @NotNull
        public Builder range(float min, float max) {
            this.min = min;
            this.max = max;
            return this;
        }

        @NotNull
        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        @NotNull
        public JLabeledFloatSlider build() {
            int decimals = (int) Math.ceil(Math.log10(scale));
            final String formatString = "%s: %." + decimals + "f";
            final Function<Float, String> format = (v) -> String.format(formatString, label, v);

            JLabeledFloatSlider sliderComp = new JLabeledFloatSlider(min, max, scale);
            sliderComp.setFloatValue(value);
            sliderComp.setLabel(format.apply(value));
            sliderComp.addChangeListener(e -> {
                float v = sliderComp.getFloatValue();
                setter.accept(v);
                sliderComp.setLabel(format.apply(v));
            });
            return sliderComp;
        }
    }
}
