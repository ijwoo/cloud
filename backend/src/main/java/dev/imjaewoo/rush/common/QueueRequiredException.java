package dev.imjaewoo.rush.common;

public class QueueRequiredException extends RuntimeException {

    public QueueRequiredException(String message) {
        super(message);
    }
}
