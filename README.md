# Parcel Delivery System (Java)

A complete, object-oriented Parcel Delivery System implemented in Java. The project strictly adheres to OOP principles and demonstrates **Polymorphism**, **Exception Handling**, and the **Singleton** and **Strategy** design patterns.

---

## 🏛️ Architecture & Design Patterns

### 1. Polymorphism & Inheritance
- **`Parcel` (abstract base class)**:
  - Defines common fields: `trackingNumber`, `receiverName`, `weight`, `deliveryService`, and `status`.
  - Defines polymorphic abstract methods:
    - `double getChargeableWeight()`
    - `double calculateBaseFee()`
    - `String getParcelType()`
  - Encapsulates common logic such as `getDeliveryFee()`, which delegates calculation to the delivery service strategy.
- **`DocumentParcel` (extends `Parcel`)**:
  - Maximum weight: `0.5 kg`
  - Field: `numberOfPages` (1 to 100)
  - `chargeableWeight = actual weight`
  - `baseFee = Php 50.00` flat
- **`BoxParcel` (extends `Parcel`)**:
  - Maximum weight: `50 kg`
  - Fields: `length`, `width`, `height` in cm (> 0)
  - `volumetricWeight = (length * width * height) / 3500.0`
  - `chargeableWeight = ceil(max(actual weight, volumetric weight))`
  - `baseFee = Php 40.00 * chargeableWeight`
- **`FragileParcel` (extends `BoxParcel`)**:
  - Maximum weight: `30 kg`
  - Field: `handlingNote` (non-empty string)
  - `chargeableWeight = same rule as Box`
  - `baseFee = Box base fee + Php 100.00 handling fee`

### 2. Strategy Design Pattern
- **`DeliveryServiceStrategy` (interface)**:
  - Declares `getName()`, `validateEligibility(double chargeableWeight)`, and `calculateFee(double baseFee, double chargeableWeight)`.
- **Implementations**:
  - **`StandardDeliveryStrategy`**: Fee = Base fee.
  - **`ExpressDeliveryStrategy`**: Fee = Base fee × 1.5.
  - **`SameDayDeliveryStrategy`**: Fee = (Base fee × 2) + Php 100.00.
    - Restricted to parcels with chargeable weight $\le 10\text{ kg}$.
    - Throws `DeliveryServiceNotAllowedException` with message `"Same Day delivery is only for parcels up to 10 kg!"` if weight exceeds 10 kg.

### 3. Singleton Design Pattern
- **`ParcelManager`**:
  - Manages the central repository of parcels.
  - Features private constructor and `public static synchronized ParcelManager getInstance()`.
  - Ensures unique tracking numbers, facilitates lookups, and safely mutates parcel delivery status and services.

### 4. Custom Exception Handling
- **`InvalidParcelException`**: Thrown when validation rules fail (format of tracking number, empty receiver name, invalid weights, invalid dimensions, page counts, or blank handling notes).
- **`DuplicateTrackingNumberException`**: Thrown when attempting to add a parcel with an existing tracking number.
- **`DeliveryServiceNotAllowedException`**: Thrown when a parcel is not eligible for Same Day delivery.
- **`ParcelNotFoundException`**: Thrown when a tracking number is not found.
- **`ParcelAlreadyDeliveredException`**: Thrown when attempting to modify or re-deliver a parcel that has already been delivered.

---

## 📁 File Structure

```
oop4/
├── ParcelStatus.java                    // Enum: PENDING, DELIVERED
├── DeliveryServiceStrategy.java        // Strategy interface
├── StandardDeliveryStrategy.java       // Concrete strategy: Standard
├── ExpressDeliveryStrategy.java        // Concrete strategy: Express
├── SameDayDeliveryStrategy.java        // Concrete strategy: Same Day
├── Parcel.java                         // Abstract base class
├── DocumentParcel.java                 // Document parcel subclass
├── BoxParcel.java                      // Box parcel subclass
├── FragileParcel.java                  // Fragile parcel (extends BoxParcel)
├── ParcelManager.java                  // Singleton manager & repository
├── InvalidParcelException.java         // Custom exception
├── DuplicateTrackingNumberException.java// Custom exception
├── DeliveryServiceNotAllowedException.java// Custom exception
├── ParcelNotFoundException.java        // Custom exception
├── ParcelAlreadyDeliveredException.java// Custom exception
├── Main.java                           // Interactive CLI application
├── ParcelSystemTest.java               // Automated test suite (51 assertions)
└── README.md                           // Documentation
```

---

## 🚀 How to Compile and Run

### 1. Compile All Files
```bash
javac *.java
```

### 2. Run the Main Application
```bash
java Main
```

### 3. Run the Automated Test Suite
```bash
java ParcelSystemTest
```

---

## 💡 Example Calculation Verification

As specified in the prompt:
- **Parcel Type**: FRAGILE
- **Dimensions**: $40 \times 20 \times 10\text{ cm}$
- **Actual Weight**: $1.8\text{ kg}$
- **Delivery Service**: EXPRESS

1. $\text{Volumetric Weight} = \frac{40 \times 20 \times 10}{3500} = \frac{8000}{3500} \approx 2.29\text{ kg}$
2. $\text{Chargeable Weight} = \lceil\max(1.8, 2.29)\rceil = \lceil 2.29 \rceil = 3\text{ kg}$
3. $\text{Base Fee} = (\text{Php } 40 \times 3) + \text{Php } 100 = \text{Php } 120 + 100 = \text{Php } 220.00$
4. $\text{Delivery Fee} = \text{Php } 220 \times 1.5 = \text{Php } 330.00$
