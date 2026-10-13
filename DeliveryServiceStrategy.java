/**
 * Strategy interface defining the delivery service algorithm and fee calculation.
 */
public interface DeliveryServiceStrategy {

    /**
     * Gets the display name of the delivery service.
     *
     * @return name of the delivery service
     */
    String getName();

    /**
     * Validates whether a parcel with the given chargeable weight is eligible for this service.
     *
     * @param chargeableWeight chargeable weight in kilograms
     * @throws DeliveryServiceNotAllowedException if the parcel is not eligible
     */
    void validateEligibility(double chargeableWeight) throws DeliveryServiceNotAllowedException;

    /**
     * Calculates the delivery fee based on base fee and chargeable weight.
     *
     * @param baseFee          base fee of the parcel in PHP
     * @param chargeableWeight chargeable weight of the parcel in kg
     * @return calculated delivery fee in PHP
     * @throws DeliveryServiceNotAllowedException if the parcel is not eligible
     */
    double calculateFee(double baseFee, double chargeableWeight) throws DeliveryServiceNotAllowedException;
}
