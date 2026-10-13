/**
 * Represents a Box parcel.
 * Dimensions: Length, Width, Height in cm (integer, > 0).
 * Max weight: 50 kg.
 * Volumetric weight = (Length * Width * Height) / 3500.
 * Chargeable weight = ceil(max(actual weight, volumetric weight)).
 * Base fee = Php 40 * chargeable weight.
 */
public class BoxParcel extends Parcel {

    public static final double MAX_WEIGHT = 50.0;
    public static final double RATE_PER_KG = 40.0;
    public static final double VOLUMETRIC_DIVISOR = 3500.0;

    private final int length;
    private final int width;
    private final int height;

    public BoxParcel(String trackingNumber, String receiverName, double weight,
                     DeliveryServiceStrategy deliveryService, int length, int width, int height)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        this(trackingNumber, receiverName, weight, deliveryService, length, width, height, MAX_WEIGHT);
    }

    protected BoxParcel(String trackingNumber, String receiverName, double weight,
                        DeliveryServiceStrategy deliveryService, int length, int width, int height,
                        double maxAllowedWeight)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        super(trackingNumber, receiverName, weight, deliveryService);

        if (weight > maxAllowedWeight) {
            throw new InvalidParcelException("Weight cannot exceed " + maxAllowedWeight + " kg!");
        }
        if (length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidParcelException("Dimensions (length, width, height) must be greater than 0 cm!");
        }

        this.length = length;
        this.width = width;
        this.height = height;

        // Validate delivery service eligibility with chargeable weight
        deliveryService.validateEligibility(getChargeableWeight());
    }

    /**
     * Calculates volumetric weight in kilograms.
     * Formula: (Length * Width * Height) / 3500
     *
     * @return volumetric weight in kg
     */
    public double getVolumetricWeight() {
        return ((double) length * width * height) / VOLUMETRIC_DIVISOR;
    }

    @Override
    public double getChargeableWeight() {
        double maxWeight = Math.max(getWeight(), getVolumetricWeight());
        return Math.ceil(maxWeight);
    }

    @Override
    public double calculateBaseFee() {
        return RATE_PER_KG * getChargeableWeight();
    }

    @Override
    public String getParcelType() {
        return "Box";
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
