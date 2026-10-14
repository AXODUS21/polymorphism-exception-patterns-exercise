/**
 * Represents a Fragile parcel, extending BoxParcel.
 * Additional field: Handling Note (string, non-empty).
 * Maximum weight: 30 kg.
 * Chargeable weight = same rule as Box.
 * Base fee = Box base fee + Php 100 handling fee.
 */
public class FragileParcel extends BoxParcel {

    public static final double MAX_FRAGILE_WEIGHT = 30.0;
    public static final double HANDLING_FEE = 100.0;
    public static final int MAX_NOTE_LENGTH = 100;

    private final String handlingNote;

    public FragileParcel(String trackingNumber, String receiverName, double weight,
                         DeliveryServiceStrategy deliveryService, int length, int width, int height,
                         String handlingNote)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        super(trackingNumber, receiverName, weight, deliveryService, length, width, height, MAX_FRAGILE_WEIGHT);

        if (handlingNote == null || handlingNote.trim().isEmpty()) {
            throw new InvalidParcelException("Handling note cannot be empty!");
        }
        String trimmedNote = handlingNote.trim();
        if (trimmedNote.length() > MAX_NOTE_LENGTH) {
            throw new InvalidParcelException("Handling note cannot exceed " + MAX_NOTE_LENGTH + " characters!");
        }

        this.handlingNote = trimmedNote;
    }

    @Override
    public double calculateBaseFee() {
        return super.calculateBaseFee() + HANDLING_FEE;
    }

    @Override
    public String getParcelType() {
        return "Fragile";
    }

    public String getHandlingNote() {
        return handlingNote;
    }
}
