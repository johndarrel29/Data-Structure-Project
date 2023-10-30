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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    private ChoiceBox<String> DaysChoices;

    @FXML
    private Button InserttaskButton, homeButton, journalButton;

    @FXML
    private TextArea taskInput;

    @FXML
    TableView<UserTask> mondayTaskTable, tuesdayTaskTable, wednesdayTaskTable, 
    thursdayTaskTable, fridayTaskTable, saturdayTaskTable;

    @FXML
    TableColumn<UserTask, String> Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;

    String userAccount = DataStored.username;
    
    ObservableList<UserTask> TaskList;

    Connection connect;
    Statement statement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {



        ObservableList<String> daysOfWeek = FXCollections.observableArrayList(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        );

        DaysChoices.setValue("Choose a day");
        DaysChoices.setItems(daysOfWeek);



        try {
            showTaskList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



    public void insertTask()throws SQLException{

        connect = Database.DBConnect();
        statement = connect.createStatement();

        String task = "SELECT Task FROM taskinput WHERE Task = '" + taskInput.getText() + "' AND Username = '" + userAccount + "' AND Day = '" + DaysChoices.getValue() + "'";
        
        ResultSet result = statement.executeQuery(task);
        
        if (result.next()) {
            AlertMaker.showSimpleAlert("Notifications", "Task is already existing");
        } else {

            String selectedDay = DaysChoices.getValue();

            String insertTask = "INSERT INTO taskinput (Task, Username, Day) VALUES ('" + taskInput.getText() + "', '" +  userAccount + "', '" + selectedDay + "')";
            statement.executeUpdate(insertTask);
            System.out.println(selectedDay);
            
            // //save the current state for possible undo action
            // undoStack.push(new UserTaskMemento(task_Input.getText(), DataStored.username, true));
           
            // //clear the redo stack, as a new action is performed
            // redoStack.clear();

            showTaskList();

            // // Enable the "Undo" button when a new task is inserted
            // UndoButton.setDisable(false);
            // // Disable the "Redo" button when a new action is performed
            // RedoButton.setDisable(true);
            // deleteButton.setDisable(false);
        }

    }



    //Retrieval of data from xampp
    public ObservableList<UserTask> dataTaskList(String day) throws SQLException {
        ObservableList<UserTask> DataList = FXCollections.observableArrayList();

        String selectedDay = DaysChoices.getValue();
    
        if (selectedDay != null && !selectedDay.isEmpty()) {
            String retrieveData = "SELECT * FROM taskinput WHERE Username = '" + userAccount + "' AND Day = '" + day + "'";
            connect = Database.DBConnect();
            statement = connect.createStatement();
    
            try {
                ResultSet result = statement.executeQuery(retrieveData);
    
                UserTask UT;
    
                while (result.next()) {
                    UT = new UserTask(result.getString("Task"), result.getString("Day"));
                    DataList.add(UT);
                }

                // Print the number of retrieved tasks
                System.out.println("Retrieved " + DataList.size() + " tasks for " + day);
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        return DataList;
    }
    

    public void showTaskList() throws SQLException {
        TaskList = dataTaskList("");

        Monday.setCellValueFactory(new PropertyValueFactory<>("Task"));
        Tuesday.setCellValueFactory(new PropertyValueFactory<>("Task"));
        Wednesday.setCellValueFactory(new PropertyValueFactory<>("Task"));
        Thursday.setCellValueFactory(new PropertyValueFactory<>("Task"));
        Friday.setCellValueFactory(new PropertyValueFactory<>("Task"));
        Saturday.setCellValueFactory(new PropertyValueFactory<>("Task"));

        mondayTaskTable.setItems(dataTaskList("Monday"));
        tuesdayTaskTable.setItems(dataTaskList("Tuesday"));
        wednesdayTaskTable.setItems(dataTaskList("Wednesday"));
        thursdayTaskTable.setItems(dataTaskList("Thursday"));
        fridayTaskTable.setItems(dataTaskList("Friday"));
        saturdayTaskTable.setItems(dataTaskList("Saturday"));



    }

    
    public void toHomePage(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    
    public void toJournal(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Journal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

   

}

