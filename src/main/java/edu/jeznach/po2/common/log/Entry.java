package edu.jeznach.po2.common.log;

import org.jetbrains.annotations.NotNull;

public final class Entry<M> {

    @NotNull private Long timestamp = System.currentTimeMillis();
    @NotNull public Long getTimestamp() { return this.timestamp; }
    public void setTimestamp(@NotNull Long timestamp) { this.timestamp = timestamp; }

    @NotNull private M message;
    @NotNull public M getMessage() { return this.message; }
    public void setMessage(@NotNull M message) { this.message = message; }

    public Entry(@NotNull M message) { this.message = message; }

    public <N> Entry<N> of(@NotNull N message) { return new Entry<>(message); }

    public static final class Message {

        @NotNull private Type type;
        @NotNull public Type getType() { return this.type; }
        public void setType(@NotNull Type type) { this.type = type; }

        public Message(@NotNull Type type) {
            this.type = type;
        }

        public enum Type {
            INFO, WARNING, ERROR
        }
    }
}
