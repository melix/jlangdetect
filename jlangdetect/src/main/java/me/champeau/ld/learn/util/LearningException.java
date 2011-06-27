package me.champeau.ld.learn.util;

/**
 * This exception is thrown whenever an error is encountered during corpus learning.
 */
public class LearningException extends RuntimeException {
    public LearningException() {
        super();
    }

    public LearningException(final Throwable cause) {
        super(cause);
    }

    public LearningException(final String message) {
        super(message);
    }

    public LearningException(final String message, final Throwable cause) {
        super(message, cause);
    }

    protected LearningException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
