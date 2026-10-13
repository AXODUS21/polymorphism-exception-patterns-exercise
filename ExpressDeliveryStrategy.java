/**
 * Express delivery service strategy.
 * Delivery fee = Base fee * 1.5.
 */
public class ExpressDeliveryStrategy implements DeliveryServiceStrategy {

    @Override
    public String getName() {
        return "Express";
    }

    @Override
    public void validateEligibility(double chargeableWeight) {
        // Express delivery is available for all eligible parcels
    }

    @Override
    public double calculateFee(double baseFee, double chargeableWeight) {
        return baseFee * 1.5;
    }

    @Override
    public String toString() {
        return getName();
    }
}
