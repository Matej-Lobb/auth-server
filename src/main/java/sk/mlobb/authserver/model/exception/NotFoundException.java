package sk.mlobb.authserver.model.exception;

public class NotFoundException extends Exception {

    public NotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
