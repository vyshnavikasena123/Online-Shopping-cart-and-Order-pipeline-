import java.util.Scanner;

public class OnlineShoppingcart {

    static final double TAX = 0.18;

    // Display products
    static void displayProducts(String[] products, double[] prices) {

        System.out.println("===== PRODUCTS =====");

        for (int i = 0; i < products.length; i++) {

            System.out.println(
                (i + 1) + ". " +
                products[i] +
                " - Rs. " +
                prices[i]
            );
        }
    }

    
    static void addProduct(int[] cart, int productNumber, int quantity) {

        cart[productNumber] =
            cart[productNumber] + quantity;

        System.out.println("Product added to cart!");
    }

    
    static double calculateTotal(double[] prices, int[] cart) {

        double total = 0;

        for (int i = 0; i < prices.length; i++) {

            total = total +
                    prices[i] * cart[i];
        }

        return total;
    }

    
    static double calculateTotal(double price, int quantity) {

        return price * quantity;
    }

    // Search product
    static int searchProduct(String[] products, String name) {

        for (int i = 0; i < products.length; i++) {

            if (products[i].equalsIgnoreCase(name)) {

                return i;
            }
        }

        return -1;
    }

    // Calculate discount
    static double calculateDiscount(double total) {

        if (total >= 5000) {

            return total * 0.20;

        } else if (total >= 3000) {

            return total * 0.10;

        } else if (total >= 1000) {

            return total * 0.05;

        } else {

            return 0;
        }
    }

    // Count items
    static int countItems(int[] cart) {

        int count = 0;

        for (int x : cart) {

            count = count + x;
        }

        return count;
    }

    // Calculate average
    static double averagePrice(double[] prices) {

        double sum = 0;

        for (double p : prices) {

            sum = sum + p;
        }

        return sum / prices.length;
    }

    // Recursion
    static int orderNumber(int n) {

        if (n == 1) {

            return 1;
        }

        return n + orderNumber(n - 1);
    }

    // Display 2D array
    static void displayOrderMatrix(int[][] matrix) {

        System.out.println("\nOrder Matrix:");

        for (int i = 0; i < matrix.length; i++) {

            for (int j = 0; j < matrix[i].length; j++) {

                System.out.print(
                    matrix[i][j] + " "
                );
            }

            System.out.println();
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        String[] products = {
            "Laptop",
            "Headphones",
            "Keyboard",
            "Mouse"
        };

        double[] prices = {
            50000,
            2000,
            1500,
            800
        };

        int[] cart = new int[4];

        int choice;
        int orderCount = 0;

        do {

            System.out.println("\n==============================");
            System.out.println("    ONLINE SHOPPING CART");
            System.out.println("==============================");

            System.out.println("1. View Products");
            System.out.println("2. Add Product to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Search Product");
            System.out.println("5. Place Order");
            System.out.println("6. Exit");

            System.out.println("Enter your choice:");
            choice = sc.nextInt();

            switch (choice) {

                case 1:

                    displayProducts(
                        products,
                        prices
                    );

                    break;

                case 2:

                    displayProducts(
                        products,
                        prices
                    );

                    System.out.println(
                        "Enter product number:"
                    );

                    int productNumber =
                        sc.nextInt();

                    System.out.println(
                        "Enter quantity:"
                    );

                    int quantity =
                        sc.nextInt();

                    if (productNumber >= 1 &&
                        productNumber <= products.length) {

                        if (quantity > 0) {

                            addProduct(
                                cart,
                                productNumber - 1,
                                quantity
                            );

                        } else {

                            System.out.println(
                                "Invalid quantity!"
                            );
                        }

                    } else {

                        System.out.println(
                            "Invalid product number!"
                        );
                    }

                    break;

                case 3:

                    System.out.println(
                        "\n===== YOUR CART ====="
                    );

                    double total = 0;

                    for (int i = 0;
                         i < products.length;
                         i++) {

                        if (cart[i] == 0) {

                            continue;
                        }

                        double itemTotal =
                            calculateTotal(
                                prices[i],
                                cart[i]
                            );

                        total =
                            total + itemTotal;

                        System.out.println(
                            products[i] +
                            " x " +
                            cart[i] +
                            " = Rs. " +
                            itemTotal
                        );
                    }

                    System.out.println(
                        "Total = Rs. " + total
                    );

                    break;

                case 4:

                    sc.nextLine();

                    System.out.println(
                        "Enter product name:"
                    );

                    String name =
                        sc.nextLine();

                    int position =
                        searchProduct(
                            products,
                            name
                        );

                    if (position != -1) {

                        System.out.println(
                            "Product Found!"
                        );

                        System.out.println(
                            "Product: " +
                            products[position]
                        );

                        System.out.println(
                            "Price: Rs. " +
                            prices[position]
                        );

                    } else {

                        System.out.println(
                            "Product Not Found!"
                        );
                    }

                    break;

                case 5:

                    total =
                        calculateTotal(
                            prices,
                            cart
                        );

                    if (total == 0) {

                        System.out.println(
                            "Cart is empty!"
                        );

                        break;
                    }

                    double discount =
                        calculateDiscount(total);

                    double afterDiscount =
                        total - discount;

                    double tax =
                        afterDiscount * TAX;

                    double finalAmount =
                        afterDiscount + tax;

                    orderCount++;

                    String status;

                    if (finalAmount >= 50000) {

                        status =
                            "Premium Order";

                    } else {

                        status =
                            "Regular Order";
                    }

                    System.out.println(
                        "\n===== ORDER PIPELINE ====="
                    );

                    System.out.println(
                        "Total: Rs. " + total
                    );

                    System.out.println(
                        "Discount: Rs. " + discount
                    );

                    System.out.println(
                        "Tax: Rs. " + tax
                    );

                    System.out.println(
                        "Final Amount: Rs. " +
                        finalAmount
                    );

                    System.out.println(
                        "Order Type: " +
                        status
                    );

                    System.out.println(
                        "Order Number: " +
                        orderCount
                    );

                    System.out.println(
                        "Order Status: Confirmed"
                    );

                    System.out.println(
                        "Payment: Processing"
                    );

                    System.out.println(
                        "Shipping: Ready"
                    );

                    int result =
                        orderNumber(orderCount);

                    System.out.println(
                        "Pipeline Value: " +
                        result
                    );

                    int[][] orderMatrix = {
                        {1, 2},
                        {3, 4}
                    };

                    displayOrderMatrix(
                        orderMatrix
                    );

                    break;

                case 6:

                    System.out.println(
                        "Thank you for shopping!"
                    );

                    break;

                default:

                    System.out.println(
                        "Invalid choice!"
                    );
            }

        } while (choice != 6);

        int totalItems =
            countItems(cart);

        double average =
            averagePrice(prices);

        System.out.println(
            "\nTotal items in cart: " +
            totalItems
        );

        System.out.println(
            "Average product price: Rs. " +
            average
        );

        sc.close();
    
}
}
    
 