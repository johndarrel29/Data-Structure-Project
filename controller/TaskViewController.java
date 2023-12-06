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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DataStored;
import model.Database;
import model.ProgressModel;
import model.ProgressModelManager;
import model.UserTask;
import model.UserTaskMemento;

public class TaskViewController implements Initializable {

    @FXML
    private ChoiceBox<String> DaysChoices;

    @FXML
    private Button InserttaskButton, homeButton, journalButton, UndoButton, RedoButton, deleteButton, weeklyDone;

    @FXML
    private TextField taskInput;

    @FXML
    private Text completed_tasks;

    @FXML
    ProgressBar progressBar;

    @FXML
    ProgressIndicator progressIndicator;

    @FXML
    private ImageView animatedLogo;
    
    private RotateTransition rotateTransition;
    private Timeline logoFlipTimeline;
    
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

    private DoubleProperty progressProperty = new SimpleDoubleProperty();

    Stack<UserTaskMemento> undoStack = new Stack<>();
    Stack<UserTaskMemento> redoStack = new Stack<>();

    private TableColumn<UserTask, ?> doneListColumn;

    private static ProgressModel progressModel; // Make it a static variable

    

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    Font customFont11 = Font.loadFont(getClass().getResource("/fonts/AlfaSlabOne-Regular.ttf").toExternalForm(), 17);
    completed_tasks.setFont(customFont11);

// Animation---------------------------------------
        rotateTransition = new RotateTransition(Duration.seconds(2), animatedLogo);
        rotateTransition.setAxis(Rotate.Y_AXIS); 
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(180);

        logoFlipTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0), new KeyValue(animatedLogo.rotateProperty(), 0)),
            new KeyFrame(Duration.seconds(5), event -> {
               rotateTransition.playFromStart();
            })
        );
        logoFlipTimeline.setCycleCount(Timeline.INDEFINITE);
        logoFlipTimeline.play();

// -----------------------------------------------
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

        doneListColumn = new TableColumn<>();
        doneListColumn.setCellValueFactory(new PropertyValueFactory<>("Task"));

        // Add the "doneList" column to the doneTaskTable
        doneTaskTable.getColumns().add(doneListColumn);

        try {
            showTaskList();
            loadCompletedTasks();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        progressModel = ProgressModelManager.getSharedProgressModel();

        progressBar.progressProperty().bind(progressModel.progressProperty());
        progressIndicator.progressProperty().bind(progressModel.progressProperty());

        progressBar.progressProperty().bind(progressProperty);
        progressIndicator.progressProperty().bind(progressBar.progressProperty());

        
        updateProgressBar();

        UndoButton.setDisable(true);
        RedoButton.setDisable(true);
    }

    private void updateProgressBar() {
        int totalCompletedTasks = 0;
        int totalRemainingTasks = 0;
    
        totalCompletedTasks += getCompletedTaskCountFromDatabase("Monday");
        totalRemainingTasks += getTotalTaskCountFromDatabase("Monday");

    
        totalCompletedTasks += getCompletedTaskCountFromDatabase("Tuesday");
        totalRemainingTasks += getTotalTaskCountFromDatabase("Tuesday");


        totalCompletedTasks += getCompletedTaskCountFromDatabase("Wednesday");
        totalRemainingTasks += getTotalTaskCountFromDatabase("Wednesday");


        totalCompletedTasks += getCompletedTaskCountFromDatabase("Thursday");
        totalRemainingTasks += getTotalTaskCountFromDatabase("Thursday");


        totalCompletedTasks += getCompletedTaskCountFromDatabase("Friday");
        totalRemainingTasks += getTotalTaskCountFromDatabase("Friday");


        totalCompletedTasks += getCompletedTaskCountFromDatabase("Saturday");
        totalRemainingTasks += getTotalTaskCountFromDatabase("Saturday");

    
        if (totalCompletedTasks == 0 && totalRemainingTasks == 0) {
            progressProperty.set(0);
            progressModel.setProgress(0);
        } else {
            double progress = (double) totalCompletedTasks / (totalCompletedTasks + totalRemainingTasks);
            System.out.println("Total Completed Tasks: " + totalCompletedTasks);
            System.out.println("Total Remaining Tasks: " + totalRemainingTasks);
            System.out.println("Progress: " + progress);
            progressProperty.set(progress);
            progressModel.setProgress(progress);
        }
    }
    


    private int getCompletedTaskCountFromDatabase(String day) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        int completedTaskCount = 0;
    
        try {
            connection = Database.DBConnect();
            String retrieveData = "SELECT COUNT(*) FROM completed_tasks WHERE Username = ? AND Day = ?";
            statement = connection.prepareStatement(retrieveData);
            statement.setString(1, userAccount);
            statement.setString(2, day);  // Set the day parameter
            result = statement.executeQuery();
    
            if (result.next()) {
                completedTaskCount = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the reverse order of their creation (result, statement, connection)
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    
        return completedTaskCount;
    }
    
    private int getTotalTaskCountFromDatabase(String day) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        int totalTaskCount = 0;
    
        try {
            connection = Database.DBConnect();
            String retrieveData = "SELECT COUNT(*) FROM taskinput WHERE Username = ? AND Day = ?";
            statement = connection.prepareStatement(retrieveData);
            statement.setString(1, userAccount);
            statement.setString(2, day);  // Set the day parameter
            result = statement.executeQuery();
    
            if (result.next()) {
                totalTaskCount = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources in the reverse order of their creation (result, statement, connection)
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    
        return totalTaskCount;
    }

    
    // Getter method to access the progressModel
    public static ProgressModel getProgressModel() {
        return progressModel;
    }




    public void refreshTableAndResetProgress() {
        deleteCompletedTasksForCurrentWeek();
        resetProgressBar(); 
        doneTaskTable.getItems().clear();
    }
    

    public void deleteCompletedTasksForCurrentWeek() {
        try (Connection connection = Database.DBConnect()) {
            String deleteQuery = "DELETE FROM completed_tasks WHERE Username = ? AND Day IN " +
                "('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday')";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setString(1, userAccount);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetProgressBar() {
        progressProperty.set(0);
        progressModel.setProgress(0);
    }

    


//------------------------------FOR COMPLETED TASK----------------------------------

    private TableColumn<UserTask, Boolean> createCompletedColumn() {
    TableColumn<UserTask, Boolean> completedColumn = new TableColumn<>("Completed");
    completedColumn.setCellValueFactory(cellData -> cellData.getValue().completedProperty());

    completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));
    completedColumn.setEditable(true);

    completedColumn.setOnEditCommit(event -> {
        UserTask task = event.getRowValue();
        task.setCompleted(event.getNewValue());
        updateProgressBar();
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
                        String day = getSelectedDay(); // Get the selected day
    
                        // Check if the "Completed" property (checkbox) is selected
                        if (task.isCompleted()) {
                            // Implement the logic to retrieve data from the task column
                            String taskDescription = task.getTask();
                            System.out.println("Retrieve data for task: " + taskDescription + " for day: " + day);
    
                            // Move the data to the `completed_tasks` database
                            moveDataToCompletedTasks(taskDescription, userAccount);
    
                            // Call the deletetask method to delete the retrieved data from the `taskinput` table
                            deletetask(task, getTableView());

                            updateProgressBar();

                            UndoButton.setDisable(true);
                            RedoButton.setDisable(true);
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
    
    private void updateDoneTaskTable() {
        try (Connection connection = Database.DBConnect()) {
            ObservableList<UserTask> completedTasks = dataCompletedTaskList(connection);
            doneTaskTable.setItems(completedTasks);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void moveDataToCompletedTasks(String taskDescription, String username) {
        try (Connection connection = Database.DBConnect()) {
            connection.setAutoCommit(false); // Set auto-commit to false
    
            // First, retrieve the day from the taskinput database
            String retrieveDayQuery = "SELECT Day FROM taskinput WHERE Task = ? AND Username = ?";
            PreparedStatement retrieveDayStatement = connection.prepareStatement(retrieveDayQuery);
            retrieveDayStatement.setString(1, taskDescription);
            retrieveDayStatement.setString(2, username);
    
            ResultSet dayResult = retrieveDayStatement.executeQuery();
            if (dayResult.next()) {
                String day = dayResult.getString("Day");
    
                // Now, insert the task into the completed_tasks database
                String insertCompletedTask = "INSERT INTO completed_tasks (Task, Username, Day) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(insertCompletedTask);
                statement.setString(1, taskDescription);
                statement.setString(2, username);
                statement.setString(3, day);
    
                int rowsAffected = statement.executeUpdate();
    
                if (rowsAffected > 0) {
                    connection.commit(); // Commit the transaction
                    System.out.println("Data moved to completed_tasks: " + taskDescription + " for day: " + day);
                    // Update the doneTaskTable
                    updateDoneTaskTable();
                } else {
                    System.out.println("Data was not moved to completed_tasks");
                }
            } else {
                System.out.println("Failed to retrieve day for the task: " + taskDescription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String getSelectedDay() {
        String selectedDay = DaysChoices.getValue(); // Get the selected day
        if (selectedDay != null && !selectedDay.isEmpty()) {
            return selectedDay;
        } else {
            return "";
        }
    }

    // Method to load data from the completed_tasks table
    private void loadCompletedTasks() throws SQLException {
        try (Connection connection = Database.DBConnect()) {
            ObservableList<UserTask> completedTasks = dataCompletedTaskList(connection);
            doneTaskTable.setItems(completedTasks);
        }
    }

    // Retrieve data from the completed_tasks table
    private ObservableList<UserTask> dataCompletedTaskList(Connection connection) throws SQLException {
        ObservableList<UserTask> completedTasks = FXCollections.observableArrayList();

        String retrieveData = "SELECT Task FROM completed_tasks WHERE Username = ?";
        PreparedStatement statement = connection.prepareStatement(retrieveData);
        statement.setString(1, userAccount);

        ResultSet result = statement.executeQuery();

        while (result.next()) {
            UserTask task = new UserTask(result.getString("Task"), "Completed", true);
            completedTasks.add(task);
        }

        System.out.println("Retrieved " + completedTasks.size() + " completed tasks.");

        return completedTasks;
    }

    //---------------------------COMPLETED TASK--------------------------------------
    

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

                updateProgressBar();

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

                updateProgressBar();
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

                updateProgressBar();
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

                updateProgressBar();
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