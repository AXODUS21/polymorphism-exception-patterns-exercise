/**
 * Exception thrown when an operation cannot be performed because the parcel is already delivered.
 */
public class ParcelAlreadyDeliveredException extends Exception {
    public ParcelAlreadyDeliveredException(String message) {
        super(message);
    }
}
