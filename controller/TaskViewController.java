package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import alert.AlertMaker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.DataStored;
import model.Database;
import model.UserTask;
import model.UserTaskMemento;

public class TaskViewController implements Initializable {

    @FXML
    private ComboBox DaysChoices;

    @FXML
    private Button InserttaskButton, homeButton, journalButton;

    @FXML
    private TextArea taskInput;

    @FXML
    TableView<UserTask> displayTaskTable;

    @FXML
    TableColumn<UserTask, String> Monday;

    String userAccount = DataStored.username;

    ObservableList<UserTask> dataList;

    Connection connect;
    Statement statement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {



        ObservableList<String> daysOfWeek = FXCollections.observableArrayList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        );

        DaysChoices.setItems(daysOfWeek);


        Monday.setCellValueFactory(new PropertyValueFactory<>("task"));
        try {
            showTask();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    public void insertTask() throws SQLException {
        connect = Database.DBConnect();
        statement = connect.createStatement();
    
        Object selectedDay = DaysChoices.getValue();
        
        if (selectedDay == null) {
            AlertMaker.showSimpleAlert("Notifications", "Please select a day.");
            return;
        }
    
        String task = "SELECT Task FROM taskinput WHERE Task = '" + taskInput.getText() + "' AND Username = '" + userAccount + "'";
        
        ResultSet result = statement.executeQuery(task);
        
        if (result.next()) {
            AlertMaker.showSimpleAlert("Notifications", "Task is already existing");
        } else {
            String insertTask = "INSERT INTO taskinput (Task, Username, Day) VALUES ('" + taskInput.getText() + "', '" +  userAccount + "', '" + selectedDay + "')";
            statement.executeUpdate(insertTask);
    
            // After inserting the task, update the table to reflect the changes
            showTask();
        }
    }

    public void showTask() throws SQLException {
        Object selectedDay = DaysChoices.getValue();
        
        if (selectedDay == null) {
            AlertMaker.showSimpleAlert("Notifications", "Please select a day.");
            return;
        }
    
        dataList = TaskList();
        displayTaskTable.setItems(dataList);
    }

    //Retrieval of data from xampp
    public ObservableList<UserTask> TaskList() throws SQLException{
        ObservableList<UserTask> DataList = FXCollections.observableArrayList();

        String selectedDay = DaysChoices.getValue().toString();

        String retrieveData = "SELECT * FROM taskinput WHERE Username = '" + userAccount + "' AND Day = '" + selectedDay + "'";
        connect = Database.DBConnect();
        statement = connect.createStatement();

        try {
            ResultSet result = statement.executeQuery(retrieveData);

            UserTask UT;

            while (result.next()) {
                UT = new UserTask(result.getString("Task"));

                DataList.add(UT);
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }

        return DataList;

    
    }

    

    @FXML
    public void toHomePage(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void toJournal(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Journal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

   

}

