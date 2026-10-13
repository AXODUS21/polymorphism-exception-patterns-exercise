/**
 * Represents a Document parcel.
 * Max weight: 0.5 kg.
 * Pages: 1 to 100.
 * Chargeable weight = actual weight.
 * Base fee = Php 50 flat.
 */
public class DocumentParcel extends Parcel {

    public static final double MAX_WEIGHT = 0.5;
    public static final double BASE_FEE = 50.0;
    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;

    private final int numberOfPages;

    public DocumentParcel(String trackingNumber, String receiverName, double weight,
                          DeliveryServiceStrategy deliveryService, int numberOfPages)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        super(trackingNumber, receiverName, weight, deliveryService);

        if (weight > MAX_WEIGHT) {
            throw new InvalidParcelException("Document weight cannot exceed " + MAX_WEIGHT + " kg!");
        }
        if (numberOfPages < MIN_PAGES || numberOfPages > MAX_PAGES) {
            throw new InvalidParcelException("Number of pages must be between " + MIN_PAGES + " and " + MAX_PAGES + "!");
        }

        this.numberOfPages = numberOfPages;
        // Validate delivery service eligibility with chargeable weight
        deliveryService.validateEligibility(getChargeableWeight());
    }

    @Override
    public double getChargeableWeight() {
        return getWeight();
    }

    @Override
    public double calculateBaseFee() {
        return BASE_FEE;
    }

    @Override
    public String getParcelType() {
        return "Document";
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }
}
