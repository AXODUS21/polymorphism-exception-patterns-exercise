/**
 * Exception thrown when a parcel with the requested tracking number cannot be found.
 */
public class ParcelNotFoundException extends Exception {
    public ParcelNotFoundException(String message) {
        super(message);
    }
}
