import java.util.List;
import java.util.Scanner;

/**
 * Console user interface for the Parcel Delivery System.
 * Handles user interactions, menus, and input validations.
 */
public class Main {

    private final ParcelManager parcelManager;
    private final Scanner scanner;

    public Main() {
        this.parcelManager = ParcelManager.getInstance();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * Main application loop.
     */
    public void run() {
        boolean running = true;
        while (running) {
            displayMenu();
            String choice = readInput("Enter choice (1-5): ");
            System.out.println();

            switch (choice) {
                case "1":
                    handleAddParcel();
                    break;
                case "2":
                    handleViewAllParcels();
                    break;
                case "3":
                    handleChangeDeliveryService();
                    break;
                case "4":
                    handleMarkAsDelivered();
                    break;
                case "5":
                    System.out.println("Thank you for using the Parcel Delivery System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice! Please select an option between 1 and 5.");
                    break;
            }
            System.out.println();
        }
    }

    /**
     * Displays the main menu.
     */
    private void displayMenu() {
        System.out.println("==================================================");
        System.out.println("            PARCEL DELIVERY SYSTEM                ");
        System.out.println("==================================================");
        System.out.println("1. Add Parcel");
        System.out.println("2. View All Parcels");
        System.out.println("3. Change Delivery Service");
        System.out.println("4. Mark as Delivered");
        System.out.println("5. Exit");
        System.out.println("==================================================");
    }

    /**
     * Handles adding a new parcel to the system.
     */
    private void handleAddParcel() {
        System.out.println("--- Add New Parcel ---");

        // 1. Select Parcel Type
        int typeChoice = 0;
        while (typeChoice < 1 || typeChoice > 3) {
            System.out.println("Select Parcel Type:");
            System.out.println("  1. Document");
            System.out.println("  2. Box");
            System.out.println("  3. Fragile");
            typeChoice = readInt("Enter choice (1-3): ");
            if (typeChoice < 1 || typeChoice > 3) {
                System.out.println("Invalid parcel type! Please choose 1, 2, or 3.\n");
            }
        }

        // 2. Tracking Number
        String trackingNumber;
        while (true) {
            trackingNumber = readInput("Enter Tracking Number (e.g. ABC0001): ").toUpperCase();
            if (!trackingNumber.matches("^ABC\\d{4}$")) {
                System.out.println("Invalid tracking number! Must be 'ABC' followed by 4 digits (e.g., ABC0001).");
                continue;
            }
            if (parcelManager.containsTrackingNumber(trackingNumber)) {
                System.out.println("Tracking number " + trackingNumber + " already exists! Please use a unique number.");
                continue;
            }
            break;
        }

        // 3. Receiver Name
        String receiverName;
        while (true) {
            receiverName = readInput("Enter Receiver Name: ");
            if (receiverName.trim().isEmpty()) {
                System.out.println("Receiver name cannot be empty!");
                continue;
            }
            break;
        }

        // 4. Weight in kg
        double weight;
        double maxWeightForType = (typeChoice == 1) ? DocumentParcel.MAX_WEIGHT
                : (typeChoice == 2) ? BoxParcel.MAX_WEIGHT
                : FragileParcel.MAX_FRAGILE_WEIGHT;

        while (true) {
            weight = readDouble("Enter Weight in kg: ");
            if (weight <= 0) {
                System.out.println("Weight must be greater than 0 kg!");
                continue;
            }
            if (weight > maxWeightForType) {
                System.out.printf("Weight exceeds maximum limit of %.2f kg for this parcel type!%n", maxWeightForType);
                continue;
            }
            break;
        }

        // 5. Type-specific inputs
        int numberOfPages = 0;
        int length = 0;
        int width = 0;
        int height = 0;
        String handlingNote = "";

        if (typeChoice == 1) { // Document
            while (true) {
                numberOfPages = readInt("Enter Number of Pages (1 to 100): ");
                if (numberOfPages < DocumentParcel.MIN_PAGES || numberOfPages > DocumentParcel.MAX_PAGES) {
                    System.out.println("Number of pages must be between 1 and 100!");
                    continue;
                }
                break;
            }
        } else { // Box or Fragile
            while (true) {
                length = readInt("Enter Length in cm: ");
                if (length <= 0) {
                    System.out.println("Length must be greater than 0!");
                    continue;
                }
                break;
            }
            while (true) {
                width = readInt("Enter Width in cm: ");
                if (width <= 0) {
                    System.out.println("Width must be greater than 0!");
                    continue;
                }
                break;
            }
            while (true) {
                height = readInt("Enter Height in cm: ");
                if (height <= 0) {
                    System.out.println("Height must be greater than 0!");
                    continue;
                }
                break;
            }

            if (typeChoice == 3) { // Fragile
                while (true) {
                    handlingNote = readInput("Enter Handling Note: ");
                    if (handlingNote.trim().isEmpty()) {
                        System.out.println("Handling note cannot be empty!");
                        continue;
                    }
                    break;
                }
            }
        }

        // Calculate chargeable weight preview for delivery service eligibility check
        double chargeableWeight;
        if (typeChoice == 1) {
            chargeableWeight = weight;
        } else {
            double volWeight = ((double) length * (double) width * (double) height) / BoxParcel.VOLUMETRIC_DIVISOR;
            double roundedMax = Math.round(Math.max(weight, volWeight) * 1e8) / 1e8;
            chargeableWeight = Math.ceil(roundedMax);
        }

        // 6. Select Delivery Service
        DeliveryServiceStrategy deliveryService = null;
        while (deliveryService == null) {
            System.out.println("Select Delivery Service:");
            System.out.println("  1. Standard");
            System.out.println("  2. Express");
            System.out.println("  3. Same Day");
            int serviceChoice = readInt("Enter choice (1-3): ");

            switch (serviceChoice) {
                case 1:
                    deliveryService = new StandardDeliveryStrategy();
                    break;
                case 2:
                    deliveryService = new ExpressDeliveryStrategy();
                    break;
                case 3:
                    DeliveryServiceStrategy candidate = new SameDayDeliveryStrategy();
                    try {
                        candidate.validateEligibility(chargeableWeight);
                        deliveryService = candidate;
                    } catch (DeliveryServiceNotAllowedException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Please select a delivery service again.\n");
                    }
                    break;
                default:
                    System.out.println("Invalid service option! Please choose 1, 2, or 3.\n");
                    break;
            }
        }

        // 7. Instantiate and register parcel
        try {
            Parcel parcel;
            if (typeChoice == 1) {
                parcel = new DocumentParcel(trackingNumber, receiverName, weight, deliveryService, numberOfPages);
            } else if (typeChoice == 2) {
                parcel = new BoxParcel(trackingNumber, receiverName, weight, deliveryService, length, width, height);
            } else {
                parcel = new FragileParcel(trackingNumber, receiverName, weight, deliveryService, length, width, height, handlingNote);
            }

            parcelManager.addParcel(parcel);
            System.out.printf("Parcel added successfully! Delivery fee: Php %.2f%n", parcel.getDeliveryFee());
        } catch (InvalidParcelException | DeliveryServiceNotAllowedException | DuplicateTrackingNumberException e) {
            System.out.println("Error adding parcel: " + e.getMessage());
        }
    }

    /**
     * Displays all parcels in a formatted table.
     */
    private void handleViewAllParcels() {
        System.out.println("--- All Registered Parcels ---");
        List<Parcel> parcels = parcelManager.getAllParcels();

        if (parcels.isEmpty()) {
            System.out.println("No parcels found in the system.");
            return;
        }

        String border = "+-----------------+------------+----------------------+--------------------+--------------+------------------+------------+";
        System.out.println(border);
        System.out.printf("| %-15s | %-10s | %-20s | %-18s | %-12s | %-16s | %-10s |%n",
                "Tracking Number", "Type", "Receiver", "Chargeable Weight", "Service", "Delivery Fee", "Status");
        System.out.println(border);

        for (Parcel p : parcels) {
            String chargeableWeightStr = formatChargeableWeight(p.getChargeableWeight());
            String feeStr = String.format("Php %.2f", p.getDeliveryFee());
            System.out.printf("| %-15s | %-10s | %-20s | %-18s | %-12s | %-16s | %-10s |%n",
                    p.getTrackingNumber(),
                    p.getParcelType(),
                    truncate(p.getReceiverName(), 20),
                    chargeableWeightStr,
                    p.getDeliveryService().getName(),
                    feeStr,
                    p.getStatus().getDisplayName());
        }
        System.out.println(border);
    }

    /**
     * Changes the delivery service of a pending parcel.
     */
    private void handleChangeDeliveryService() {
        System.out.println("--- Change Delivery Service ---");
        String trackingNumber = readInput("Enter Tracking Number: ").toUpperCase();

        Parcel parcel = parcelManager.findParcel(trackingNumber);
        if (parcel == null) {
            System.out.println("Parcel not found!");
            return;
        }

        if (parcel.isDelivered()) {
            System.out.println("Parcel already delivered!");
            return;
        }

        System.out.printf("Current Service: %s | Current Fee: Php %.2f%n",
                parcel.getDeliveryService().getName(), parcel.getDeliveryFee());
        System.out.println("Select New Delivery Service:");
        System.out.println("  1. Standard");
        System.out.println("  2. Express");
        System.out.println("  3. Same Day");

        DeliveryServiceStrategy newService = null;
        while (newService == null) {
            int choice = readInt("Enter choice (1-3): ");
            switch (choice) {
                case 1:
                    newService = new StandardDeliveryStrategy();
                    break;
                case 2:
                    newService = new ExpressDeliveryStrategy();
                    break;
                case 3:
                    newService = new SameDayDeliveryStrategy();
                    break;
                default:
                    System.out.println("Invalid service option! Please choose 1, 2, or 3.\n");
                    break;
            }
        }

        try {
            parcelManager.changeDeliveryService(trackingNumber, newService);
            System.out.printf("Delivery service changed! New fee: Php %.2f%n", parcel.getDeliveryFee());
        } catch (DeliveryServiceNotAllowedException e) {
            System.out.println(e.getMessage());
            // The parcel keeps its old service
        } catch (ParcelNotFoundException | ParcelAlreadyDeliveredException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Marks a pending parcel as delivered.
     */
    private void handleMarkAsDelivered() {
        System.out.println("--- Mark as Delivered ---");
        String trackingNumber = readInput("Enter Tracking Number: ").toUpperCase();

        Parcel parcel = parcelManager.findParcel(trackingNumber);
        if (parcel == null) {
            System.out.println("Parcel not found!");
            return;
        }

        if (parcel.isDelivered()) {
            System.out.println("Parcel was already delivered.");
            return;
        }

        try {
            parcelManager.markAsDelivered(trackingNumber);
            System.out.println("Parcel delivered successfully!");
        } catch (ParcelNotFoundException | ParcelAlreadyDeliveredException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Helper to read a non-null string from the console.
     */
    private String readInput(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        String input = scanner.nextLine();
        return (input == null) ? "" : input.trim();
    }

    /**
     * Helper to safely read an integer.
     */
    private int readInt(String prompt) {
        while (true) {
            String input = readInput(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid integer.");
            }
        }
    }

    /**
     * Helper to safely read a double, rejecting NaN and Infinities.
     */
    private double readDouble(String prompt) {
        while (true) {
            String input = readInput(prompt);
            try {
                double val = Double.parseDouble(input);
                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    System.out.println("Invalid input! Please enter a valid finite decimal number.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid decimal number.");
            }
        }
    }

    /**
     * Formats chargeable weight cleanly without unnecessary trailing zeros on whole numbers.
     * Examples: 3.0 -> "3 kg", 0.4 -> "0.4 kg", 0.25 -> "0.25 kg".
     */
    private String formatChargeableWeight(double weight) {
        if (weight == (long) weight) {
            return String.format("%d kg", (long) weight);
        } else {
            String str = String.format(java.util.Locale.US, "%.2f", weight);
            if (str.endsWith("0")) {
                str = str.substring(0, str.length() - 1);
            }
            return str + " kg";
        }
    }

    /**
     * Truncates long strings to fit tabular display nicely.
     */
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
