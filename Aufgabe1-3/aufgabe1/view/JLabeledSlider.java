package aufgabe1.view;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class JLabeledSlider extends JPanel implements ListenableComponent {
    @NotNull
    private final JSlider slider;
    @NotNull
    private final JLabel label;


    /**
     * @param min must be < max
     * @param max must be > min
     */
    protected JLabeledSlider(int min, int max) {
        if(max <= min) throw new IllegalArgumentException("min must be less than max");
        label = new JLabel();
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        slider = new JSlider(min, max);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(label);
        add(slider);
    }

    public void setLabel(@NotNull String text) {
        label.setText(text);
    }

    /**
     * @return the slider value, always between min and max (inclusive)
     */
    public int getValue() {
        return slider.getValue();
    }

    /**
     * @param value must be between min and max (inclusive)
     */
    public void setValue(int value) {
        slider.setValue(value);
    }

    public void addChangeListener(@NotNull ChangeListener l) {
        slider.addChangeListener(l);
    }

    public static class Builder {
        private String label;
        private int min = 0;
        private int max = 10;
        private int value;
        private Consumer<Integer> setter;

        @NotNull
        public Builder label(String label) {
            this.label = label;
            return this;
        }

        @NotNull
        public Builder binding(int defaultValue, Consumer<Integer> setter) {
            this.value = defaultValue;
            this.setter = setter;
            return this;
        }

        @NotNull
        public Builder range(int min, int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        @NotNull
        public JLabeledSlider build() {
            final Function<Integer, String> format = (v) -> String.format("%s: %d", label, v);

            JLabeledSlider sliderComp = new JLabeledSlider(min, max);
            sliderComp.setValue(value);
            sliderComp.setLabel(format.apply(value));
            sliderComp.addChangeListener(e -> {
                int v = sliderComp.getValue();
                setter.accept(v);
                sliderComp.setLabel(format.apply(v));
            });
            return sliderComp;
        }
    }
}
