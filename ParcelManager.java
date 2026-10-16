import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class managing the storage and operations on all parcels.
 * Implements the Singleton design pattern.
 */
public class ParcelManager {

    private static ParcelManager instance;
    private final Map<String, Parcel> parcelRepository;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ParcelManager() {
        this.parcelRepository = new LinkedHashMap<>();
    }

    /**
     * Retrieves the single global instance of ParcelManager (Singleton pattern).
     * Thread-safe using synchronized method.
     *
     * @return the singleton ParcelManager instance
     */
    public static synchronized ParcelManager getInstance() {
        if (instance == null) {
            instance = new ParcelManager();
        }
        return instance;
    }

    /**
     * Adds a new parcel to the system.
     *
     * @param parcel the parcel to add
     * @throws DuplicateTrackingNumberException if tracking number already exists
     */
    public synchronized void addParcel(Parcel parcel) throws DuplicateTrackingNumberException {
        if (parcel == null) {
            throw new IllegalArgumentException("Parcel cannot be null.");
        }
        String key = parcel.getTrackingNumber().toUpperCase();
        if (parcelRepository.containsKey(key)) {
            throw new DuplicateTrackingNumberException("Tracking number " + parcel.getTrackingNumber() + " already exists!");
        }
        parcelRepository.put(key, parcel);
    }

    /**
     * Searches for a parcel by tracking number.
     *
     * @param trackingNumber the tracking number to search
     * @return the Parcel if found, or null otherwise
     */
    public synchronized Parcel findParcel(String trackingNumber) {
        if (trackingNumber == null) {
            return null;
        }
        return parcelRepository.get(trackingNumber.trim().toUpperCase());
    }

    /**
     * Checks if a tracking number is already registered in the system.
     *
     * @param trackingNumber tracking number to check
     * @return true if exists, false otherwise
     */
    public synchronized boolean containsTrackingNumber(String trackingNumber) {
        if (trackingNumber == null) {
            return false;
        }
        return parcelRepository.containsKey(trackingNumber.trim().toUpperCase());
    }

    /**
     * Returns an unmodifiable list of all registered parcels in order of insertion.
     *
     * @return list of parcels
     */
    public synchronized List<Parcel> getAllParcels() {
        return Collections.unmodifiableList(new ArrayList<>(parcelRepository.values()));
    }

    /**
     * Checks if there are no parcels registered in the system.
     *
     * @return true if empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return parcelRepository.isEmpty();
    }

    /**
     * Changes the delivery service of a parcel.
     *
     * @param trackingNumber tracking number of the parcel
     * @param newService     the new delivery service strategy
     * @throws ParcelNotFoundException            if parcel does not exist
     * @throws ParcelAlreadyDeliveredException     if parcel is already delivered
     * @throws DeliveryServiceNotAllowedException  if new service is not eligible
     */
    public synchronized void changeDeliveryService(String trackingNumber, DeliveryServiceStrategy newService)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException, DeliveryServiceNotAllowedException {
        if (newService == null) {
            throw new IllegalArgumentException("New delivery service cannot be null.");
        }
        Parcel parcel = findParcel(trackingNumber);
        if (parcel == null) {
            throw new ParcelNotFoundException("Parcel not found!");
        }
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel already delivered!");
        }
        parcel.setDeliveryService(newService);
    }

    /**
     * Marks a parcel as delivered.
     *
     * @param trackingNumber tracking number of the parcel
     * @throws ParcelNotFoundException        if parcel does not exist
     * @throws ParcelAlreadyDeliveredException if parcel is already delivered
     */
    public synchronized void markAsDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel == null) {
            throw new ParcelNotFoundException("Parcel not found!");
        }
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException("Parcel was already delivered.");
        }
        parcel.markAsDelivered();
    }

    /**
     * Clears all registered parcels. Useful for resetting state during testing.
     */
    public synchronized void clear() {
        parcelRepository.clear();
    }
}
