/**
 * Exception thrown when a delivery service strategy is not eligible for a given parcel.
 */
public class DeliveryServiceNotAllowedException extends Exception {
    public DeliveryServiceNotAllowedException(String message) {
        super(message);
    }
}
