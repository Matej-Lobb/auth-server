package sk.mlobb.authserver.model.exception;

/**
 * The type Invalid request exception.
 */
public class InvalidRequestException extends RuntimeException {

    /**
     * Instantiates a new Invalid request exception.
     *
     * @param errorMessage the error message
     */
    public InvalidRequestException(String errorMessage) {
        super(errorMessage);
    }
}
