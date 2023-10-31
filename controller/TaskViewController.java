package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.Stack;

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
    private Button InserttaskButton, homeButton, journalButton, UndoButton, RedoButton;

    @FXML
    private TextArea taskInput;

    @FXML
    TableView<UserTask> mondayTaskTable, tuesdayTaskTable, wednesdayTaskTable, 
    thursdayTaskTable, fridayTaskTable, saturdayTaskTable;

    @FXML
    TableColumn<UserTask, String> Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;

    String userAccount = DataStored.username;
    
    ObservableList<UserTask> TaskList;

    Stack<UserTaskMemento> undoStack = new Stack<>();
    Stack<UserTaskMemento> redoStack = new Stack<>();


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

        UndoButton.setDisable(true);
        RedoButton.setDisable(true);

    }



    public void insertTask() {
    String selectedDay = DaysChoices.getValue(); // Get the selected day
    String taskText = taskInput.getText().trim();
 
    if (!selectedDay.equals("Choose a day") && !taskText.isEmpty()) {
        try (Connection connection = Database.DBConnect()) {
            String task = "SELECT Task FROM taskinput WHERE Task = ? AND Username = ? AND Day = ?";
            PreparedStatement statement = connection.prepareStatement(task);
            statement.setString(1, taskInput.getText());
            statement.setString(2, userAccount);
            statement.setString(3, selectedDay);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                AlertMaker.showSimpleAlert("Notifications", "Task is already existing");
            } else {
                String insertTask = "INSERT INTO taskinput (Task, Username, Day) VALUES (?, ?, ?)";
                statement = connection.prepareStatement(insertTask);
                statement.setString(1, taskInput.getText());
                statement.setString(2, userAccount);
                statement.setString(3, selectedDay);
                statement.executeUpdate();  

                // Save the current state for possible undo action
                undoStack.push(new UserTaskMemento(taskInput.getText(), DataStored.username, selectedDay, true));

                // Clear the redo stack, as a new action is performed
                redoStack.clear();

                showTaskList();

                // Enable the "Undo" button when a new task is inserted
                UndoButton.setDisable(false);
                // Disable the "Redo" button when a new action is performed
                RedoButton.setDisable(true);
                }   
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Display an error message if the input is empty
            AlertMaker.showSimpleAlert("Input Error", "Please enter a non-empty task before inserting.");
        }
    }



    //Retrieval of data from xampp
    public ObservableList<UserTask> dataTaskList(Connection connection, String day) throws SQLException {
        ObservableList<UserTask> DataList = FXCollections.observableArrayList();
        String selectedDay = DaysChoices.getValue();
        if (selectedDay != null && !selectedDay.isEmpty()) {
            String retrieveData = "SELECT * FROM taskinput WHERE Username = ? AND Day = ?";
            PreparedStatement statement = connection.prepareStatement(retrieveData);
            statement.setString(1, userAccount);
            statement.setString(2, day);
            ResultSet result = statement.executeQuery();
            UserTask UT;
            while (result.next()) {
                UT = new UserTask(result.getString("Task"), result.getString("Day"));
                DataList.add(UT);
            }
            System.out.println("Retrieved " + DataList.size() + " tasks for " + day);
        }
        return DataList;
    }
    

    public void showTaskList() throws SQLException {
        
        try (Connection connection = Database.DBConnect()) {

            TaskList = dataTaskList(connection, "");
            Monday.setCellValueFactory(new PropertyValueFactory<>("Task"));
            Tuesday.setCellValueFactory(new PropertyValueFactory<>("Task"));
            Wednesday.setCellValueFactory(new PropertyValueFactory<>("Task"));
            Thursday.setCellValueFactory(new PropertyValueFactory<>("Task"));
            Friday.setCellValueFactory(new PropertyValueFactory<>("Task"));
            Saturday.setCellValueFactory(new PropertyValueFactory<>("Task"));
            mondayTaskTable.setItems(dataTaskList(connection, "Monday"));
            tuesdayTaskTable.setItems(dataTaskList(connection, "Tuesday"));
            wednesdayTaskTable.setItems(dataTaskList(connection, "Wednesday"));
            thursdayTaskTable.setItems(dataTaskList(connection, "Thursday"));
            fridayTaskTable.setItems(dataTaskList(connection, "Friday"));
            saturdayTaskTable.setItems(dataTaskList(connection, "Saturday"));


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public void undo() throws SQLException {
        if (!undoStack.isEmpty()) {
            UserTaskMemento memento = undoStack.pop();
            if (DataStored.username.equals(memento.getUserAccount())) {
                redoStack.push(memento);

                if (memento.isInsert()) {
                    // If it was originally an insert, delete the task from the database
                    try (Connection connection = Database.DBConnect()) {
                        String deleteQuery = "DELETE FROM taskinput WHERE Task = ? AND Username = ? AND Day = ?";
                        PreparedStatement statement = connection.prepareStatement(deleteQuery);
                        statement.setString(1, memento.getTask());
                        statement.setString(2, DataStored.username);
                        statement.setString(3, memento.getDay());
                        statement.executeUpdate();
                    }

                    // Remove the task from TaskList
                    TaskList.removeIf(task -> task.getTask().equals(memento.getTask()));
                } else {
                    // If it was originally a delete, insert the task back into the database
                    try (Connection connection = Database.DBConnect()) {
                        String insertQuery = "INSERT INTO taskinput (Task, Username, Day) VALUES (?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(insertQuery);
                        statement.setString(1, memento.getTask());
                        statement.setString(2, DataStored.username);
                        statement.setString(3, memento.getDay());
                        statement.executeUpdate();
                    }

                    // Check if the task is not already in the list before adding it
                    if (TaskList.stream().noneMatch(task -> task.getTask().equals(memento.getTask()))) {
                        TaskList.add(new UserTask(memento.getTask(), " "));
                    }
                }

                try (Connection connection = Database.DBConnect()) {
                    mondayTaskTable.setItems(dataTaskList(connection, "Monday"));
                    tuesdayTaskTable.setItems(dataTaskList(connection, "Tuesday"));
                    wednesdayTaskTable.setItems(dataTaskList(connection, "Wednesday"));
                    thursdayTaskTable.setItems(dataTaskList(connection, "Thursday"));
                    fridayTaskTable.setItems(dataTaskList(connection, "Friday"));
                    saturdayTaskTable.setItems(dataTaskList(connection, "Saturday"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                UndoButton.setDisable(undoStack.isEmpty());
                RedoButton.setDisable(false);
            }
        }
    }

    
    public void redo() throws SQLException {
        if (!redoStack.isEmpty()) {
            UserTaskMemento memento = redoStack.pop();
            if (DataStored.username.equals(memento.getUserAccount())) {
                undoStack.push(memento);
    
                try (Connection connection = Database.DBConnect()) {
                    if (memento.isInsert()) {
                        // If it was originally an insert, insert the task back into the database
                        String insertQuery = "INSERT INTO taskinput (Task, Username, Day) VALUES (?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(insertQuery);
                        statement.setString(1, memento.getTask());
                        statement.setString(2, DataStored.username);
                        statement.setString(3, memento.getDay());
                        statement.executeUpdate();
    
                        // Update the TaskList with the newly added task
                        TaskList.add(new UserTask(memento.getTask(), " "));
                    } else {
                        // If it was originally a delete, delete the task from the database
                        String deleteQuery = "DELETE FROM taskinput WHERE Task = ? AND Username = ? AND Day = ?";
                        PreparedStatement statement = connection.prepareStatement(deleteQuery);
                        statement.setString(1, memento.getTask());
                        statement.setString(2, DataStored.username);
                        statement.setString(3, memento.getDay());
                        statement.executeUpdate();
    
                        // Remove the task from TaskList
                        TaskList.removeIf(task -> task.getTask().equals(memento.getTask()));
                    }
    
                    // Set the table view items
                    mondayTaskTable.setItems(dataTaskList(connection, "Monday"));
                    tuesdayTaskTable.setItems(dataTaskList(connection, "Tuesday"));
                    wednesdayTaskTable.setItems(dataTaskList(connection, "Wednesday"));
                    thursdayTaskTable.setItems(dataTaskList(connection, "Thursday"));
                    fridayTaskTable.setItems(dataTaskList(connection, "Friday"));
                    saturdayTaskTable.setItems(dataTaskList(connection, "Saturday"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
    
                UndoButton.setDisable(false);
                RedoButton.setDisable(redoStack.isEmpty());
            }
        }
    }
    





//---------------------Buttons-------------------------------------
    
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

