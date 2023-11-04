package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import alert.AlertMaker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import model.DataStored;
import model.Database;
import model.StudentID;

public class AccountController {

    @FXML
    private TextField LastNameField, GivenNameField, StudentIDField, ProgramField;

    private StudentID student = new StudentID();

    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }


    public void StudentID() throws IOException {

    String LastName = LastNameField.getText();
    String GivenName = GivenNameField.getText();
    String IDNumber = StudentIDField.getText();
    String program = ProgramField.getText();

    if (!LastName.isEmpty() && !GivenName.isEmpty() && !IDNumber.isEmpty() && !program.isEmpty()) {
        try (Connection connection = Database.DBConnect()) {
            String task = "SELECT * FROM `studentID` WHERE LastName = ? AND GivenName = ? AND IDNumber = ? AND program = ? AND username = ?";
            PreparedStatement statement = connection.prepareStatement(task);
            statement.setString(1, LastName);
            statement.setString(2, GivenName);
            statement.setString(3, IDNumber);
            statement.setString(4, program);
            statement.setString(5, DataStored.username);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                AlertMaker.showSimpleAlert("Notifications", "Task is already existing");
            } else {
                String insertTask = "INSERT INTO studentID (LastName, GivenName, IDNumber, program, username) VALUES (?, ? , ?, ?, ?)";
                statement = connection.prepareStatement(insertTask);
                statement.setString(1, LastName);
                statement.setString(2, GivenName);
                statement.setString(3, IDNumber);
                statement.setString(4, program);
                statement.setString(5, DataStored.username);

                statement.executeUpdate();  
            
                student.setLastName(LastName);
                student.setGivenName(GivenName);
                student.setIDNumber(IDNumber);
                student.setProgram(program);
            
                homeController.displayUser();
            }   
        }   catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Display an error message if the input is empty
            AlertMaker.showSimpleAlert("Input Error", "Input Cannot be Blanked.");
        }

    }
}