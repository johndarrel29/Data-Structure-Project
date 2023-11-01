package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;

import alert.AlertMaker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
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
    private Button InserttaskButton, homeButton, journalButton, UndoButton, RedoButton, deleteButton;

    @FXML
    private TextArea taskInput;

    @FXML
    TableView<UserTask> mondayTaskTable, tuesdayTaskTable, wednesdayTaskTable, 
    thursdayTaskTable, fridayTaskTable, saturdayTaskTable;

    @FXML
    TableView<UserTask> doneTaskTable;

    @FXML
    TableColumn<UserTask, String> Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;

    TableColumn<UserTask, Boolean> completedColumn; // Add the "Completed" column

    TableColumn<UserTask, String> retrieveColumn;

    TableColumn<UserTask, String> doneList;

    // Separate "Completed" columns for each day
    TableColumn<UserTask, Boolean> mondayCompletedColumn;
    TableColumn<UserTask, Boolean> tuesdayCompletedColumn;
    TableColumn<UserTask, Boolean> wednesdayCompletedColumn;
    TableColumn<UserTask, Boolean> thursdayCompletedColumn;
    TableColumn<UserTask, Boolean> fridayCompletedColumn;
    TableColumn<UserTask, Boolean> saturdayCompletedColumn;

    // Create separate retrieveColumns for each TableView
    TableColumn<UserTask, String> retrieveColumnMonday = createRetrieveColumn();
    TableColumn<UserTask, String> retrieveColumnTuesday = createRetrieveColumn();
    TableColumn<UserTask, String> retrieveColumnWednesday = createRetrieveColumn();
    TableColumn<UserTask, String> retrieveColumnThursday = createRetrieveColumn();
    TableColumn<UserTask, String> retrieveColumnFriday = createRetrieveColumn();
    TableColumn<UserTask, String> retrieveColumnSaturday = createRetrieveColumn();

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

        completedColumn = createCompletedColumn();

        // Initialize the "Completed" columns for each day
        mondayCompletedColumn = createCompletedColumn();
        tuesdayCompletedColumn = createCompletedColumn();
        wednesdayCompletedColumn = createCompletedColumn();
        thursdayCompletedColumn = createCompletedColumn();
        fridayCompletedColumn = createCompletedColumn();
        saturdayCompletedColumn = createCompletedColumn();

        // Add the "Completed" columns to their respective TableView 
        mondayTaskTable.getColumns().add(mondayCompletedColumn);
        tuesdayTaskTable.getColumns().add(tuesdayCompletedColumn);
        wednesdayTaskTable.getColumns().add(wednesdayCompletedColumn);
        thursdayTaskTable.getColumns().add(thursdayCompletedColumn);
        fridayTaskTable.getColumns().add(fridayCompletedColumn);
        saturdayTaskTable.getColumns().add(saturdayCompletedColumn);

         // Set the TableView to be editable (ito yung para maging clickable yung checkbox)
        mondayTaskTable.setEditable(true);
        tuesdayTaskTable.setEditable(true);
        wednesdayTaskTable.setEditable(true);
        thursdayTaskTable.setEditable(true);
        fridayTaskTable.setEditable(true);
        saturdayTaskTable.setEditable(true);

        // Add the retrieveColumns to their respective TableView
        mondayTaskTable.getColumns().add(retrieveColumnMonday);
        tuesdayTaskTable.getColumns().add(retrieveColumnTuesday);
        wednesdayTaskTable.getColumns().add(retrieveColumnWednesday);
        thursdayTaskTable.getColumns().add(retrieveColumnThursday);
        fridayTaskTable.getColumns().add(retrieveColumnFriday);
        saturdayTaskTable.getColumns().add(retrieveColumnSaturday);
        
        // Initialize separate retrieveColumns for each TableView
        retrieveColumnMonday = createRetrieveColumn();
        retrieveColumnTuesday = createRetrieveColumn();
        retrieveColumnWednesday = createRetrieveColumn();
        retrieveColumnThursday = createRetrieveColumn();
        retrieveColumnFriday = createRetrieveColumn();
        retrieveColumnSaturday = createRetrieveColumn();

        try {
            showTaskList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        UndoButton.setDisable(true);
        RedoButton.setDisable(true);
    }


    private TableColumn<UserTask, Boolean> createCompletedColumn() {
    TableColumn<UserTask, Boolean> completedColumn = new TableColumn<>("Completed");
    completedColumn.setCellValueFactory(cellData -> cellData.getValue().completedProperty());

    completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));
    completedColumn.setEditable(true);

    completedColumn.setOnEditCommit(event -> {
        UserTask task = event.getRowValue();
        task.setCompleted(event.getNewValue());
        
    });

    return completedColumn;
}

    

// Modify the createRetrieveColumn method to create a new TableColumn
    // for each TableView
    private TableColumn<UserTask, String> createRetrieveColumn() {
        TableColumn<UserTask, String> retrieveColumn = new TableColumn<>("Retrieve");
        retrieveColumn.setCellValueFactory(new PropertyValueFactory<>("Task"));
        retrieveColumn.setCellFactory(column -> {
            return new TableCell<UserTask, String>() {
                final Button retrieveButton = new Button("Confirm");
    
                {
                    retrieveButton.setOnAction(event -> {
                        UserTask task = getTableView().getItems().get(getIndex());
    
                        // Check if the "Completed" property (checkbox) is selected
                        if (task.isCompleted()) {
                            // Implement the logic to retrieve data from the task column
                            String taskDescription = task.getTask();
                            System.out.println("Retrieve data for task: " + taskDescription);
    
                            // Call the deletetask method to delete the retrieved data
                            deletetask(task, getTableView());
                        } else {
                            // Display a message or take appropriate action if the checkbox is not checked
                            System.out.println("Checkbox is not checked. Cannot retrieve data.");
                        }
                    });
                }
    
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(retrieveButton);
                    }
                }
            };
        });
    
        return retrieveColumn;
    }
        
    
    


    // Rest of your code remains the same...


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

                redoStack.clear();

                showTaskList();

              
                UndoButton.setDisable(false);
                RedoButton.setDisable(true);
                }   
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Display an error message if the input is empty
            AlertMaker.showSimpleAlert("Input Error", "Input Cannot be Blanked.");
        }
    }
    

    //---------------------DELETE TASK LNG TO ------------------------------------- 

    public void deleteSelectedTask() {
        TableView<UserTask> selectedTableView = getSelectedTableView();
        if (selectedTableView != null) {
            UserTask selectedTask = selectedTableView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                // Delete the selected task
                deletetask(selectedTask, selectedTableView);
            } else {
                AlertMaker.showSimpleAlert("Oops!", "Select a task to delete.");
            }
        } else {
            AlertMaker.showSimpleAlert("Oops!", "Select a task and a day to delete.");
        }
    }


    public void deletetask(UserTask selectedTask, TableView<UserTask> selectedTableView) {
        try (Connection connection = Database.DBConnect()) {
            String deleteQuery = "DELETE FROM taskinput WHERE Task = ? AND Username = ? AND Day = ?";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setString(1, selectedTask.getTask());
            statement.setString(2, userAccount);
            statement.setString(3, getDayFromTableView(selectedTableView));
    
            int rowsAffected = statement.executeUpdate();
    
            if (rowsAffected > 0) {
                // Successfully deleted the task from the database
    
                // Push a memento onto the undoStack
                undoStack.push(new UserTaskMemento(selectedTask.getTask(), userAccount, getDayFromTableView(selectedTableView), false));
    
                // Remove the task from the TaskList
                TaskList.remove(selectedTask);
    
                // Clear the redo stack, as a new action is performed
                redoStack.clear();
    
                // Disable the "Redo" button when a delete is performed
                RedoButton.setDisable(true);
    
                // Refresh the task table view
                showTaskList();
    
                // Disable the "Undo" button when the undoStack is empty
                UndoButton.setDisable(undoStack.isEmpty());
            } else {
                // Task deletion was not successful, display an error message
                AlertMaker.showSimpleAlert("Error", "Failed to delete the task.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private TableView<UserTask> getSelectedTableView() {
        if (mondayTaskTable.getSelectionModel().getSelectedItem() != null) {
            return mondayTaskTable;
        } else if (tuesdayTaskTable.getSelectionModel().getSelectedItem() != null) {
            return tuesdayTaskTable;
        } else if (wednesdayTaskTable.getSelectionModel().getSelectedItem() != null) {
            return wednesdayTaskTable;
        } else if (thursdayTaskTable.getSelectionModel().getSelectedItem() != null) {
            return thursdayTaskTable;
        } else if (fridayTaskTable.getSelectionModel().getSelectedItem() != null) {
            return fridayTaskTable;
        } else if (saturdayTaskTable.getSelectionModel().getSelectedItem() != null) {
            return saturdayTaskTable;
        } else {
            return null;
        }
    }

    
    

    private String getDayFromTableView(TableView<UserTask> selectedTableView) {
        if (selectedTableView == mondayTaskTable) {
            return "Monday";
        } else if (selectedTableView == tuesdayTaskTable) {
            return "Tuesday";
        } else if (selectedTableView == wednesdayTaskTable) {
            return "Wednesday";
        } else if (selectedTableView == thursdayTaskTable) {
            return "Thursday";
        } else if (selectedTableView == fridayTaskTable) {
            return "Friday";
        } else if (selectedTableView == saturdayTaskTable) {
            return "Saturday";
        } else {
            return "";
        }
    }
    
    //------------------DELETE TASK LNG TO ---------------------------------------

    
    

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
                        TaskList.add(new UserTask(memento.getTask(), " ", false));
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
                        TaskList.add(new UserTask(memento.getTask(), " ", false));
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
    








    //--------------DISPLAY OF TASKS----------------------------------

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
                UT = new UserTask(result.getString("Task"), result.getString("Day"), false);
                DataList.add(UT);
            }
            System.out.println("Retrieved " + DataList.size() + " tasks for " + day);
        }
        return DataList;
    }

    public void showTaskList() throws SQLException {
    try (Connection connection = Database.DBConnect()) {
        String selectedDay = DaysChoices.getValue(); // Get the selected day
        if (selectedDay != null && !selectedDay.isEmpty()) {
            TaskList = dataTaskList(connection, selectedDay);
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
        }
    } catch (SQLException e) {
        e.printStackTrace();
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