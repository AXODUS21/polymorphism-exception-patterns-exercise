/**
 * Standard delivery service strategy.
 * Delivery fee = Base fee.
 */
public class StandardDeliveryStrategy implements DeliveryServiceStrategy {

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public void validateEligibility(double chargeableWeight) {
        // Standard delivery is available for all eligible parcels
    }

    @Override
    public double calculateFee(double baseFee, double chargeableWeight) {
        return baseFee;
    }

    @Override
    public String toString() {
        return getName();
    }
}
