package sk.mlobb.authserver.model.exception;

/**
 * The type Unauthorized exception.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Instantiates a new Unauthorized exception.
     *
     * @param errorMessage the error message
     */
    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
    }
}
