package lk.ijse.dep8.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import lk.ijse.dep8.Customer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;


public class ManageCustomerFormController {
    private final Path dbPath = Paths.get("database/customers.dep8db");
    public TextField txtID;
    public TextField txtName;
    public TextField txtAddress;
    public TableView<Customer> tblCustomers;
    public TextField txtPicture;
    public AnchorPane Anchorpane;
    public Button btnSave;
    public ImageView img;

    public void initialize() {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Customer, Button> lastCol = (TableColumn<Customer, Button>) tblCustomers.getColumns().get(4);
        lastCol.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");
            btnDelete.setOnAction(event -> {
                tblCustomers.getItems().remove(param.getValue());
                saveCustomers();
            });
            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        TableColumn<Customer, ImageView> col = (TableColumn<Customer, ImageView>) tblCustomers.getColumns().get(3);
        col.setCellValueFactory(param -> {
            ByteArrayInputStream bis = new ByteArrayInputStream(param.getValue().getBytes());
            ImageView imageView = new ImageView(new Image(bis));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            return new ReadOnlyObjectWrapper<>(imageView);
        });

        tblCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtID.setText(newValue.getId());
                txtID.setEditable(false);
                txtName.setText(newValue.getName());
                txtAddress.setText(newValue.getAddress());
                txtPicture.setText("[PICTURE]");
                ByteArrayInputStream bis = new ByteArrayInputStream(newValue.getBytes());
                img.setImage(new Image(bis));
                btnSave.setText("Update");
            }
        });

        Anchorpane.setOnMouseClicked(event -> {
            if(!tblCustomers.getSelectionModel().isEmpty()){
                tblCustomers.getSelectionModel().clearSelection();
                clearFields();
                txtID.setEditable(true);
                txtID.requestFocus();
                btnSave.setText("Save Customer");
            }
        });

        initDatabase();
    }

    private void clearFields() {
        txtID.clear();
        txtName.clear();
        txtAddress.clear();
        txtPicture.clear();
        img.setImage(null);
    }

    public void btnSaveCustomer_OnAction(ActionEvent actionEvent) throws IOException {
        if (isValidated()){
            if (btnSave.getText().equals("Save Customer")){
                Path path = Paths.get(txtPicture.getText());
                InputStream is = Files.newInputStream(path);
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                is.close();

                Customer newCustomer = new Customer(
                        txtID.getText(),
                        txtName.getText(),
                        txtAddress.getText(),bytes);
                tblCustomers.getItems().add(newCustomer);

                boolean result = saveCustomers();

                if (!result) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save the customer, try again").show();
                    tblCustomers.getItems().remove(newCustomer);
                } else {
                    new Alert(Alert.AlertType.CONFIRMATION,"Customer Saved Successfully!",ButtonType.OK).show();
                    clearFields();
                }
                txtID.requestFocus();
            }else {
                Customer cus = tblCustomers.getItems().stream().filter(customer -> customer.getId().equals(txtID.getText())).findAny().orElse(null);
                cus.setName(txtName.getText());
                cus.setAddress(txtAddress.getText());
                if (!txtPicture.getText().equals("[PICTURE]")){
                    try {
                        Path path = Paths.get(txtPicture.getText());
                        InputStream is = Files.newInputStream(path);
                        byte[] bytes = new byte[is.available()];
                        is.read(bytes);
                        cus.setBytes(bytes);
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR,"Failed read the picture,try again!",ButtonType.OK).show();
                        e.printStackTrace();
                    }
                }
                boolean res = saveCustomers();
                if (!res){
                    new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again.",ButtonType.OK).show();
                    return;
                }
                new Alert(Alert.AlertType.CONFIRMATION,"Updated Successfully!",ButtonType.OK).show();
                clearFields();
                btnSave.setText("Save Customers");
                tblCustomers.refresh();
                txtID.setEditable(true);
                txtID.requestFocus();
            }
        }
    }

    public boolean isValidated(){
        if (!txtID.getText().matches("C\\d{3}") ||
                (tblCustomers.getItems().stream().anyMatch(c -> c.getId().equalsIgnoreCase(txtID.getText())) && btnSave.getText().equals("Save Customer"))){
            txtID.requestFocus();
            txtID.selectAll();
            return false;
        } else if (txtName.getText().trim().isEmpty()) {
            txtName.requestFocus();
            txtName.selectAll();
            return false;
        } else if (txtAddress.getText().trim().isEmpty()) {
            txtAddress.requestFocus();
            txtAddress.selectAll();
            return false;
        } else if (txtPicture.getText().trim().isEmpty()) {
            txtPicture.requestFocus();
            return false;
        }else {
            return true;
        }
    }

    private void initDatabase() {
        try {

            if (!Files.exists(dbPath)) {
                Files.createDirectories(dbPath.getParent());
                Files.createFile(dbPath);
            }

            loadAllCustomers();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to initialize the database").showAndWait();
            Platform.exit();
        }
    }

    private boolean saveCustomers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dbPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(new ArrayList<Customer>(tblCustomers.getItems()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadAllCustomers() {
        try (InputStream is = Files.newInputStream(dbPath, StandardOpenOption.READ);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            tblCustomers.getItems().clear();
            tblCustomers.setItems(FXCollections.observableArrayList((ArrayList<Customer>) ois.readObject()));
        } catch (IOException | ClassNotFoundException e) {
            if (!(e instanceof EOFException)) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to load customers").showAndWait();
            }
        }
    }

    public void btnBrowseOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("images", "*.jpg", "*.jpeg", ".png"));
        File file = fileChooser.showOpenDialog(tblCustomers.getScene().getWindow());
        if (file != null) {
            txtPicture.setText(file.getAbsolutePath());
            try {
                InputStream is = Files.newInputStream(Paths.get(file.getAbsolutePath()));
                img.setImage(new Image(is));
                img.setFitHeight(100);
                img.setFitWidth(100);
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR,"Can not read the Image file,Please try again!").show();
                txtPicture.clear();
                txtPicture.requestFocus();
                e.printStackTrace();
            }
        }

    }
}
