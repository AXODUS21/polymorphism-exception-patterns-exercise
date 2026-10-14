/**
 * Abstract base class representing a parcel in the delivery system.
 * Demonstrates polymorphism through abstract calculation methods.
 */
public abstract class Parcel {

    private final String trackingNumber;
    private final String receiverName;
    private final double weight;
    private DeliveryServiceStrategy deliveryService;
    private ParcelStatus status;

    /**
     * Constructs a new Parcel.
     *
     * @param trackingNumber unique tracking number (e.g. ABC0001)
     * @param receiverName   name of receiver
     * @param weight         actual weight in kg (> 0)
     * @param deliveryService strategy used for delivery calculation
     * @throws InvalidParcelException if any general parcel rule is violated
     */
    public Parcel(String trackingNumber, String receiverName, double weight, DeliveryServiceStrategy deliveryService)
            throws InvalidParcelException {
        if (trackingNumber == null) {
            throw new InvalidParcelException("Tracking number cannot be null!");
        }
        String trimmedTracking = trackingNumber.trim().toUpperCase();
        if (!trimmedTracking.matches("^ABC\\d{4}$")) {
            throw new InvalidParcelException("Invalid tracking number! Must be 'ABC' followed by 4 digits (e.g., ABC0001).");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new InvalidParcelException("Receiver name cannot be empty!");
        }
        String trimmedReceiver = receiverName.trim();
        if (trimmedReceiver.length() > 50) {
            throw new InvalidParcelException("Receiver name cannot exceed 50 characters!");
        }
        if (!trimmedReceiver.matches(".*[a-zA-Z].*")) {
            throw new InvalidParcelException("Receiver name must contain at least one letter!");
        }
        if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0) {
            throw new InvalidParcelException("Weight must be greater than 0 kg!");
        }
        if (deliveryService == null) {
            throw new InvalidParcelException("Delivery service must be specified!");
        }

        this.trackingNumber = trimmedTracking;
        this.receiverName = trimmedReceiver;
        this.weight = weight;
        this.deliveryService = deliveryService;
        this.status = ParcelStatus.PENDING;
    }

    /**
     * Polymorphic method to get the chargeable weight of the parcel.
     *
     * @return chargeable weight in kilograms
     */
    public abstract double getChargeableWeight();

    /**
     * Polymorphic method to calculate the base fee of the parcel.
     *
     * @return base fee in PHP
     */
    public abstract double calculateBaseFee();

    /**
     * Returns the parcel type name (e.g., Document, Box, Fragile).
     *
     * @return parcel type string
     */
    public abstract String getParcelType();

    /**
     * Calculates the delivery fee using the configured delivery strategy.
     *
     * @return total delivery fee in PHP
     */
    public double getDeliveryFee() {
        try {
            return deliveryService.calculateFee(calculateBaseFee(), getChargeableWeight());
        } catch (DeliveryServiceNotAllowedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Changes the delivery service strategy for this parcel after validating eligibility.
     *
     * @param newService the new delivery service strategy
     * @throws DeliveryServiceNotAllowedException if the parcel is ineligible
     * @throws ParcelAlreadyDeliveredException if the parcel is already delivered
     */
    public void setDeliveryService(DeliveryServiceStrategy newService)
            throws DeliveryServiceNotAllowedException, ParcelAlreadyDeliveredException {
        if (isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel already delivered!");
        }
        if (newService == null) {
            throw new IllegalArgumentException("Delivery service cannot be null.");
        }
        newService.validateEligibility(getChargeableWeight());
        this.deliveryService = newService;
    }

    /**
     * Marks the parcel as delivered.
     *
     * @throws ParcelAlreadyDeliveredException if the parcel is already delivered
     */
    public void markAsDelivered() throws ParcelAlreadyDeliveredException {
        if (this.status == ParcelStatus.DELIVERED) {
            throw new ParcelAlreadyDeliveredException("Parcel was already delivered.");
        }
        this.status = ParcelStatus.DELIVERED;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public double getWeight() {
        return weight;
    }

    public DeliveryServiceStrategy getDeliveryService() {
        return deliveryService;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Parcel status cannot be null.");
        }
        this.status = status;
    }

    public boolean isDelivered() {
        return this.status == ParcelStatus.DELIVERED;
    }
}
