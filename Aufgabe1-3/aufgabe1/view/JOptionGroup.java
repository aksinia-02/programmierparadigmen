package aufgabe1.view;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JOptionGroup extends JPanel implements ListenableComponent {
    private final List<ChangeListener> listeners = new ArrayList<>();

    private JOptionGroup(@NotNull List<JComponent> components, @Nullable String label) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (label == null || label.isEmpty()) {
            Border border = UIManager.getBorder("TitledBorder.border");
            Border inset = new EmptyBorder(1, 1, 1, 1);
            Border padding = new EmptyBorder(2, 2, 2, 2);
            setBorder(new CompoundBorder(new CompoundBorder(inset, border), padding));
        } else {
            Border border = BorderFactory.createTitledBorder(label);
            setBorder(border);
        }

        for (JComponent component : components) {
            if (component instanceof ListenableComponent listenable) {
                listenable.addChangeListener(this::broadcastChangeEvent);
            }

            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(component);
        }
    }

    private void broadcastChangeEvent(@NotNull ChangeEvent e) {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(e);
        }
    }

    @Override
    public void addChangeListener(@NotNull ChangeListener l) {
        listeners.add(l);
    }

    public static class Builder {
        @NotNull
        private final List<JComponent> components = new ArrayList<>();
        @NotNull
        private final List<ChangeListener> listeners = new ArrayList<>();
        @Nullable
        private String label = null;

        @NotNull
        public Builder add(@NotNull JComponent component) {
            this.components.add(component);
            return this;
        }

        @NotNull
        public Builder addChangeListener(@NotNull ChangeListener l) {
            listeners.add(l);
            return this;
        }

        @NotNull
        public Builder label(@Nullable String label) {
            this.label = label;
            return this;
        }

        @NotNull
        public JOptionGroup build() {
            JOptionGroup groupComp = new JOptionGroup(components, label);
            for (ChangeListener listener : listeners) {
                groupComp.addChangeListener(listener);
            }
            return groupComp;
        }
    }
}
