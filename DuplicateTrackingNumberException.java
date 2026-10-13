/**
 * Exception thrown when attempting to add a parcel with a tracking number that already exists.
 */
public class DuplicateTrackingNumberException extends Exception {
    public DuplicateTrackingNumberException(String message) {
        super(message);
    }
}
