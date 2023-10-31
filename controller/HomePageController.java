package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.Stack;
// import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import alert.AlertMaker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomePageController implements Initializable {


    @FXML
    Button logoutButton, taskButton, UndoButton, RedoButton, deleteButton;

    @FXML
    Label displayUsername;

    @FXML
    TextArea task_Input;

    @FXML
    TableView<UserTask> displayTaskTable;

    @FXML
    TableView<UserTask> doneTaskTable;

    @FXML
    TableColumn<UserTask, String> userTaskList;
    TableColumn<UserTask, String> doneList;

    @FXML
    ProgressBar progressBar;

   @FXML
    ProgressIndicator progressIndicator;

    @FXML
    private Label dateLabel;

    @FXML
    private Label quoteLabel;

    ObservableList<UserTask> TaskList;

    Stack<UserTaskMemento> undoStack = new Stack<>();
    Stack<UserTaskMemento> redoStack = new Stack<>();
    

    Statement statement;
    Connection connect;


    //ready for designing
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayUser();
        
    
        doneList = new TableColumn<>("Completed Tasks"); // Initialize doneList
        try {
            showTaskList(userTaskList);
            doneList.setCellValueFactory(new PropertyValueFactory<>("task"));
            doneTaskTable.getColumns().setAll(doneList);
            doneTaskTable.setItems(retrieveCompletedTasks());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        UndoButton.setDisable(true);
        RedoButton.setDisable(true);
        setupDoneTaskTable();
        updateProgress();
        updateDateLabel();
        setRandomQuote();
    }
    
   // date --------------------------------------------------------------------

   private void updateDateLabel() {
  
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMM dd");

    String formattedDate = dateFormat.format(new Date());
  
    dateLabel.setText("Today is" + "\n" + formattedDate);
    
}

//--- QUOTE OF THE DAY-------------------------------------------------------------

private void setRandomQuote() {
    try {
        URL url = new URL("https://zenquotes.io/api/today"); 
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonArray = objectMapper.readTree(response.toString());

            if (jsonArray.isArray() && jsonArray.size() > 0) {
                String quote = jsonArray.get(0).get("q").asText();
                quoteLabel.setText(quote);
            } else {
                quoteLabel.setText("Failed to fetch a quote.");
            }
        } else {
            quoteLabel.setText("Failed to fetch a quote. HTTP response code: " + responseCode);
        }
    } catch (IOException e) {
        e.printStackTrace();
        quoteLabel.setText("Failed to fetch a quote: " + e.getMessage());
    }
}



    //----------------------------METHODS for display------------------------------------------
    
    private void setupDoneTaskTable() {
    }

    public void displayUser(){
        //display user
        String user = DataStored.username;
            displayUsername.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
            
    }

        
    public void showTaskList(TableColumn<UserTask, String> tableColumn) throws SQLException {
    TaskList = dataTaskList();

    userTaskList.setCellValueFactory(new PropertyValueFactory<>("Task"));

    TableColumn<UserTask, Boolean> completedColumn = new TableColumn<>("Completed");
    completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
    completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));

    TableColumn<UserTask, UserTask> retrieveColumn = new TableColumn<>("Confirm");
    retrieveColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    retrieveColumn.setCellFactory(cell -> new TableCell<UserTask, UserTask>() {
        final Button retrieveButton = new Button("Confirm");

        {
            retrieveButton.setOnAction(event -> {
                UserTask userTask = getTableRow().getItem();
                if (userTask != null && userTask.isCompleted()) {
                    String task = userTask.getTask();
                    System.out.println("Retrieving data for task: " + task);
                    moveTaskToDone(userTask); // Call a new method to handle moving the task
                } else {
                    System.out.println("Cannot retrieve data because the checkbox is not selected.");
                }
            });
        }

        @Override
        protected void updateItem(UserTask userTask, boolean empty) {
            super.updateItem(userTask, empty);
            if (userTask == null || empty) {
                setGraphic(null);
            } else {
                setGraphic(retrieveButton);
            }
        }
    });

    displayTaskTable.getColumns().setAll(tableColumn, completedColumn, retrieveColumn);
    displayTaskTable.setItems(TaskList);
}

    private void moveTaskToDone(UserTask userTask) {
        if (userTask != null) {
            // Remove the task from the 'TaskList' (displayTaskTable)
        TaskList.remove(userTask);

            // Insert the task into the 'completed_tasks' table
        try {
            connect = Database.DBConnect();
            statement = connect.createStatement();
            String insertQuery = "INSERT INTO completed_tasks (Task, Admin) VALUES ('" + userTask.getTask() + "', '" + DataStored.username + "')";
            statement.executeUpdate(insertQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

            // Remove the task from the 'usertask' table
        deleteTaskFromDatabase(userTask.getTask());

            // Add the task to the 'doneTaskTable'
        doneTaskTable.getItems().add(userTask);

            // Refresh the displayTaskTable
        displayTaskTable.setItems(TaskList);

        updateProgress();
          
        }
    }

       // progress bar --------------------------------------------------------------------------------

    private void updateProgress() {
        int totalTasks = TaskList.size();  // Total number of tasks
        int completedTasks = doneTaskTable.getItems().size();  // Number of completed tasks
    
        // Ensure you are not dividing by zero (if totalTasks is zero, set progress to 0)
        double progress = (totalTasks == 0) ? 0 : (double) completedTasks / totalTasks;
    
        progressBar.setProgress(progress);
        progressIndicator.setProgress(progress);
    }
//----------------------------------------------------------------------------------
    private void deleteTaskFromDatabase(String task) {
        try {
            connect = Database.DBConnect();
            statement = connect.createStatement();
            String deleteQuery = "DELETE FROM usertask WHERE Task = '" + task + "' AND Admin = '" + DataStored.username + "'";
            statement.executeUpdate(deleteQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayTaskTable.setItems(TaskList);

         updateProgress();
     
    }


    public void showCompletedTasks(TableColumn<UserTask, String> tableColumn) throws SQLException {
        // Load completed tasks from the "completed_tasks" table
        ObservableList<UserTask> completedTaskList = dataCompletedTaskList();

        tableColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        doneTaskTable.getColumns().setAll(tableColumn);
        doneTaskTable.setItems(completedTaskList);
    }

    public ObservableList<UserTask> dataCompletedTaskList() throws SQLException {
        ObservableList<UserTask> completedTaskList = FXCollections.observableArrayList();

        String retrieveData = "SELECT * FROM completed_tasks WHERE Admin = '" + DataStored.username + "'";
        connect = Database.DBConnect();
        statement = connect.createStatement();

        try {
            ResultSet result = statement.executeQuery(retrieveData);

            UserTask UT;

            while (result.next()) {
                UT = new UserTask(result.getString("Task"), " ");

                completedTaskList.add(UT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return completedTaskList;
    }

    public ObservableList<UserTask> retrieveCompletedTasks() {
        ObservableList<UserTask> completedTasks = FXCollections.observableArrayList();

        try {
            connect = Database.DBConnect();
            statement = connect.createStatement();

            String selectQuery = "SELECT Task FROM completed_tasks WHERE Admin = '" + DataStored.username + "'";
            ResultSet resultSet = statement.executeQuery(selectQuery);

            while (resultSet.next()) {
                String task = resultSet.getString("Task");
                completedTasks.add(new UserTask(task, " "));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return completedTasks;
    }


    


    //---------------------------------------------------------------------------------------
    

    //gumana na..pwede na ulit mag input si user paulit ulit
    public void insertTask()throws SQLException{

        connect = Database.DBConnect();
        statement = connect.createStatement();

        String task = "SELECT Task FROM usertask WHERE Task = '" + task_Input.getText() + "' AND Admin = '" + DataStored.username + "'";;
        
        ResultSet result = statement.executeQuery(task);
        
        if (result.next()) {
            AlertMaker.showSimpleAlert("Notifications", "Task is already existing");
        } else {
            String insertTask = "INSERT INTO usertask (Task, Admin) VALUES ('" + task_Input.getText() + "', '" +  DataStored.username + "')";
            statement.executeUpdate(insertTask);
            
            //save the current state for possible undo action
            undoStack.push(new UserTaskMemento(task_Input.getText(), DataStored.username, "", true));
           
            //clear the redo stack, as a new action is performed
            redoStack.clear();

            showTaskList(userTaskList);

            // Enable the "Undo" button when a new task is inserted
            UndoButton.setDisable(false);
            // Disable the "Redo" button when a new action is performed
            RedoButton.setDisable(true);
            deleteButton.setDisable(false);
        }

    }

    public void deletetask(ActionEvent event) throws SQLException {
        UserTask selectedTask = displayTaskTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            connect = Database.DBConnect();
            statement = connect.createStatement();

            // Create a backup of the deleted task for undo
            UserTaskMemento memento = new UserTaskMemento(selectedTask.getTask(), DataStored.username, "", false);
            
            String deleteQuery = "DELETE FROM usertask WHERE Task = '" + selectedTask.getTask() + "'";
            
            try {
                statement.executeUpdate(deleteQuery);

                //kapag nag redo tsaka lng bumabalik yung task after it was deleted because the undo button was supposed to delete only.
                //We need to make the undo button add the deleted task again
                //wag kalimutan tooo!!!!!
                
                // Push the memento onto the undo stack
                undoStack.push(memento);
                // Clear the redo stack, as a new action is performed
                redoStack.clear();

                 // Disable the "Undo" button when the undoStack is empty
                UndoButton.setDisable(undoStack.isEmpty());

                // Enable the "Redo" button when a delete is performed
                RedoButton.setDisable(true);

                showTaskList(userTaskList); 
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            AlertMaker.showSimpleAlert("Ooops!", "Select a task to delete.");
        }
    }

    //undo for task
    //KAPAG NAG UNDO AND REDO NANG PAULIT ULIT..DI NA GUMAGANA
    public void undo() throws SQLException {
        if (!undoStack.isEmpty()) {
            UserTaskMemento memento = undoStack.pop();
            if (DataStored.username.equals(memento.getUserAccount())) {
                redoStack.push(memento);
    
                if (memento.isInsert()) {
                    // If it was originally an insert, delete the task from the database
                    connect = Database.DBConnect();
                    statement = connect.createStatement();
                    String deleteQuery = "DELETE FROM usertask WHERE Task = '" + memento.getTask() + "' AND Admin = '" + DataStored.username + "'";
                    statement.executeUpdate(deleteQuery);
    
                    // Remove the task from TaskList
                    TaskList.removeIf(task -> task.getTask().equals(memento.getTask()));
                } else {
                    // If it was originally a delete, insert the task back into the database
                    connect = Database.DBConnect();
                    statement = connect.createStatement();
                    String insertQuery = "INSERT INTO usertask (Task, Admin) VALUES ('" + memento.getTask() + "', '" + DataStored.username + "')";
                    statement.executeUpdate(insertQuery);
    
                    // Check if the task is not already in the list before adding it
                    if (TaskList.stream().noneMatch(task -> task.getTask().equals(memento.getTask()))) {
                        TaskList.add(new UserTask(memento.getTask(), " "));
                    }
                }
    
                displayTaskTable.refresh();
                UndoButton.setDisable(undoStack.isEmpty());
                RedoButton.setDisable(false);
             
            }
        }
    }
    
    

    //redo for task
    public void redo() throws SQLException {
        if (!redoStack.isEmpty()) {
            UserTaskMemento memento = redoStack.pop();
            if (DataStored.username.equals(memento.getUserAccount())) {
                undoStack.push(memento);
                connect = Database.DBConnect();
                statement = connect.createStatement();
    
                if (memento.isInsert()) {
                    // If it was originally an insert, insert the task back into the database
                    String insertQuery = "INSERT INTO usertask (Task, Admin) VALUES ('" + memento.getTask() + "', '" + DataStored.username + "')";
                    statement.executeUpdate(insertQuery);
    
                    // Update the TaskList with the newly added task
                    TaskList.add(new UserTask(memento.getTask(), " "));
                } else {
                    // If it was originally a delete, delete the task from the database
                    String deleteQuery = "DELETE FROM usertask WHERE Task = '" + memento.getTask() + "' AND Admin = '" + DataStored.username + "'";
                    statement.executeUpdate(deleteQuery);
    
                    // Remove the task from TaskList
                    TaskList.removeIf(task -> task.getTask().equals(memento.getTask()));
                }
    
                displayTaskTable.refresh();
                UndoButton.setDisable(false);
                RedoButton.setDisable(redoStack.isEmpty());
            }
        }
    }
    
    


    //Retrieval of data from xampp
    public ObservableList<UserTask> dataTaskList() throws SQLException{
        ObservableList<UserTask> DataList = FXCollections.observableArrayList();
    
        String retrieveData = "SELECT * FROM usertask WHERE Admin = '" + DataStored.username + "'";
        connect = Database.DBConnect();
        statement = connect.createStatement();

        try {
            ResultSet result = statement.executeQuery(retrieveData);

            UserTask UT;

            while (result.next()) {
                UT = new UserTask(result.getString("Task"), " ");

                DataList.add(UT);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return DataList;

    }


    public void toJournal(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Journal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    public void logout(ActionEvent event)throws IOException{

        System.out.println("Logout method called");

        Stage stage = (Stage) (((Node) event.getSource()).getScene().getWindow());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignIn.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        DataStored.clearUsername();

        stage.show();

    }




}