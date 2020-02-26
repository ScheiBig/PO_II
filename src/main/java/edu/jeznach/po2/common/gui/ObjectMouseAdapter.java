package edu.jeznach.po2.common.gui;

import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseAdapter;

public class ObjectMouseAdapter<T> extends MouseAdapter {

    @SuppressWarnings("NotNullFieldNotInitialized")
    private @NotNull T object;
    public @NotNull T getObject() { return this.object; }
    public void setObject(@NotNull T object) { this.object = object; }

    public ObjectMouseAdapter() {  }

    public ObjectMouseAdapter(@NotNull T object) {
        this.object = object;
    }
}
