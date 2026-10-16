/**
 * Unit and integration tests for the Parcel Delivery System.
 * Tests polymorphism, Strategy pattern, Singleton pattern, exception handling, and fee calculations.
 */
public class ParcelSystemTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("Running Parcel Delivery System Tests...\n");

        testSingletonPattern();
        testPolymorphismAndInheritance();
        testDocumentParcelCalculations();
        testDocumentParcelValidations();
        testBoxParcelCalculations();
        testBoxParcelValidations();
        testFragileParcelPromptExample();
        testFragileParcelValidations();
        testStrategyPatternAndEligibility();
        testParcelManagerOperations();
        testChangeDeliveryServiceWorkflow();
        testMarkAsDeliveredWorkflow();
        testEdgeCasesAndInputHandling();
        testNewInputValidations();

        System.out.println("\n-------------------------------------------");
        System.out.printf("Test Summary: %d/%d passed.%n", passedTests, totalTests);
        if (passedTests == totalTests) {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY! (100% OK)");
        } else {
            System.err.println("SOME TESTS FAILED!");
            System.exit(1);
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String testName) {
        totalTests++;
        if (Math.abs(expected - actual) <= delta) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.printf("[FAIL] %s - Expected: %f, Actual: %f%n", testName, expected, actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        totalTests++;
        if (expected == null && actual == null || (expected != null && expected.equals(actual))) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.printf("[FAIL] %s - Expected: %s, Actual: %s%n", testName, expected, actual);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.printf("[FAIL] %s - Expected true, got false%n", testName);
        }
    }

    private static void testSingletonPattern() {
        ParcelManager instance1 = ParcelManager.getInstance();
        ParcelManager instance2 = ParcelManager.getInstance();
        assertTrue(instance1 != null, "ParcelManager instance should not be null");
        assertTrue(instance1 == instance2, "ParcelManager must follow Singleton pattern (identical references)");
    }

    private static void testPolymorphismAndInheritance() {
        try {
            DeliveryServiceStrategy standard = new StandardDeliveryStrategy();
            Parcel doc = new DocumentParcel("ABC0001", "Alice", 0.3, standard, 10);
            Parcel box = new BoxParcel("ABC0002", "Bob", 5.0, standard, 30, 20, 10);
            Parcel fragile = new FragileParcel("ABC0003", "Charlie", 2.0, standard, 30, 20, 10, "Glassware");

            // Polymorphism checks
            assertTrue(fragile instanceof BoxParcel, "FragileParcel must extend BoxParcel");
            assertTrue(fragile instanceof Parcel, "FragileParcel must be an instance of Parcel");
            assertTrue(box instanceof Parcel, "BoxParcel must be an instance of Parcel");
            assertTrue(doc instanceof Parcel, "DocumentParcel must be an instance of Parcel");

            assertEquals("Document", doc.getParcelType(), "Document getParcelType");
            assertEquals("Box", box.getParcelType(), "Box getParcelType");
            assertEquals("Fragile", fragile.getParcelType(), "Fragile getParcelType");
        } catch (Exception e) {
            assertTrue(false, "Polymorphism test threw unexpected exception: " + e.getMessage());
        }
    }

    private static void testDocumentParcelCalculations() {
        try {
            // Actual weight 0.25 kg, 20 pages
            DocumentParcel doc = new DocumentParcel("ABC0010", "Dan", 0.25, new StandardDeliveryStrategy(), 20);
            assertEquals(0.25, doc.getChargeableWeight(), 0.001, "Document chargeable weight equals actual weight");
            assertEquals(50.0, doc.calculateBaseFee(), 0.001, "Document base fee is 50 flat");
            assertEquals(50.0, doc.getDeliveryFee(), 0.001, "Document Standard fee is 50.00");

            // Express
            doc.setDeliveryService(new ExpressDeliveryStrategy());
            assertEquals(75.0, doc.getDeliveryFee(), 0.001, "Document Express fee is 50 * 1.5 = 75.00");

            // Same Day (allowed since <= 10 kg)
            doc.setDeliveryService(new SameDayDeliveryStrategy());
            assertEquals(200.0, doc.getDeliveryFee(), 0.001, "Document Same Day fee is (50 * 2) + 100 = 200.00");
        } catch (Exception e) {
            assertTrue(false, "DocumentParcel calculation test failed: " + e.getMessage());
        }
    }

    private static void testDocumentParcelValidations() {
        // Exceeding max weight (0.5 kg)
        boolean caughtWeightExceeded = false;
        try {
            new DocumentParcel("ABC0011", "Eve", 0.51, new StandardDeliveryStrategy(), 10);
        } catch (InvalidParcelException e) {
            caughtWeightExceeded = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtWeightExceeded, "Document exceeding 0.5 kg throws InvalidParcelException");

        // Invalid pages (< 1 or > 100)
        boolean caughtInvalidPages = false;
        try {
            new DocumentParcel("ABC0012", "Eve", 0.4, new StandardDeliveryStrategy(), 101);
        } catch (InvalidParcelException e) {
            caughtInvalidPages = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtInvalidPages, "Document with 101 pages throws InvalidParcelException");
    }

    private static void testBoxParcelCalculations() {
        try {
            // Length 30, Width 20, Height 10 -> Volumetric = 6000 / 3500 = 1.714 kg
            // Actual weight = 2.0 kg -> max is 2.0 -> ceil is 2 kg
            BoxParcel box1 = new BoxParcel("ABC0020", "Frank", 2.0, new StandardDeliveryStrategy(), 30, 20, 10);
            assertEquals(2.0, box1.getChargeableWeight(), 0.001, "Box chargeable weight when actual weight is bigger (2.0 -> 2)");
            assertEquals(80.0, box1.calculateBaseFee(), 0.001, "Box base fee: Php 40 * 2 = 80");
            assertEquals(80.0, box1.getDeliveryFee(), 0.001, "Box standard fee: Php 80");

            // Length 40, Width 30, Height 25 -> Volumetric = 30000 / 3500 = 8.571 kg
            // Actual weight = 5.0 kg -> max is 8.571 -> ceil is 9 kg
            BoxParcel box2 = new BoxParcel("ABC0021", "Grace", 5.0, new ExpressDeliveryStrategy(), 40, 30, 25);
            assertEquals(9.0, box2.getChargeableWeight(), 0.001, "Box chargeable weight rounded UP to next whole kg (8.57 -> 9)");
            assertEquals(360.0, box2.calculateBaseFee(), 0.001, "Box base fee: Php 40 * 9 = 360");
            assertEquals(540.0, box2.getDeliveryFee(), 0.001, "Box express fee: 360 * 1.5 = 540");
        } catch (Exception e) {
            assertTrue(false, "BoxParcel calculation test failed: " + e.getMessage());
        }
    }

    private static void testBoxParcelValidations() {
        // Exceeding max weight (50 kg)
        boolean caughtWeightExceeded = false;
        try {
            new BoxParcel("ABC0022", "Hank", 50.1, new StandardDeliveryStrategy(), 10, 10, 10);
        } catch (InvalidParcelException e) {
            caughtWeightExceeded = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtWeightExceeded, "Box exceeding 50 kg throws InvalidParcelException");

        // Non-positive dimensions
        boolean caughtInvalidDim = false;
        try {
            new BoxParcel("ABC0023", "Hank", 5.0, new StandardDeliveryStrategy(), 10, 0, 10);
        } catch (InvalidParcelException e) {
            caughtInvalidDim = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtInvalidDim, "Box with 0 width throws InvalidParcelException");
    }

    private static void testFragileParcelPromptExample() {
        // From prompt:
        // Example: FRAGILE, 40 x 20 x 10 cm, 1.8 kg, EXPRESS
        // Volumetric weight = 8000 / 3500 = 2.29 kg -> chargeable weight 3 kg
        // Base fee = (Php 40 × 3) + Php 100 = Php 220
        // Delivery fee = Php 220 × 1.5 = Php 330.00
        try {
            FragileParcel fragile = new FragileParcel("ABC0030", "Ivy", 1.8,
                    new ExpressDeliveryStrategy(), 40, 20, 10, "Handle with care");

            assertEquals(8000.0 / 3500.0, fragile.getVolumetricWeight(), 0.0001, "Fragile volumetric weight calculation");
            assertEquals(3.0, fragile.getChargeableWeight(), 0.001, "Fragile chargeable weight is 3 kg");
            assertEquals(220.0, fragile.calculateBaseFee(), 0.001, "Fragile base fee = (40 * 3) + 100 = Php 220");
            assertEquals(330.0, fragile.getDeliveryFee(), 0.001, "Fragile delivery fee = 220 * 1.5 = Php 330.00");
        } catch (Exception e) {
            assertTrue(false, "FragileParcel prompt example failed: " + e.getMessage());
        }
    }

    private static void testFragileParcelValidations() {
        // Exceeding fragile max weight (30 kg)
        boolean caughtWeightExceeded = false;
        try {
            new FragileParcel("ABC0031", "Jack", 30.5, new StandardDeliveryStrategy(), 10, 10, 10, "Fragile item");
        } catch (InvalidParcelException e) {
            caughtWeightExceeded = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtWeightExceeded, "Fragile exceeding 30 kg throws InvalidParcelException");

        // Empty handling note
        boolean caughtEmptyNote = false;
        try {
            new FragileParcel("ABC0032", "Jack", 5.0, new StandardDeliveryStrategy(), 10, 10, 10, "   ");
        } catch (InvalidParcelException e) {
            caughtEmptyNote = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtEmptyNote, "Fragile with blank handling note throws InvalidParcelException");
    }

    private static void testStrategyPatternAndEligibility() {
        // Same Day allowed for <= 10 kg
        try {
            BoxParcel smallBox = new BoxParcel("ABC0040", "Karen", 4.0, new SameDayDeliveryStrategy(), 20, 20, 20);
            // Volumetric = 8000 / 3500 = 2.28 -> chargeable = 4 kg <= 10 kg
            assertEquals(4.0, smallBox.getChargeableWeight(), 0.001, "Small box chargeable weight");
            // Base fee = 40 * 4 = 160. Same day fee = (160 * 2) + 100 = 420.0
            assertEquals(420.0, smallBox.getDeliveryFee(), 0.001, "Small box same day delivery fee: (160 * 2) + 100 = 420.00");
        } catch (Exception e) {
            assertTrue(false, "Same Day delivery for eligible parcel threw exception: " + e.getMessage());
        }

        // Same Day NOT allowed for > 10 kg
        boolean caughtSameDayNotAllowed = false;
        String errorMessage = "";
        try {
            // Weight 12 kg -> chargeable weight 12 kg > 10 kg
            new BoxParcel("ABC0041", "Leo", 12.0, new SameDayDeliveryStrategy(), 20, 20, 20);
        } catch (DeliveryServiceNotAllowedException e) {
            caughtSameDayNotAllowed = true;
            errorMessage = e.getMessage();
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtSameDayNotAllowed, "Same Day with chargeable weight > 10 kg throws DeliveryServiceNotAllowedException");
        assertEquals("Same Day delivery is only for parcels up to 10 kg!", errorMessage, "Exact error message for Same Day violation");
    }

    private static void testParcelManagerOperations() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();
        assertTrue(manager.isEmpty(), "Manager is empty after clear");

        try {
            DocumentParcel doc = new DocumentParcel("ABC0050", "Mia", 0.4, new StandardDeliveryStrategy(), 15);
            manager.addParcel(doc);
            assertTrue(!manager.isEmpty(), "Manager is not empty after adding a parcel");
            assertTrue(manager.containsTrackingNumber("ABC0050"), "Manager contains ABC0050");
            assertTrue(manager.findParcel("ABC0050") == doc, "findParcel returns correct parcel");

            // Duplicate tracking number test
            boolean caughtDuplicate = false;
            try {
                DocumentParcel duplicate = new DocumentParcel("ABC0050", "Other", 0.2, new StandardDeliveryStrategy(), 5);
                manager.addParcel(duplicate);
            } catch (DuplicateTrackingNumberException e) {
                caughtDuplicate = true;
            }
            assertTrue(caughtDuplicate, "Adding duplicate tracking number throws DuplicateTrackingNumberException");
        } catch (Exception e) {
            assertTrue(false, "ParcelManager test failed: " + e.getMessage());
        }
    }

    private static void testChangeDeliveryServiceWorkflow() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        try {
            BoxParcel box = new BoxParcel("ABC0060", "Noah", 15.0, new StandardDeliveryStrategy(), 20, 20, 20);
            manager.addParcel(box);
            assertEquals(600.0, box.getDeliveryFee(), 0.001, "Initial standard fee: 40 * 15 = 600");

            // Change to Express (allowed)
            manager.changeDeliveryService("ABC0060", new ExpressDeliveryStrategy());
            assertEquals("Express", box.getDeliveryService().getName(), "Service changed to Express");
            assertEquals(900.0, box.getDeliveryFee(), 0.001, "New express fee: 600 * 1.5 = 900");

            // Change to Same Day (NOT allowed because chargeable weight is 15 kg > 10 kg)
            boolean caughtNotAllowed = false;
            try {
                manager.changeDeliveryService("ABC0060", new SameDayDeliveryStrategy());
            } catch (DeliveryServiceNotAllowedException e) {
                caughtNotAllowed = true;
            }
            assertTrue(caughtNotAllowed, "Changing to Same Day when weight > 10 kg throws DeliveryServiceNotAllowedException");
            // Parcel must keep its old service
            assertEquals("Express", box.getDeliveryService().getName(), "Parcel keeps previous service when Same Day rejected");
            assertEquals(900.0, box.getDeliveryFee(), 0.001, "Parcel keeps old fee");

            // Parcel not found test
            boolean caughtNotFound = false;
            try {
                manager.changeDeliveryService("ABC9999", new StandardDeliveryStrategy());
            } catch (ParcelNotFoundException e) {
                caughtNotFound = true;
                assertEquals("Parcel not found!", e.getMessage(), "Error message for parcel not found");
            }
            assertTrue(caughtNotFound, "Changing service on non-existent parcel throws ParcelNotFoundException");
        } catch (Exception e) {
            assertTrue(false, "Change delivery service test failed: " + e.getMessage());
        }
    }

    private static void testMarkAsDeliveredWorkflow() {
        ParcelManager manager = ParcelManager.getInstance();
        manager.clear();

        try {
            DocumentParcel doc = new DocumentParcel("ABC0070", "Olivia", 0.3, new StandardDeliveryStrategy(), 10);
            manager.addParcel(doc);
            assertEquals(ParcelStatus.PENDING, doc.getStatus(), "Initial status is Pending");

            // Mark as delivered
            manager.markAsDelivered("ABC0070");
            assertEquals(ParcelStatus.DELIVERED, doc.getStatus(), "Status changed to Delivered");

            // Mark as delivered again (already delivered)
            boolean caughtAlreadyDelivered = false;
            try {
                manager.markAsDelivered("ABC0070");
            } catch (ParcelAlreadyDeliveredException e) {
                caughtAlreadyDelivered = true;
                assertEquals("Parcel was already delivered.", e.getMessage(), "Exact message for already delivered");
            }
            assertTrue(caughtAlreadyDelivered, "Marking already delivered parcel throws ParcelAlreadyDeliveredException");

            // Change delivery service on delivered parcel
            boolean caughtChangeOnDelivered = false;
            try {
                manager.changeDeliveryService("ABC0070", new ExpressDeliveryStrategy());
            } catch (ParcelAlreadyDeliveredException e) {
                caughtChangeOnDelivered = true;
                assertEquals("Parcel already delivered!", e.getMessage(), "Exact message for change service on delivered");
            }
            assertTrue(caughtChangeOnDelivered, "Changing service on delivered parcel throws ParcelAlreadyDeliveredException");
        } catch (Exception e) {
            assertTrue(false, "Mark as delivered test failed: " + e.getMessage());
        }
    }

    private static void testEdgeCasesAndInputHandling() {
        // 1. NaN weight validation
        boolean caughtNaN = false;
        try {
            new DocumentParcel("ABC0080", "Tester", Double.NaN, new StandardDeliveryStrategy(), 10);
        } catch (InvalidParcelException e) {
            caughtNaN = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtNaN, "Double.NaN weight throws InvalidParcelException");

        // 2. Infinity weight validation
        boolean caughtInfinity = false;
        try {
            new BoxParcel("ABC0081", "Tester", Double.POSITIVE_INFINITY, new StandardDeliveryStrategy(), 10, 10, 10);
        } catch (InvalidParcelException e) {
            caughtInfinity = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtInfinity, "Double.POSITIVE_INFINITY weight throws InvalidParcelException");

        // 3. Lowercase and whitespace tracking number normalization
        try {
            DocumentParcel doc = new DocumentParcel("  abc0082  ", "Tester", 0.4, new StandardDeliveryStrategy(), 10);
            assertEquals("ABC0082", doc.getTrackingNumber(), "Tracking number trimmed and uppercase normalized");
        } catch (Exception e) {
            assertTrue(false, "Tracking normalization failed: " + e.getMessage());
        }

        // 4. Large dimensions that would overflow int multiplication
        try {
            BoxParcel largeBox = new BoxParcel("ABC0083", "Tester", 5.0, new StandardDeliveryStrategy(), 2000, 2000, 100);
            // 2000 * 2000 * 100 = 400,000,000 / 3500 = 114285.71
            assertTrue(largeBox.getVolumetricWeight() > 0, "Large dimensions do not overflow int");
        } catch (Exception e) {
            assertTrue(false, "Large box failed: " + e.getMessage());
        }

        // 5. Floating point precision near integer boundary
        try {
            // Actual weight = 2.0. Volumetric weight = 7000 / 3500 = 2.0.
            BoxParcel boxPrecision = new BoxParcel("ABC0084", "Tester", 2.0, new StandardDeliveryStrategy(), 70, 50, 2);
            assertEquals(2.0, boxPrecision.getChargeableWeight(), 0.001, "Exact 2.0 kg rounds to 2, not 3");
        } catch (Exception e) {
            assertTrue(false, "Precision test failed: " + e.getMessage());
        }
    }

    private static void testNewInputValidations() {
        // 1. Receiver name exceeding 50 characters
        boolean caughtLongName = false;
        try {
            String longName = "A".repeat(51);
            new DocumentParcel("ABC0090", longName, 0.4, new StandardDeliveryStrategy(), 10);
        } catch (InvalidParcelException e) {
            caughtLongName = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtLongName, "Receiver name exceeding 50 chars throws InvalidParcelException");

        // 2. Receiver name without letters (purely numeric or symbols)
        boolean caughtNoLettersName = false;
        try {
            new DocumentParcel("ABC0091", "123456", 0.4, new StandardDeliveryStrategy(), 10);
        } catch (InvalidParcelException e) {
            caughtNoLettersName = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtNoLettersName, "Receiver name without letters throws InvalidParcelException");

        // 3. BoxParcel dimension exceeding MAX_DIMENSION (3000 cm)
        boolean caughtExcessDimension = false;
        try {
            new BoxParcel("ABC0092", "Tester", 5.0, new StandardDeliveryStrategy(), 3001, 10, 10);
        } catch (InvalidParcelException e) {
            caughtExcessDimension = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtExcessDimension, "BoxParcel dimension exceeding 3000 cm throws InvalidParcelException");

        // 4. FragileParcel handling note exceeding 100 characters
        boolean caughtLongNote = false;
        try {
            String longNote = "X".repeat(101);
            new FragileParcel("ABC0093", "Tester", 5.0, new StandardDeliveryStrategy(), 10, 10, 10, longNote);
        } catch (InvalidParcelException e) {
            caughtLongNote = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtLongNote, "FragileParcel note exceeding 100 characters throws InvalidParcelException");

        // 5. Parcel.setStatus null validation
        boolean caughtNullStatus = false;
        try {
            DocumentParcel doc = new DocumentParcel("ABC0094", "Tester", 0.4, new StandardDeliveryStrategy(), 10);
            doc.setStatus(null);
        } catch (IllegalArgumentException e) {
            caughtNullStatus = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtNullStatus, "Parcel.setStatus(null) throws IllegalArgumentException");

        // 6. Parcel.setDeliveryService on already delivered parcel
        boolean caughtSetServiceOnDelivered = false;
        try {
            DocumentParcel doc = new DocumentParcel("ABC0095", "Tester", 0.4, new StandardDeliveryStrategy(), 10);
            doc.markAsDelivered();
            doc.setDeliveryService(new ExpressDeliveryStrategy());
        } catch (ParcelAlreadyDeliveredException e) {
            caughtSetServiceOnDelivered = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtSetServiceOnDelivered, "Parcel.setDeliveryService on delivered parcel throws ParcelAlreadyDeliveredException");

        // 7. ParcelManager.changeDeliveryService null service check
        boolean caughtNullService = false;
        try {
            ParcelManager manager = ParcelManager.getInstance();
            manager.changeDeliveryService("ABC0050", null);
        } catch (IllegalArgumentException e) {
            caughtNullService = true;
        } catch (Exception e) {
            // unexpected
        }
        assertTrue(caughtNullService, "ParcelManager.changeDeliveryService with null service throws IllegalArgumentException");
    }
}
