import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BrandPortalGUI extends JFrame {
    private Map<String, String> brandDatabase = new HashMap<>();
    private String loggedInBrand = null;
    private List<Earring> earringList = new ArrayList<>();

    private JTextField brandNameField;
    private JPasswordField passwordField;
    private JTextArea descriptionField;
    private JLabel earringImageLabel;
    private JLabel imagePreviewLabel; // New JLabel for image preview
    private JButton registerButton, loginButton, logoutButton, uploadButton, saveButton;

    // Panels for different views
    private JPanel loginPanel;
    private JPanel uploadPanel;
    private JPanel adminPanel;

    private JTable earringTable;
    private DefaultTableModel tableModel;

    public BrandPortalGUI() {
        setTitle("Brand Portal");
        setSize(600, 500); // Increased size to accommodate image preview
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        // Initialize the brand database with default credentials
        initializeDefaultCredentials();

        // Create panels
        loginPanel = createLoginPanel();
        uploadPanel = createUploadPanel();
        adminPanel = createAdminPanel();

        // Add panels to the frame
        add(loginPanel, "Login");
        add(uploadPanel, "Upload");
        add(adminPanel, "Admin");

        setVisible(true);
    }

    private void initializeDefaultCredentials() {
        brandDatabase.put("admin", "password");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        brandNameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        registerButton = new JButton("Register");
        loginButton = new JButton("Login");

        panel.add(new JLabel("Brand Name:"));
        panel.add(brandNameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(registerButton);
        panel.add(loginButton);

        // Action Listeners
        registerButton.addActionListener(e -> registerBrand());
        loginButton.addActionListener(e -> loginBrand());

        return panel;
    }

    private JPanel createUploadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        descriptionField = new JTextArea(5, 20);
        earringImageLabel = new JLabel("No image selected");
        imagePreviewLabel = new JLabel(); // JLabel to show image preview
        uploadButton = new JButton("Upload Earring Image");
        saveButton = new JButton("Save Earring Details");
        logoutButton = new JButton("Logout");

        uploadButton.setEnabled(false);
        saveButton.setEnabled(false);
        logoutButton.setEnabled(false);

        panel.add(new JLabel("Earring Description:"));
        panel.add(descriptionField);
        panel.add(uploadButton);
        panel.add(earringImageLabel);
        panel.add(imagePreviewLabel); // Add image preview label
        panel.add(saveButton);
        panel.add(logoutButton);

        // Action Listeners
        uploadButton.addActionListener(e -> uploadEarring());
        saveButton.addActionListener(e -> saveEarring());
        logoutButton.addActionListener(e -> logoutBrand());

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        String[] columnNames = {"Brand", "Description", "Image Path", "Image Preview"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) { // Image Preview column
                    return ImageIcon.class; // Return ImageIcon class for the preview column
                }
                return super.getColumnClass(columnIndex);
            }
        };
        earringTable = new JTable(tableModel);
        
        earringTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 3 && value instanceof ImageIcon) { // Check if the column is Image Preview
                    JLabel label = new JLabel((ImageIcon) value);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    return label;
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        JScrollPane scrollPane = new JScrollPane(earringTable);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logoutBrand());
        panel.add(logoutButton, BorderLayout.SOUTH);

        return panel;
    }

    private void registerBrand() {
        String brandName = brandNameField.getText();
        String password = new String(passwordField.getPassword());

        if (!brandName.isEmpty() && !password.isEmpty()) {
            if (!brandDatabase.containsKey(brandName)) {
                brandDatabase.put(brandName, password);
                JOptionPane.showMessageDialog(this, "Brand registered successfully: " + brandName);
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Brand already registered.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please fill in both fields.");
        }
    }

    private void loginBrand() {
        String brandName = brandNameField.getText();
        String password = new String(passwordField.getPassword());

        if (brandDatabase.containsKey(brandName) && brandDatabase.get(brandName).equals(password)) {
            loggedInBrand = brandName;
            JOptionPane.showMessageDialog(this, "Logged in as: " + loggedInBrand);
            clearFields();

            // If admin, show admin panel, otherwise show upload panel
            CardLayout layout = (CardLayout) getContentPane().getLayout();
            if (loggedInBrand.equals("admin")) {
                layout.show(getContentPane(), "Admin");
            } else {
                enableUploadOptions();
                layout.show(getContentPane(), "Upload");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
        }
    }

    private void logoutBrand() {
        if (loggedInBrand != null) {
            JOptionPane.showMessageDialog(this, "Logged out: " + loggedInBrand);
            loggedInBrand = null;
            disableUploadOptions();

            // Switch back to the login panel
            CardLayout layout = (CardLayout) getContentPane().getLayout();
            layout.show(getContentPane(), "Login");
        } else {
            JOptionPane.showMessageDialog(this, "You are not logged in.");
        }
    }

    private void enableUploadOptions() {
        uploadButton.setEnabled(true);
        saveButton.setEnabled(true);
        logoutButton.setEnabled(true);
    }

    private void disableUploadOptions() {
        uploadButton.setEnabled(false);
        saveButton.setEnabled(false);
        logoutButton.setEnabled(false);
        imagePreviewLabel.setIcon(null); // Clear the image preview
    }

    private void uploadEarring() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Earring Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            earringImageLabel.setText("Selected Image: " + selectedFile.getName());
            showImagePreview(selectedFile); // Show the image preview
        }
    }

    private void showImagePreview(File file) {
        // Load the image and set it as an icon for the image preview label
        ImageIcon imageIcon = new ImageIcon(file.getPath());
        Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Resize image to fit in label
        imagePreviewLabel.setIcon(new ImageIcon(image));
    }

    private void saveEarring() {
        if (loggedInBrand != null) {
            String description = descriptionField.getText();
            if (earringImageLabel.getText().contains("Selected Image:")) {
                String imagePath = earringImageLabel.getText().substring(15); // Extract image name
                ImageIcon imageIcon = new ImageIcon(imagePath); // Load the image icon
                earringList.add(new Earring(loggedInBrand, description, imagePath));
                tableModel.addRow(new Object[]{loggedInBrand, description, imagePath, imageIcon});
                JOptionPane.showMessageDialog(this, "Earring details saved.");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Please upload an earring image.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "You must be logged in to save earring details.");
        }
    }

    private void clearFields() {
        brandNameField.setText("");
        passwordField.setText("");
        descriptionField.setText("");
        earringImageLabel.setText("No image selected");
        imagePreviewLabel.setIcon(null); // Clear the image preview
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BrandPortalGUI::new);
    }
}

class Earring {
    private String brand;
    private String description;
    private String imagePath;

    public Earring(String brand, String description, String imagePath) {
        this.brand = brand;
        this.description = description;
        this.imagePath = imagePath;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }
}