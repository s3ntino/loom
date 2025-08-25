package dev.vanqure.loom.tests;

public final class UserNotFoundException extends IllegalStateException {

    public UserNotFoundException(final String message) {
        super(message);
    }
}
