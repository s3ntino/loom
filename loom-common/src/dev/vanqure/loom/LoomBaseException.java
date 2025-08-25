package dev.vanqure.loom;

public abstract class LoomBaseException extends IllegalStateException {

    protected LoomBaseException(final String message) {
        super(message);
    }

    protected LoomBaseException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
