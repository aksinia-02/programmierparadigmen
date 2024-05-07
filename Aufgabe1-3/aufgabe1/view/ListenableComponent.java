package aufgabe1.view;

import org.jetbrains.annotations.NotNull;

import javax.swing.event.ChangeListener;

interface ListenableComponent {
    /**
     * The change listener will be called when the component's value changes
     */
    void addChangeListener(@NotNull ChangeListener l);
}
