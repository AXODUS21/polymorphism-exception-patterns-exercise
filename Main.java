import java.util.List;
import java.util.Scanner;

/**
 * Console user interface for the Parcel Delivery System.
 * Demonstrates clean UI/UX, robust input validation (rejecting leading zeroes,
 * invalid decimal formats, etc.), and seamless navigation with cancellation options.
 */
public class Main {

    private static final int CANCEL_INT = -1;
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
     * Displays the main menu with a clean visual banner.
     */
    private void displayMenu() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║            📦 PARCEL DELIVERY SYSTEM             ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println("  [1] Add Parcel");
        System.out.println("  [2] View All Parcels");
        System.out.println("  [3] Change Delivery Service");
        System.out.println("  [4] Mark as Delivered");
        System.out.println("  [5] Exit");
        System.out.println("════════════════════════════════════════════════════");
    }

    /**
     * Handles adding a new parcel to the system with cancellation support.
     */
    private void handleAddParcel() {
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("│                 ➕ ADD NEW PARCEL                │");
        System.out.println("└──────────────────────────────────────────────────┘");
        System.out.println("Tip: Enter '0' at any prompt to cancel and return to menu.\n");

        // 1. Select Parcel Type
        int typeChoice = 0;
        while (typeChoice < 1 || typeChoice > 3) {
            System.out.println("Select Parcel Type:");
            System.out.println("  [1] Document (Base: Php 50.00 flat, Max: 0.50 kg, 1-100 pages)");
            System.out.println("  [2] Box      (Base: Php 40.00/kg, Max: 50.00 kg, L x W x H)");
            System.out.println("  [3] Fragile  (Base: Box fee + Php 100.00, Max: 30.00 kg)");
            System.out.println("  [0] Cancel");
            typeChoice = readStrictInt("Enter choice (1-3, or 0 to Cancel): ", true);
            if (typeChoice == CANCEL_INT) {
                printCancelled();
                return;
            }
            if (typeChoice < 1 || typeChoice > 3) {
                System.out.println("Invalid parcel type! Please choose 1, 2, or 3.\n");
            }
        }

        // 2. Tracking Number
        String trackingNumber;
        while (true) {
            trackingNumber = readInput("Enter Tracking Number (e.g. ABC0001) [0 to Cancel]: ").toUpperCase();
            if (isCancel(trackingNumber)) {
                printCancelled();
                return;
            }
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
            receiverName = readInput("Enter Receiver Name [0 to Cancel]: ");
            if (isCancel(receiverName)) {
                printCancelled();
                return;
            }
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
            weight = readStrictDouble("Enter Weight in kg [0 to Cancel]: ", true);
            if (Double.isNaN(weight)) {
                printCancelled();
                return;
            }
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
                numberOfPages = readStrictInt("Enter Number of Pages (1 to 100) [0 to Cancel]: ", true);
                if (numberOfPages == CANCEL_INT) {
                    printCancelled();
                    return;
                }
                if (numberOfPages < DocumentParcel.MIN_PAGES || numberOfPages > DocumentParcel.MAX_PAGES) {
                    System.out.println("Number of pages must be between 1 and 100!");
                    continue;
                }
                break;
            }
        } else { // Box or Fragile
            while (true) {
                length = readStrictInt("Enter Length in cm [0 to Cancel]: ", true);
                if (length == CANCEL_INT) {
                    printCancelled();
                    return;
                }
                if (length <= 0) {
                    System.out.println("Length must be greater than 0!");
                    continue;
                }
                break;
            }
            while (true) {
                width = readStrictInt("Enter Width in cm [0 to Cancel]: ", true);
                if (width == CANCEL_INT) {
                    printCancelled();
                    return;
                }
                if (width <= 0) {
                    System.out.println("Width must be greater than 0!");
                    continue;
                }
                break;
            }
            while (true) {
                height = readStrictInt("Enter Height in cm [0 to Cancel]: ", true);
                if (height == CANCEL_INT) {
                    printCancelled();
                    return;
                }
                if (height <= 0) {
                    System.out.println("Height must be greater than 0!");
                    continue;
                }
                break;
            }

            if (typeChoice == 3) { // Fragile
                while (true) {
                    handlingNote = readInput("Enter Handling Note [0 to Cancel]: ");
                    if (isCancel(handlingNote)) {
                        printCancelled();
                        return;
                    }
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
            System.out.println("  [1] Standard - Base fee");
            System.out.println("  [2] Express  - Base fee × 1.5");
            System.out.println("  [3] Same Day - (Base fee × 2) + Php 100 (Max 10 kg)");
            System.out.println("  [0] Cancel");
            int serviceChoice = readStrictInt("Enter choice (1-3, or 0 to Cancel): ", true);
            if (serviceChoice == CANCEL_INT) {
                printCancelled();
                return;
            }

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
            displayParcelReceipt(parcel);
        } catch (InvalidParcelException | DeliveryServiceNotAllowedException | DuplicateTrackingNumberException e) {
            System.out.println("Error adding parcel: " + e.getMessage());
        }
    }

    /**
     * Displays all parcels in a formatted table with summary statistics.
     */
    private void handleViewAllParcels() {
        System.out.println("┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                                       📦 ALL REGISTERED PARCELS                                        │");
        System.out.println("└────────────────────────────────────────────────────────────────────────────────────────────────────────┘");
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

        int pendingCount = 0;
        int deliveredCount = 0;
        double totalFees = 0.0;

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

            if (p.isDelivered()) {
                deliveredCount++;
            } else {
                pendingCount++;
            }
            totalFees += p.getDeliveryFee();
        }
        System.out.println(border);
        System.out.printf("Summary: %d Total Parcel(s) | %d Pending | %d Delivered | Total Expected Fees: Php %.2f%n",
                parcels.size(), pendingCount, deliveredCount, totalFees);
    }

    /**
     * Changes the delivery service of a pending parcel.
     */
    private void handleChangeDeliveryService() {
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("│             🔄 CHANGE DELIVERY SERVICE           │");
        System.out.println("└──────────────────────────────────────────────────┘");
        String trackingNumber = readInput("Enter Tracking Number [0 to Cancel]: ").toUpperCase();
        if (isCancel(trackingNumber)) {
            printCancelled();
            return;
        }

        Parcel parcel = parcelManager.findParcel(trackingNumber);
        if (parcel == null) {
            System.out.println("Parcel not found!");
            return;
        }

        if (parcel.isDelivered()) {
            System.out.println("Parcel already delivered!");
            return;
        }

        System.out.printf("Parcel: %s (%s) | Current Service: %s | Current Fee: Php %.2f%n",
                parcel.getTrackingNumber(), parcel.getParcelType(),
                parcel.getDeliveryService().getName(), parcel.getDeliveryFee());
        System.out.println("Select New Delivery Service:");
        System.out.println("  [1] Standard");
        System.out.println("  [2] Express");
        System.out.println("  [3] Same Day");
        System.out.println("  [0] Cancel (keep current service)");

        DeliveryServiceStrategy newService = null;
        while (newService == null) {
            int choice = readStrictInt("Enter choice (1-3, or 0 to Cancel): ", true);
            if (choice == CANCEL_INT) {
                printCancelled();
                return;
            }
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
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("│               📬 MARK AS DELIVERED               │");
        System.out.println("└──────────────────────────────────────────────────┘");
        String trackingNumber = readInput("Enter Tracking Number [0 to Cancel]: ").toUpperCase();
        if (isCancel(trackingNumber)) {
            printCancelled();
            return;
        }

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
     * Reads a strict integer from the console, rejecting leading zeroes (e.g. '01', '001')
     * and non-integer inputs.
     *
     * @param prompt      prompt message
     * @param allowCancel if true, entering '0' returns CANCEL_INT (-1)
     * @return the parsed positive integer or CANCEL_INT if cancelled
     */
    private int readStrictInt(String prompt, boolean allowCancel) {
        while (true) {
            String input = readInput(prompt);

            if (allowCancel && isCancel(input)) {
                return CANCEL_INT;
            }

            // Check for illegal leading zeroes (e.g. 01, 001, 007)
            if (input.matches("^0\\d+$")) {
                System.out.println("Invalid input! Leading zeroes (e.g., '01', '001') are not allowed. Please enter a valid number.");
                continue;
            }

            if (!input.matches("^[1-9]\\d*$")) {
                if (input.equals("0")) {
                    System.out.println("Value must be greater than 0!");
                } else if (input.startsWith("-")) {
                    System.out.println("Negative numbers are not allowed!");
                } else {
                    System.out.println("Invalid input! Please enter a valid whole number without leading zeros.");
                }
                continue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Number is too large! Please enter a valid number.");
            }
        }
    }

    /**
     * Reads a strict double from the console, rejecting leading zeroes (e.g. '01', '001', '01.5', '00.5'),
     * malformed decimal points ('.5', '5.'), and special non-finite numbers (NaN, Infinity).
     *
     * @param prompt      prompt message
     * @param allowCancel if true, entering '0' returns Double.NaN to signal cancellation
     * @return the parsed positive decimal number or Double.NaN if cancelled
     */
    private double readStrictDouble(String prompt, boolean allowCancel) {
        while (true) {
            String input = readInput(prompt);

            if (allowCancel && isCancel(input)) {
                return Double.NaN;
            }

            // Check for illegal leading zeroes (e.g. 01, 001, 01.5, 00.5)
            if (input.matches("^0\\d+(\\.\\d+)?$") || input.matches("^0{2,}\\..*$")) {
                System.out.println("Invalid input! Leading zeroes (e.g., '01', '001') are not allowed. Please enter a valid decimal number.");
                continue;
            }

            // Check for misplaced decimal point (e.g. .5 or 5.)
            if (input.startsWith(".") || input.endsWith(".")) {
                System.out.println("Invalid input! Please use standard decimal format (e.g. 0.5 instead of .5).");
                continue;
            }

            // Strict positive decimal regex: must be 0.xxx or 1-9xxx(.xxx)
            if (!input.matches("^(0\\.\\d+|[1-9]\\d*(\\.\\d+)?)$")) {
                if (input.equals("0") || input.equals("0.0") || input.equals("0.00")) {
                    System.out.println("Weight must be greater than 0 kg!");
                } else {
                    System.out.println("Invalid input! Please enter a valid positive decimal number without leading zeros.");
                }
                continue;
            }

            try {
                double val = Double.parseDouble(input);
                if (Double.isNaN(val) || Double.isInfinite(val) || val <= 0) {
                    System.out.println("Weight must be greater than 0 kg!");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid decimal number.");
            }
        }
    }

    /**
     * Checks if user entered a cancellation keyword.
     */
    private boolean isCancel(String input) {
        return input != null && (input.equals("0") || input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("back"));
    }

    /**
     * Prints a consistent cancellation notice.
     */
    private void printCancelled() {
        System.out.println(">> Operation cancelled. Returning to main menu.\n");
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
     * Displays a clean formatted receipt card for a parcel.
     */
    private void displayParcelReceipt(Parcel parcel) {
        System.out.println("┌──────────────────────────────────────────────────┐");
        System.out.println("│                  PARCEL RECEIPT                  │");
        System.out.println("├──────────────────────────────────────────────────┤");
        System.out.printf("│ Tracking Number   : %-28s │%n", parcel.getTrackingNumber());
        System.out.printf("│ Parcel Type       : %-28s │%n", parcel.getParcelType());
        System.out.printf("│ Receiver          : %-28s │%n", truncate(parcel.getReceiverName(), 28));
        System.out.printf("│ Chargeable Weight : %-28s │%n", formatChargeableWeight(parcel.getChargeableWeight()));
        System.out.printf("│ Delivery Service  : %-28s │%n", parcel.getDeliveryService().getName());
        System.out.printf("│ Base Fee          : Php %-24.2f │%n", parcel.calculateBaseFee());
        System.out.printf("│ Delivery Fee      : Php %-24.2f │%n", parcel.getDeliveryFee());
        System.out.printf("│ Status            : %-28s │%n", parcel.getStatus().getDisplayName());
        System.out.println("└──────────────────────────────────────────────────┘");
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
