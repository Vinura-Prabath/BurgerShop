import javax.swing.*;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// ======ORDER MODEL ========
class Order {
    private String orderID;
    private String customerID;
    private String customerName;
    private int quantity;
    private double burgerPrice;
    private double totalPrice;
    private String status;

    public Order(String orderID, String customerID, String customerName, int quantity, double burgerPrice, String status) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.customerName = customerName;
        this.quantity = quantity;
        this.burgerPrice = burgerPrice;
        this.totalPrice = quantity * burgerPrice;
        this.status = status;
    }

    public String getOrderID() { return orderID; }
    public String getCustomerID() { return customerID; }
    public String getCustomerName() { return customerName; }
    public int getQuantity() { return quantity; }
    public double getBurgerPrice() { return burgerPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = quantity * burgerPrice;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

// =======ORDER MANAGER ===========
class OrderManager {
    private ArrayList<Order> orders;
    private int orderCounter = 1;
    private int customerCounter = 1;
    private static final double BURGER_PRICE = 500.0;
    
    // Track existing customers to map names to unique Customer IDs
    private Map<String, String> customerMap = new HashMap<>();

    public OrderManager() {
        orders = new ArrayList<>();
    }

    public String generateNextOrderID() {
        return String.format("%04d", orderCounter);
    }

    public String getOrCreateCustomerID(String name) {
        String cleanName = name.trim().toLowerCase();
        if (customerMap.containsKey(cleanName)) {
            return customerMap.get(cleanName);
        }
        String newID = String.format("C%03d", customerCounter++);
        customerMap.put(cleanName, newID);
        return newID;
    }

    public void placeOrder(String customerName, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        String orderID = String.format("%04d", orderCounter++);
        String customerID = getOrCreateCustomerID(customerName);
        Order order = new Order(orderID, customerID, customerName, quantity, BURGER_PRICE, "PREPARING");
        orders.add(order);
    }

    public Order searchOrderByID(String orderID) {
        for (Order order : orders) {
            if (order.getOrderID().equals(orderID)) {
                return order;
            }
        }
        return null;
    }

    public ArrayList<Order> searchOrdersByCustomerID(String customerID) {
        ArrayList<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCustomerID().equalsIgnoreCase(customerID)) {
                result.add(order);
            }
        }
        return result;
    }

    public ArrayList<Order> getOrdersByStatus(String status) {
        ArrayList<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus().equalsIgnoreCase(status)) {
                result.add(order);
            }
        }
        return result;
    }

    public ArrayList<Object[]> getBestCustomers() {
        Map<String, Double> customerTotals = new HashMap<>();
        Map<String, String> idMap = new HashMap<>();
        Map<String, String> normalNameMap = new HashMap<>();

        for (Order order : orders) {
            if (order.getStatus().equalsIgnoreCase("DELIVERED")) {
                String key = order.getCustomerName().trim().toLowerCase();
                customerTotals.put(key, customerTotals.getOrDefault(key, 0.0) + order.getTotalPrice());
                idMap.put(key, order.getCustomerID());
                normalNameMap.put(key, order.getCustomerName());
            }
        }

        ArrayList<Map.Entry<String, Double>> list = new ArrayList<>(customerTotals.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        ArrayList<Object[]> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : list) {
            result.add(new Object[]{
                idMap.get(entry.getKey()),
                normalNameMap.get(entry.getKey()),
                entry.getValue()
            });
        }
        return result;
    }
}

// =========MAIN CONTROLLER=============
class BurgerShopController {
    private static BurgerShopController instance;
    private OrderManager orderManager;

    private BurgerShopController() {
        orderManager = new OrderManager();
    }

    public static BurgerShopController getInstance() {
        if (instance == null) {
            instance = new BurgerShopController();
        }
        return instance;
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }
}

// ======UI STYLING==============
class UIComponents {
    public static JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(217, 83, 79)); 
        header.setPreferredSize(new Dimension(800, 55));
        
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 22));
        label.setForeground(Color.WHITE);
        header.add(label, BorderLayout.CENTER);
        return header;
    }

    // Custom Button Component for Capsule Shapes
    private static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false); // Keeps standard background painting off
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }
            
            // Draw a capsule/pill shape matching button's height
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            
            super.paintComponent(g);
        }
    }

    public static JButton createStyledButton(String text, Color bg) {
        JButton btn = new RoundedButton(text); // Using custom rounded button
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 38));
        return btn;
    }
}

// =========HOME PAGE GUI==============
class HomeFrame extends JFrame {
    public HomeFrame() {
        setTitle("iHungry Burger Shop Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520); 
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        // Left Banner Panel
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel titleLabel = new JLabel("Welcome to Burgers", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(184, 134, 11));
        leftPanel.add(titleLabel, gbc);

        // ======Add Image======
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        try {
            ImageIcon originalIcon = new ImageIcon("burger.jpg"); 
            Image scaledImage = originalIcon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            
            JLabel imageLabel = new JLabel(scaledIcon);
            leftPanel.add(imageLabel, gbc);
        } catch (Exception e) {
            System.out.println("Image could not be loaded: " + e.getMessage());
        }

        // Right Menu Panel
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(230, 230, 230));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        String[] options = {"Place Order", "Search", "View Orders", "Update Order Details"};
        Color btnColor = new Color(217, 83, 79);

        rightPanel.add(Box.createVerticalGlue());
        for (String opt : options) {
            JButton btn = UIComponents.createStyledButton(opt, btnColor);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(240, 40));
            btn.addActionListener(e -> navigate(opt));
            rightPanel.add(btn);
            rightPanel.add(Box.createVerticalStrut(15));
        }

        // Lower right positioning for Exit button using layout logic matching the image reference
        JPanel exitContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exitContainer.setBackground(new Color(230, 230, 230));
        JButton exitBtn = UIComponents.createStyledButton("Exit", new Color(217, 83, 79));
        exitBtn.setPreferredSize(new Dimension(100, 40));
        exitBtn.addActionListener(e -> System.exit(0));
        exitContainer.add(exitBtn);

        rightPanel.add(exitContainer);
        rightPanel.add(Box.createVerticalGlue());

        add(leftPanel);
        add(rightPanel);
        setVisible(true);
    }

    private void navigate(String option) {
        switch (option) {
            case "Place Order": new PlaceOrderFrame(); break;
            case "Search": new SearchSelectionFrame(); break;
            case "View Orders": new ViewOrdersCategoryFrame(); break;
            case "Update Order Details": new UpdateOrderFrame(); break;
        }
    }
}

// =======PLACE ORDER FRAME========
class PlaceOrderFrame extends JFrame {
    private JTextField nameField, qtyField;
    private JLabel orderIdVal, totalVal;
    private OrderManager manager;

    public PlaceOrderFrame() {
        manager = BurgerShopController.getInstance().getOrderManager();
        setTitle("Place Order");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIComponents.createHeader("Place Order"), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(Color.WHITE);

        JLabel lbl1 = new JLabel("Order Id :"); lbl1.setBounds(50, 40, 100, 30);
        orderIdVal = new JLabel(manager.generateNextOrderID()); orderIdVal.setBounds(180, 40, 100, 30);
        orderIdVal.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lbl2 = new JLabel("Customer Name :"); lbl2.setBounds(50, 90, 120, 30);
        nameField = new JTextField(); nameField.setBounds(180, 90, 180, 30);

        JLabel lbl3 = new JLabel("Burger QTY :"); lbl3.setBounds(50, 150, 100, 30);
        qtyField = new JTextField(); qtyField.setBounds(180, 150, 180, 30);

        JLabel lbl4 = new JLabel("Order Status :"); lbl4.setBounds(50, 200, 100, 30);
        JLabel statusVal = new JLabel("Pending.."); statusVal.setBounds(180, 200, 100, 30);

        totalVal = new JLabel("NET Total: Rs. 0.00");
        totalVal.setBounds(400, 280, 200, 30);
        totalVal.setFont(new Font("Arial", Font.BOLD, 16));
        totalVal.setForeground(new Color(217, 83, 79));

        qtyField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    int qty = Integer.parseInt(qtyField.getText().trim());
                    totalVal.setText(String.format("NET Total: Rs. %.2f", qty * 500.0));
                } catch (NumberFormatException ex) {
                    totalVal.setText("NET Total: Rs. 0.00");
                }
            }
        });

        mainPanel.add(lbl1); mainPanel.add(orderIdVal);
        mainPanel.add(lbl2); mainPanel.add(nameField);
        mainPanel.add(lbl3); mainPanel.add(qtyField);
        mainPanel.add(lbl4); mainPanel.add(statusVal);
        mainPanel.add(totalVal);

        JPanel rightBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton placeBtn = UIComponents.createStyledButton("Place Order", new Color(40, 167, 69));
        JButton backBtn = UIComponents.createStyledButton("Back to Home", new Color(108, 117, 125));
        
        placeBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if(name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Customer name cannot be empty!");
                return;
            }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0");
                    return;
                }
                manager.placeOrder(name, qty);
                JOptionPane.showMessageDialog(this, "Order Placed Successfully!");
                dispose();
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid quantity value!");
            }
        });
        
        backBtn.addActionListener(e -> dispose());

        rightBtnPanel.add(placeBtn);
        rightBtnPanel.add(backBtn);
        
        add(mainPanel, BorderLayout.CENTER);
        add(rightBtnPanel, BorderLayout.SOUTH);
        setVisible(true);
    }
}

// =====SEARCH SELECTION FRAME========
class SearchSelectionFrame extends JFrame {
    public SearchSelectionFrame() {
        setTitle("Search Menu Options");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnBest = UIComponents.createStyledButton("Search Best Customer", new Color(217, 83, 79));
        JButton btnOrder = UIComponents.createStyledButton("Search Order Details", new Color(217, 83, 79));
        JButton btnCust = UIComponents.createStyledButton("Search Customer Details", new Color(217, 83, 79));

        btnBest.addActionListener(e -> { dispose(); new SearchBestCustomerFrame(); });
        btnOrder.addActionListener(e -> { dispose(); new SearchOrderFrame(); });
        btnCust.addActionListener(e -> { dispose(); new SearchCustomerFrame(); });

        gbc.gridy = 0; add(btnBest, gbc);
        gbc.gridy = 1; add(btnOrder, gbc);
        gbc.gridy = 2; add(btnCust, gbc);

        setVisible(true);
    }
}

// =======SEARCH BEST CUSTOMER==========
class SearchBestCustomerFrame extends JFrame {
    public SearchBestCustomerFrame() {
        setTitle("Search Best Customers");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIComponents.createHeader("Search Best Customers"), BorderLayout.NORTH);

        String[] columns = {"Customer ID", "Name", "Total Purchases"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        
        ArrayList<Object[]> data = BurgerShopController.getInstance().getOrderManager().getBestCustomers();
        for (Object[] row : data) {
            model.addRow(new Object[]{row[0], row[1], String.format("Rs. %.2f", (Double)row[2])});
        }

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = UIComponents.createStyledButton("Back", new Color(108, 117, 125));
        backBtn.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(backBtn);
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}

// =========SEARCH ORDER FRAME============
class SearchOrderFrame extends JFrame {
    public SearchOrderFrame() {
        setTitle("Search Order Details");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIComponents.createHeader("Search Order Details"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(null);
        contentPanel.setBackground(Color.WHITE);

        JLabel lblInput = new JLabel("Enter Order ID:"); lblInput.setBounds(40, 30, 120, 30);
        JTextField txtInput = new JTextField(); txtInput.setBounds(160, 30, 150, 30);
        JButton btnSearch = UIComponents.createStyledButton("Search", new Color(217, 83, 79));
        btnSearch.setBounds(330, 30, 100, 30);

        JLabel lblCustId = new JLabel("Customer ID : "); lblCustId.setBounds(60, 100, 300, 25);
        JLabel lblName = new JLabel("Name : "); lblName.setBounds(60, 140, 300, 25);
        JLabel lblQty = new JLabel("QTY : "); lblQty.setBounds(60, 180, 300, 25);
        JLabel lblTotal = new JLabel("Total : "); lblTotal.setBounds(60, 220, 300, 25);
        JLabel lblStatus = new JLabel("Order Status : "); lblStatus.setBounds(60, 260, 300, 25);

        btnSearch.addActionListener(e -> {
            Order o = BurgerShopController.getInstance().getOrderManager().searchOrderByID(txtInput.getText().trim());
            if (o != null) {
                lblCustId.setText("Customer ID : " + o.getCustomerID());
                lblName.setText("Name : " + o.getCustomerName());
                lblQty.setText("QTY : " + o.getQuantity());
                lblTotal.setText(String.format("Total : Rs. %.2f", o.getTotalPrice()));
                lblStatus.setText("Order Status : " + o.getStatus());
            } else {
                JOptionPane.showMessageDialog(this, "Order ID not found.");
            }
        });

        contentPanel.add(lblInput); contentPanel.add(txtInput); contentPanel.add(btnSearch);
        contentPanel.add(lblCustId); contentPanel.add(lblName); contentPanel.add(lblQty);
        contentPanel.add(lblTotal); contentPanel.add(lblStatus);

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}

// ==========SEARCH CUSTOMER FRAME===============
class SearchCustomerFrame extends JFrame {
    public SearchCustomerFrame() {
        setTitle("Search Customer Details");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIComponents.createHeader("Search Customer"), BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.add(new JLabel("Enter Customer ID:"));
        JTextField txtCustId = new JTextField(12);
        topPanel.add(txtCustId);
        JButton btnSearch = UIComponents.createStyledButton("Search", new Color(217, 83, 79));
        btnSearch.setPreferredSize(new Dimension(100, 30));
        topPanel.add(btnSearch);

        JLabel lblName = new JLabel("Name: --");
        topPanel.add(lblName);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Order ID", "Order QTY", "Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnSearch.addActionListener(e -> {
            model.setRowCount(0);
            String cid = txtCustId.getText().trim();
            ArrayList<Order> list = BurgerShopController.getInstance().getOrderManager().searchOrdersByCustomerID(cid);
            if(!list.isEmpty()) {
                lblName.setText("Name: " + list.get(0).getCustomerName());
                for(Order o : list) {
                    model.addRow(new Object[]{o.getOrderID(), o.getQuantity(), String.format("Rs. %.2f", o.getTotalPrice())});
                }
            } else {
                lblName.setText("Name: --");
                JOptionPane.showMessageDialog(this, "No orders found for this customer ID.");
            }
        });

        setVisible(true);
    }
}

// ===========VIEW ORDERS BY CATEGORY=============
class ViewOrdersCategoryFrame extends JFrame {
    public ViewOrdersCategoryFrame() {
        setTitle("View Orders");
        setSize(450, 320);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnDelivered = UIComponents.createStyledButton("Delivered Orders", new Color(40, 167, 69));
        JButton btnPreparing = UIComponents.createStyledButton("Preparing Orders", new Color(23, 162, 184));
        JButton btnCancelled = UIComponents.createStyledButton("Cancelled Orders", new Color(217, 83, 79));

        btnDelivered.addActionListener(e -> new TableDisplayFrame("DELIVERED"));
        btnPreparing.addActionListener(e -> new TableDisplayFrame("PREPARING"));
        btnCancelled.addActionListener(e -> new TableDisplayFrame("CANCELLED"));

        gbc.gridy = 0; add(btnDelivered, gbc);
        gbc.gridy = 1; add(btnPreparing, gbc);
        gbc.gridy = 2; add(btnCancelled, gbc);

        setVisible(true);
    }
}

class TableDisplayFrame extends JFrame {
    public TableDisplayFrame(String status) {
        setTitle(status + " Orders List");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIComponents.createHeader(status + " Orders"), BorderLayout.NORTH);

        String[] columns = {"Order ID", "Customer ID", "Name", "Order QTY", "Total"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        ArrayList<Order> list = BurgerShopController.getInstance().getOrderManager().getOrdersByStatus(status);
        for (Order o : list) {
            model.addRow(new Object[]{o.getOrderID(), o.getCustomerID(), o.getCustomerName(), o.getQuantity(), String.format("Rs. %.2f", o.getTotalPrice())});
        }

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setVisible(true);
    }
}

// ==========UPDATE ORDER FRAME============
class UpdateOrderFrame extends JFrame {
    private JTextField txtOrderId, txtNewQty;
    private JComboBox<String> cmbStatus;
    private JLabel lblCustId, lblName, lblCurrentQty, lblTotal, lblStatusVal;
    private OrderManager manager;
    private Order currentFoundOrder;

    public UpdateOrderFrame() {
        manager = BurgerShopController.getInstance().getOrderManager();
        setTitle("Update Order Details");
        setSize(650, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIComponents.createHeader("Update Order Details"), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(null);
        contentPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Enter Order ID:"); lblSearch.setBounds(30, 20, 100, 30);
        txtOrderId = new JTextField(); txtOrderId.setBounds(140, 20, 150, 30);
        JButton btnSearch = UIComponents.createStyledButton("Search", new Color(217, 83, 79));
        btnSearch.setBounds(310, 20, 90, 30);

        lblCustId = new JLabel("Customer ID: "); lblCustId.setBounds(40, 80, 250, 25);
        lblName = new JLabel("Name: "); lblName.setBounds(40, 110, 250, 25);
        lblCurrentQty = new JLabel("Current QTY: "); lblCurrentQty.setBounds(40, 140, 250, 25);
        lblTotal = new JLabel("Total Value: "); lblTotal.setBounds(40, 170, 250, 25);
        lblStatusVal = new JLabel("Order Status: "); lblStatusVal.setBounds(40, 200, 250, 25);

        JSeparator sep = new JSeparator(); sep.setBounds(30, 240, 570, 10);

        JLabel lblUpdateOptions = new JLabel("Update Selections:");
        lblUpdateOptions.setFont(new Font("Arial", Font.BOLD, 14));
        lblUpdateOptions.setBounds(30, 260, 200, 25);

        JLabel lblNewQty = new JLabel("New Burger Quantity:"); lblNewQty.setBounds(40, 300, 150, 25);
        txtNewQty = new JTextField(); txtNewQty.setBounds(200, 300, 150, 25);
        txtNewQty.setEnabled(false);

        JLabel lblNewStatus = new JLabel("New Order Status:"); lblNewStatus.setBounds(40, 340, 150, 25);
        cmbStatus = new JComboBox<>(new String[]{"PREPARING", "DELIVERED", "CANCELLED"});
        cmbStatus.setBounds(200, 340, 150, 25);
        cmbStatus.setEnabled(false);

        JButton btnUpdate = UIComponents.createStyledButton("Update Order", new Color(40, 167, 69));
        btnUpdate.setBounds(200, 400, 160, 35);
        btnUpdate.setEnabled(false);

        btnSearch.addActionListener(e -> {
            currentFoundOrder = manager.searchOrderByID(txtOrderId.getText().trim());
            if (currentFoundOrder != null) {
                lblCustId.setText("Customer ID: " + currentFoundOrder.getCustomerID());
                lblName.setText("Name: " + currentFoundOrder.getCustomerName());
                lblCurrentQty.setText("Current QTY: " + currentFoundOrder.getQuantity());
                lblTotal.setText(String.format("Total Value: Rs. %.2f", currentFoundOrder.getTotalPrice()));
                lblStatusVal.setText("Order Status: " + currentFoundOrder.getStatus());

                if (!currentFoundOrder.getStatus().equals("PREPARING")) {
                    JOptionPane.showMessageDialog(this, "Sorry, you cannot update this order. It has already been " + currentFoundOrder.getStatus() + ".");
                    txtNewQty.setEnabled(false);
                    cmbStatus.setEnabled(false);
                    btnUpdate.setEnabled(false);
                } else {
                    txtNewQty.setText(String.valueOf(currentFoundOrder.getQuantity()));
                    cmbStatus.setSelectedItem(currentFoundOrder.getStatus());
                    txtNewQty.setEnabled(true);
                    cmbStatus.setEnabled(true);
                    btnUpdate.setEnabled(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Order ID!");
            }
        });

        btnUpdate.addActionListener(e -> {
            if (currentFoundOrder != null) {
                try {
                    int nQty = Integer.parseInt(txtNewQty.getText().trim());
                    if (nQty <= 0) {
                        JOptionPane.showMessageDialog(this, "Quantity must be greater than 0");
                        return;
                    }
                    currentFoundOrder.setQuantity(nQty);
                    currentFoundOrder.setStatus((String) cmbStatus.getSelectedItem());
                    JOptionPane.showMessageDialog(this, "Order Updated Successfully!");
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid quantity formatted entry.");
                }
            }
        });

        contentPanel.add(lblSearch); contentPanel.add(txtOrderId); contentPanel.add(btnSearch);
        contentPanel.add(lblCustId); contentPanel.add(lblName); contentPanel.add(lblCurrentQty);
        contentPanel.add(lblTotal); contentPanel.add(lblStatusVal); contentPanel.add(sep);
        contentPanel.add(lblUpdateOptions); contentPanel.add(lblNewQty); contentPanel.add(txtNewQty);
        contentPanel.add(lblNewStatus); contentPanel.add(cmbStatus); contentPanel.add(btnUpdate);

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }
}

public class BurgerShop {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomeFrame());
    }
}