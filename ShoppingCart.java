import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/*
 * ONLINE SHOPPING CART (Console based)
 * ------------------------------------
 * The store already has products in 5 categories:
 *     Makeup, Tech, Home Decor, Stationery, Personal Care
 * The CUSTOMER can:
 *     1) Browse products by category      2) See all products
 *     3) Add a product to the cart        4) View the cart
 *     5) Remove a product                 6) Apply a discount coupon
 *     7) Checkout (enter name, phone, house no, address, city, pincode)
 *        -> shows the expected DELIVERY DATE and the coupon applied
 *     8) Track the order (status + delivery date + coupon used)
 *     9) Exit
 *
 * Syllabus topics used (look for the "MODULE" comments):
 *  Module 1 : variables, constants, operators, ternary, Scanner, printf
 *  Module 2 : if / else-if ladder, switch, while, do-while, for, break
 *  Module 3 : methods, overloading, recursion, 1D and 2D arrays, for-each
 *  Module 4 : classes, objects, constructors, this, encapsulation, static,
 *             toString(), inheritance, interface, overriding, exceptions
 *  Module 5 : Strings, StringBuilder, split/join/trim, equals vs ==,
 *             Arrays.sort, Comparable, file read/write with Files and Path
 *
 * How to run:   javac ShoppingCart.java
 *               java ShoppingCart
 */

// ====================================================================
// MODULE 4 : INTERFACE (a "promise": every class that implements it
//            MUST write the calculateDiscount method)
// ====================================================================
interface Discountable {
    double calculateDiscount(double amount);
}

// ====================================================================
// MODULE 4 : CLASS Product (blueprint for a product)
//   - private fields + getters = ENCAPSULATION
//   - implements Comparable    = lets Arrays.sort() sort products by price
// ====================================================================
class Product implements Comparable<Product> {
    private int id;
    private String category;
    private String name;
    private double price;

    // Constructor: runs when we write  new Product(...)
    public Product(int id, String category, String name, double price) {
        this.id = id;              // "this.id" is the field, "id" is the parameter
        this.category = category;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    // Sorting rule: the cheaper product comes first
    @Override
    public int compareTo(Product other) {
        return Double.compare(this.price, other.price);
    }

    // toString() decides how the object looks when printed
    @Override
    public String toString() {
        return String.format("%-4d %-24s Rs.%9.2f", id, name, price);
    }
}

// ====================================================================
// CartItem : one line in the cart (a product + how many we want)
// ====================================================================
class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void addQuantity(int extra) {
        quantity = quantity + extra;
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("%-4d %-24s Rs.%9.2f x %-3d = Rs.%10.2f",
                product.getId(), product.getName(), product.getPrice(), quantity, getTotal());
    }
}

// ====================================================================
// MODULE 4 : INHERITANCE
//   Coupon is the PARENT class. PercentCoupon and FlatCoupon are CHILD
//   classes ("is-a": a PercentCoupon IS A Coupon). The children OVERRIDE
//   calculateDiscount() with their own formula.
// ====================================================================
class Coupon implements Discountable {
    protected String code;
    protected double minOrder;   // minimum cart value needed to use the coupon

    public Coupon(String code, double minOrder) {
        this.code = code;
        this.minOrder = minOrder;
    }

    public String getCode() { return code; }
    public double getMinOrder() { return minOrder; }

    @Override
    public double calculateDiscount(double amount) {
        return 0;   // a plain coupon gives nothing; the children change this
    }
}

class PercentCoupon extends Coupon {
    private double percent;

    public PercentCoupon(String code, double minOrder, double percent) {
        super(code, minOrder);      // call the parent's constructor
        this.percent = percent;
    }

    @Override
    public double calculateDiscount(double amount) {
        return amount * percent / 100;   // e.g. 10% of 1000 = 100
    }

    @Override
    public String toString() {
        return code + " - " + percent + "% off on orders of Rs." + minOrder + " or more";
    }
}

class FlatCoupon extends Coupon {
    private double flatAmount;

    public FlatCoupon(String code, double minOrder, double flatAmount) {
        super(code, minOrder);
        this.flatAmount = flatAmount;
    }

    @Override
    public double calculateDiscount(double amount) {
        // Ternary operator (Module 1): the discount can never be bigger than the bill
        return (flatAmount > amount) ? amount : flatAmount;
    }

    @Override
    public String toString() {
        return code + " - Rs." + flatAmount + " off on orders of Rs." + minOrder + " or more";
    }
}

// ====================================================================
// MODULE 4 : OUR OWN CHECKED EXCEPTION
//   Thrown when a coupon is wrong. Java forces us to handle it (try-catch).
// ====================================================================
class InvalidCouponException extends Exception {
    public InvalidCouponException(String message) {
        super(message);
    }
}

// ====================================================================
// Customer : stores where the parcel must be delivered
// ====================================================================
class Customer {
    private String name;
    private String phone;
    private String houseNo;
    private String address;     // street and area
    private String city;
    private String pincode;

    public Customer(String name, String phone, String houseNo,
                    String address, String city, String pincode) {
        this.name = name;
        this.phone = phone;
        this.houseNo = houseNo;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getHouseNo() { return houseNo; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getPincode() { return pincode; }

    @Override
    public String toString() {
        return name + " (Ph: " + phone + ")\n   House No: " + houseNo + ", " + address
                + ", " + city + " - " + pincode;
    }
}

// ====================================================================
// MAIN CLASS : the program starts here (main method)
// ====================================================================
public class ShoppingCart {

    // ---------- MODULE 1 : constants (final = the value never changes) ----------
    static final String ORDER_FILE = "orders.txt";
    static final double DELIVERY_CHARGE = 40.0;
    static final double FREE_DELIVERY_ABOVE = 500.0;
    static final int DELIVERY_DAYS = 5;     // parcel reaches the customer in 5 days

    // MODULE 3 : 1D array of Strings
    static final String[] CATEGORIES = {"Makeup", "Tech", "Home Decor", "Stationery", "Personal Care"};

    // The parcel journey (stage 0 to 4)
    static final String[] STAGES = {"Order Placed", "Packed", "Shipped", "Out for Delivery", "Delivered"};

    // MODULE 3 : 2D array - every row is one product: { category, name, price }
    static final String[][] PRODUCT_DATA = {
            // ----- Makeup -----
            {"Makeup", "Matte Lipstick", "349"},
            {"Makeup", "Kajal Eye Pencil", "199"},
            {"Makeup", "Liquid Foundation", "599"},
            {"Makeup", "Compact Powder", "299"},
            {"Makeup", "Waterproof Mascara", "399"},
            {"Makeup", "Nail Polish Set", "249"},
            // ----- Tech -----
            {"Tech", "Wireless Mouse", "499"},
            {"Tech", "Bluetooth Earbuds", "1299"},
            {"Tech", "USB Pen Drive 32GB", "350"},
            {"Tech", "Power Bank 10000mAh", "999"},
            {"Tech", "Mobile Phone Stand", "199"},
            // ----- Home Decor -----
            {"Home Decor", "Scented Candle Set", "449"},
            {"Home Decor", "Wall Photo Frame", "299"},
            {"Home Decor", "Table Lamp", "899"},
            {"Home Decor", "Artificial Plant", "349"},
            {"Home Decor", "Cushion Cover (Pair)", "399"},
            // ----- Stationery -----
            {"Stationery", "Notebook (Pack of 5)", "250"},
            {"Stationery", "Gel Pen Set", "120"},
            {"Stationery", "Highlighter Set", "150"},
            {"Stationery", "Sticky Notes", "99"},
            {"Stationery", "Desk Organizer", "349"},
            // ----- Personal Care -----
            {"Personal Care", "Face Wash", "199"},
            {"Personal Care", "Body Lotion", "299"},
            {"Personal Care", "Perfume", "799"},
            {"Personal Care", "Hair Brush", "149"}
    };

    static ArrayList<Product> catalog = new ArrayList<>();   // filled by loadProducts()
    static ArrayList<Coupon> coupons = new ArrayList<>();    // filled by loadCoupons()
    static ArrayList<CartItem> cart = new ArrayList<>();     // the shopping cart
    static Coupon appliedCoupon = null;                      // null = no coupon applied yet

    static Scanner sc = new Scanner(System.in);              // MODULE 1 : reads keyboard input

    // Date format used when we SHOW a date, example: 20 Oct 2026
    static DateTimeFormatter showFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ================================================================
    // main : load the store, then show the menu until the user chooses Exit
    // ================================================================
    public static void main(String[] args) {
        loadProducts();
        loadCoupons();

        printLine("WELCOME TO JAVA ONLINE SHOP");

        int choice;
        // MODULE 2 : do-while runs at least once
        do {
            showMenu();
            choice = readInt("Enter your choice: ");

            // MODULE 2 : switch statement
            switch (choice) {
                case 1:
                    browseByCategory();
                    break;
                case 2:
                    showAllProducts();
                    break;
                case 3:
                    addToCart();
                    break;
                case 4:
                    viewCart();
                    break;
                case 5:
                    removeFromCart();
                    break;
                case 6:
                    applyCoupon();
                    break;
                case 7:
                    checkout();
                    break;
                case 8:
                    trackOrder();
                    break;
                case 9:
                    System.out.println("Thank you for shopping with us. Bye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1 to 9.");
            }
        } while (choice != 9);
    }

    static void showMenu() {
        printLine();
        System.out.println("1. Browse products by category");
        System.out.println("2. Show ALL products");
        System.out.println("3. Add product to cart");
        System.out.println("4. View cart");
        System.out.println("5. Remove product from cart");
        System.out.println("6. Apply discount coupon");
        System.out.println("7. Checkout (enter delivery details)");
        System.out.println("8. Track my order");
        System.out.println("9. Exit");
        printLine();
    }

    // ================================================================
    // STORE SETUP : products and coupons are already inside the code
    // ================================================================

    // Reads every row of the 2D array and creates a Product object from it
    static void loadProducts() {
        for (int i = 0; i < PRODUCT_DATA.length; i++) {
            String[] row = PRODUCT_DATA[i];
            int id = i + 1;                          // ids are 1, 2, 3 ...
            String category = row[0];
            String name = row[1];
            double price = Double.parseDouble(row[2]);   // text "349" -> number 349.0
            catalog.add(new Product(id, category, name, price));
        }
    }

    static void loadCoupons() {
        coupons.add(new PercentCoupon("WELCOME5", 0, 5));
        coupons.add(new PercentCoupon("SAVE10", 500, 10));
        coupons.add(new FlatCoupon("FLAT100", 1000, 100));
        coupons.add(new PercentCoupon("BIG20", 2500, 20));
    }

    // ================================================================
    // MODULE 3 : METHOD OVERLOADING
    //   Two methods with the same name but different parameters.
    // ================================================================
    static void printLine() {
        System.out.println("--------------------------------------------------");
    }

    static void printLine(String title) {
        printLine();
        System.out.println("  " + title);
        printLine();
    }

    // ================================================================
    // INPUT HELPERS
    //   Scanner reads text. We read a whole line and convert it, so that
    //   a wrong input (like "abc" instead of a number) does not crash us.
    // ================================================================
    static int readInt(String message) {
        while (true) {                       // keep asking until the number is valid
            System.out.print(message);
            String text = sc.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {   // unchecked exception (Module 4)
                System.out.println("Please type a whole number.");
            }
        }
    }

    static String readText(String message) {
        String text;
        do {
            System.out.print(message);
            text = sc.nextLine().trim();             // trim() removes extra spaces
            if (text.isEmpty()) {
                System.out.println("This field cannot be empty.");
            }
        } while (text.isEmpty());
        return text;
    }

    // The '|' sign separates the fields in our file, so remove it from typed text
    static String clean(String text) {
        return text.replace("|", " ");
    }

    // ================================================================
    // 1. BROWSE BY CATEGORY
    // ================================================================
    static void browseByCategory() {
        printLine("CATEGORIES");
        for (int i = 0; i < CATEGORIES.length; i++) {
            System.out.println((i + 1) + ". " + CATEGORIES[i]);
        }
        int pick = readInt("Choose a category (0 to cancel): ");

        if (pick == 0) {
            return;
        }
        if (pick < 1 || pick > CATEGORIES.length) {
            System.out.println("Invalid category.");
            return;
        }

        String category = CATEGORIES[pick - 1];

        System.out.println("1. Normal order");
        System.out.println("2. Price low to high");
        int option = readInt("How do you want to see the products? ");

        // Collect only the products of this category
        ArrayList<Product> selected = new ArrayList<>();
        for (Product p : catalog) {
            // MODULE 5 : equals compares String VALUES
            if (p.getCategory().equals(category)) {
                selected.add(p);
            }
        }

        // MODULE 3 : copy the list into an ARRAY so we can sort it
        Product[] list = selected.toArray(new Product[0]);
        if (option == 2) {
            // MODULE 5 : Arrays.sort uses Product.compareTo() (Comparable)
            Arrays.sort(list);
        }

        printLine(category.toUpperCase());
        printTableHeader();
        for (Product p : list) {
            System.out.println(p);     // toString() is called automatically
        }
        System.out.println("\nTo buy, choose option 3 in the main menu and enter the product ID.");
    }

    // ================================================================
    // 2. SHOW ALL PRODUCTS (grouped by category)
    // ================================================================
    static void showAllProducts() {
        // MODULE 2 : nested loops - outer loop = categories, inner loop = products
        for (String category : CATEGORIES) {
            printLine(category.toUpperCase());
            printTableHeader();
            for (Product p : catalog) {
                if (p.getCategory().equals(category)) {
                    System.out.println(p);
                }
            }
        }
    }

    static void printTableHeader() {
        System.out.printf("%-4s %-24s %12s%n", "ID", "Name", "Price");
    }

    // ================================================================
    // 3. ADD TO CART
    // ================================================================
    static void addToCart() {
        int id = readInt("Enter product ID to add (0 to cancel, see IDs in options 1 or 2): ");
        if (id == 0) {
            return;
        }

        Product chosen = findProduct(id);
        if (chosen == null) {
            System.out.println("No product with that ID.");
            return;
        }

        int qty = readInt("Enter quantity for " + chosen.getName() + ": ");
        if (qty <= 0) {
            System.out.println("Quantity must be at least 1.");
            return;
        }

        // If the product is already in the cart, only increase its quantity
        for (CartItem item : cart) {
            if (item.getProduct().getId() == id) {
                item.addQuantity(qty);
                System.out.println("Quantity updated for " + chosen.getName());
                return;
            }
        }

        cart.add(new CartItem(chosen, qty));     // create the object with "new"
        System.out.println(chosen.getName() + " added to cart.");
    }

    // Linear search (Module 3): check products one by one
    static Product findProduct(int id) {
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).getId() == id) {
                return catalog.get(i);
            }
        }
        return null;     // not found
    }

    // ================================================================
    // 4. VIEW CART
    // ================================================================
    static void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        printLine("YOUR CART");
        for (CartItem item : cart) {
            System.out.println(item);
        }
        printLine();
        printBill();
    }

    // ================================================================
    // 5. REMOVE FROM CART
    // ================================================================
    static void removeFromCart() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }

        for (CartItem item : cart) {
            System.out.println(item);
        }
        int id = readInt("Enter product ID to remove (0 to cancel): ");

        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct().getId() == id) {
                cart.remove(i);
                System.out.println("Item removed.");
                // a smaller cart may not qualify for the coupon any more
                if (appliedCoupon != null && getSubtotal() < appliedCoupon.getMinOrder()) {
                    System.out.println("Coupon " + appliedCoupon.getCode()
                            + " was removed because the cart value is now too low.");
                    appliedCoupon = null;
                }
                return;
            }
        }
        System.out.println("That product is not in your cart.");
    }

    // ================================================================
    // MONEY CALCULATIONS
    // ================================================================

    // MODULE 3 : RECURSION
    //   Adds the totals of cart items from position 'index' to the end.
    //   Base case      : index reached the end -> nothing left to add -> 0
    //   Recursive case : this item's total + total of the remaining items
    static double sumItems(int index) {
        if (index == cart.size()) {          // base case
            return 0;
        }
        return cart.get(index).getTotal() + sumItems(index + 1);   // recursive case
    }

    static double getSubtotal() {
        return sumItems(0);
    }

    static double getDiscount() {
        if (appliedCoupon == null) {
            return 0;
        }
        return appliedCoupon.calculateDiscount(getSubtotal());
    }

    static double getDeliveryCharge() {
        double afterDiscount = getSubtotal() - getDiscount();
        // MODULE 1 : ternary operator  (condition ? valueIfTrue : valueIfFalse)
        return (afterDiscount >= FREE_DELIVERY_ABOVE) ? 0 : DELIVERY_CHARGE;
    }

    static double getGrandTotal() {
        return getSubtotal() - getDiscount() + getDeliveryCharge();
    }

    static void printBill() {
        System.out.printf("Subtotal         : Rs.%10.2f%n", getSubtotal());
        if (appliedCoupon != null) {
            System.out.println("Coupon applied   : " + appliedCoupon.getCode());
            System.out.printf("Discount         : -Rs.%9.2f%n", getDiscount());
        } else {
            System.out.println("Coupon applied   : None");
        }
        String note = (getDeliveryCharge() == 0) ? " (FREE)" : "";
        System.out.printf("Delivery charge  : Rs.%10.2f%s%n", getDeliveryCharge(), note);
        System.out.printf("GRAND TOTAL      : Rs.%10.2f%n", getGrandTotal());
    }

    // ================================================================
    // 6. APPLY COUPON (uses our own exception)
    // ================================================================
    static void applyCoupon() {
        if (cart.isEmpty()) {
            System.out.println("Add some products before applying a coupon.");
            return;
        }

        printLine("AVAILABLE COUPONS");
        for (Coupon c : coupons) {
            System.out.println("  " + c);     // toString() of the child class
        }

        String code = readText("Enter coupon code: ");

        try {
            Coupon coupon = findCoupon(code);        // may throw InvalidCouponException
            double subtotal = getSubtotal();

            if (subtotal < coupon.getMinOrder()) {
                throw new InvalidCouponException(
                        "Cart value must be at least Rs." + coupon.getMinOrder() + " for this coupon.");
            }

            appliedCoupon = coupon;
            System.out.println("Coupon applied: " + coupon.getCode());
            System.out.printf("You saved Rs.%.2f%n", getDiscount());

        } catch (InvalidCouponException e) {
            System.out.println("Coupon error: " + e.getMessage());
        }
    }

    // Looks for the code in the coupon list
    static Coupon findCoupon(String code) throws InvalidCouponException {
        for (Coupon c : coupons) {
            // MODULE 5 : equalsIgnoreCase compares the VALUES of two Strings
            // (== would only check if they are the same object - not what we want)
            if (c.getCode().equalsIgnoreCase(code)) {
                return c;
            }
        }
        throw new InvalidCouponException("'" + code + "' is not a valid coupon code.");
    }

    // ================================================================
    // 7. CHECKOUT : customer details + delivery address + delivery date
    // ================================================================
    static void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty. Add products first.");
            return;
        }

        printLine("CHECKOUT - DELIVERY DETAILS");
        Customer customer = readCustomerDetails();

        // LocalDate.now() = today's date. plusDays() adds days to it.
        LocalDate orderDate = LocalDate.now();
        LocalDate deliveryDate = orderDate.plusDays(DELIVERY_DAYS);

        // Show a summary and ask for confirmation
        printLine("ORDER SUMMARY");
        for (CartItem item : cart) {
            System.out.println(item);
        }
        printLine();
        printBill();
        printLine();
        System.out.println("Parcel will be delivered to:");
        System.out.println("   " + customer);
        System.out.println("Expected delivery date: " + deliveryDate.format(showFormat));

        String answer = readText("Confirm order? (yes/no): ");
        if (!answer.equalsIgnoreCase("yes")) {
            System.out.println("Order cancelled. Your cart is still saved.");
            return;
        }

        String trackingId = generateTrackingId();
        saveOrder(trackingId, customer, orderDate, deliveryDate);

        printLine("ORDER PLACED SUCCESSFULLY!");
        System.out.println("Tracking ID            : " + trackingId);
        System.out.println("Expected delivery date : " + deliveryDate.format(showFormat));
        System.out.println("Keep the tracking ID safe - you need it to track your order.");

        // Empty the cart for the next order
        cart.clear();
        appliedCoupon = null;
    }

    // Reads and VALIDATES customer details (Module 5 : string validation)
    static Customer readCustomerDetails() {
        String name = clean(readText("Full name                     : "));

        String phone;
        while (true) {
            phone = readText("Phone (10 digits)             : ");
            if (isAllDigits(phone) && phone.length() == 10) {
                break;                         // valid -> leave the loop
            }
            System.out.println("Phone number must be exactly 10 digits.");
        }

        String houseNo = clean(readText("House / Flat no              : "));
        String address = clean(readText("Street and area              : "));
        String city = clean(readText("City                          : "));

        String pincode;
        while (true) {
            pincode = readText("Pincode (6 digits)           : ");
            if (isAllDigits(pincode) && pincode.length() == 6) {
                break;
            }
            System.out.println("Pincode must be exactly 6 digits.");
        }

        return new Customer(name, phone, houseNo, address, city, pincode);
    }

    // Checks every character of the text one by one
    static boolean isAllDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static String generateTrackingId() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);   // a 6-digit number
        return "TRK" + number;
    }

    // ================================================================
    // MODULE 5 : FILE WRITING
    //   Each order is saved as one line, fields separated by '|'
    //   Field numbers:
    //   0 trackingId | 1 name | 2 phone | 3 houseNo | 4 address | 5 city | 6 pincode
    //   7 items | 8 couponCode | 9 discount | 10 total | 11 orderDate | 12 deliveryDate
    // ================================================================
    static void saveOrder(String trackingId, Customer c, LocalDate orderDate, LocalDate deliveryDate) {
        // StringBuilder = efficient way to build a long String piece by piece
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            items.append(item.getProduct().getName()).append(" x").append(item.getQuantity());
            if (i < cart.size() - 1) {
                items.append(", ");
            }
        }

        // Ternary operator: if no coupon was used, store the word NONE
        String couponCode = (appliedCoupon == null) ? "NONE" : appliedCoupon.getCode();

        // String.join puts '|' between all the pieces
        String line = String.join("|",
                trackingId, c.getName(), c.getPhone(), c.getHouseNo(), c.getAddress(),
                c.getCity(), c.getPincode(), items.toString(),
                couponCode,
                String.format("%.2f", getDiscount()),
                String.format("%.2f", getGrandTotal()),
                orderDate.toString(), deliveryDate.toString());

        try {
            Path path = Path.of(ORDER_FILE);
            // CREATE = make the file if missing, APPEND = add at the end (keep old orders)
            Files.writeString(path, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {          // checked exception - must be handled
            System.out.println("Warning: could not save the order file: " + e.getMessage());
        }
    }

    // ================================================================
    // 8. TRACK ORDER
    //   MODULE 5 : read the file line by line, split each line, compare tracking ID
    // ================================================================
    static void trackOrder() {
        String id = readText("Enter your tracking ID: ").toUpperCase();

        Path path = Path.of(ORDER_FILE);
        if (!Files.exists(path)) {
            System.out.println("No orders have been placed yet.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path);   // read the whole file

            for (String line : lines) {
                String[] parts = line.split("\\|");           // cut the line at every '|'
                if (parts.length < 13) {
                    continue;                                  // skip broken lines
                }

                if (parts[0].equals(id)) {
                    printTrackingDetails(parts);
                    return;                                    // found -> stop searching
                }
            }
            System.out.println("No order found with tracking ID " + id);

        } catch (IOException e) {
            System.out.println("Could not read the order file: " + e.getMessage());
        }
    }

    static void printTrackingDetails(String[] p) {
        // Turn the saved text back into dates
        LocalDate orderDate = LocalDate.parse(p[11]);
        LocalDate deliveryDate = LocalDate.parse(p[12]);

        printLine("ORDER TRACKING : " + p[0]);
        System.out.println("Customer          : " + p[1] + "  (Ph: " + p[2] + ")");
        System.out.println("Deliver to        : House No " + p[3] + ", " + p[4] + ", " + p[5] + " - " + p[6]);
        System.out.println("Items             : " + p[7]);
        System.out.println("Coupon applied    : " + p[8] + (p[8].equals("NONE") ? "" : "  (saved Rs." + p[9] + ")"));
        System.out.println("Amount paid       : Rs." + p[10]);
        System.out.println("Order date        : " + orderDate.format(showFormat));
        System.out.println("Delivery date     : " + deliveryDate.format(showFormat));
        printLine();

        int stage = findStage(orderDate, deliveryDate);

        for (int i = 0; i < STAGES.length; i++) {
            String mark = (i <= stage) ? "[X] " : "[ ] ";
            System.out.println(mark + STAGES[i]);
        }
        System.out.println("\nCurrent status: " + STAGES[stage]);
    }

    // ================================================================
    // The status changes by itself as the days pass:
    //   delivery date reached     -> Delivered
    //   one day before delivery   -> Out for Delivery
    //   2 or more days after order-> Shipped
    //   1 day after order         -> Packed
    //   same day as order         -> Order Placed
    // MODULE 2 : if - else if ladder
    // ================================================================
    static int findStage(LocalDate orderDate, LocalDate deliveryDate) {
        LocalDate today = LocalDate.now();

        if (!today.isBefore(deliveryDate)) {                  // today is delivery day or later
            return 4;
        } else if (!today.isBefore(deliveryDate.minusDays(1))) {
            return 3;
        } else if (!today.isBefore(orderDate.plusDays(2))) {
            return 2;
        } else if (!today.isBefore(orderDate.plusDays(1))) {
            return 1;
        } else {
            return 0;
        }
    }
}