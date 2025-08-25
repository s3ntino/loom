package dev.vanqure.loom.tests;

final class UserNotFoundException extends IllegalArgumentException {

    UserNotFoundException(final String message) {
        super(message);
    }
}
