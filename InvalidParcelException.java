/**
 * Exception thrown when parcel input details fail validation rules.
 */
public class InvalidParcelException extends Exception {
    public InvalidParcelException(String message) {
        super(message);
    }
}
