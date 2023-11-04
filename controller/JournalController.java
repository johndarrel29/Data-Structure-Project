package controller;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Stack;

import alert.AlertMaker;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.DataStored;
import model.Database;
import model.UserJournal;
import model.UserJournalMemento;
import model.UserTask;
import model.UserTaskMemento;

public class JournalController implements Initializable {

    @FXML
    Button journalButton, delJournalButton, UndoButton1, RedoButton1, toTask, seeMoreButton;

    @FXML
    Label displayUsername1;

    @FXML
    TextArea journal_Input, journalTitle_Input;

    @FXML
    TableView<UserJournal> displayJournalTable;

    @FXML
    TableColumn<UserJournal, String> userJournalList, userJournalText;

    ObservableList<UserJournal> JournalList;

    Stack<UserJournalMemento> undoStack = new Stack<>();
    Stack<UserJournalMemento> redoStack = new Stack<>();

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Statement statement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayUser();
        
        try {
            showJournalList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        UndoButton1.setDisable(true);
        RedoButton1.setDisable(true);
    }

    public void displayUser() {
        String user = DataStored.username;
        displayUsername1.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
    }

    public void showJournalList() throws SQLException {
        JournalList = dataJournalList();
        userJournalList.setCellValueFactory(new PropertyValueFactory<UserJournal, String>("Title"));
        userJournalText.setCellValueFactory(new PropertyValueFactory<UserJournal, String>("JournalText"));
        displayJournalTable.setItems(JournalList);
    }

    // public void insertJournal() throws SQLException {
    //     connect = Database.DBConnect();
    //     String journalText = journal_Input.getText();
    //     String title = journalTitle_Input.getText();
    //     String username = DataStored.username;

    //     if (journalText.isEmpty() || title.isEmpty()) {
    //         AlertMaker.showSimpleAlert("Empty Fields", "Both Title and Journal Text are required.");
    //         return;
    //     }
    
    //     String insertJournal = "INSERT INTO journal (JournalText, Title, Username) VALUES (?,?,?)";
    //     try (PreparedStatement preparedStatement = connect.prepareStatement(insertJournal)) {
    //         preparedStatement.setString(1, journalText);
    //         preparedStatement.setString(2, title);
    //         preparedStatement.setString(3, username);
    //         preparedStatement.executeUpdate();
    
    //         showJournalList();
    //         myJournalShowData();
    
    //         undoStack.push(new UserJournalMemento(journalText, insertJournal, title, username));
    //         redoStack.clear();
    
    //         UndoButton1.setDisable(false);
    //         RedoButton1.setDisable(true);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    public void insertJournal() {
    String title = journalTitle_Input.getText().trim(); 
    String journaltext = journal_Input.getText().trim();
 
    if (!title.isEmpty() && !journaltext.isEmpty()) {
        try (Connection connection = Database.DBConnect()) {
            String insertQuery = "SELECT * FROM journal WHERE JournalText = ? AND Username = ?";
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, journaltext);
            statement.setString(2, DataStored.username);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                AlertMaker.showSimpleAlert("Notifications", "Journal is already existing");
            } else {
                String insertTask = "INSERT INTO journal (JournalText, Username, Title) VALUES (?, ?, ?)";
                statement = connection.prepareStatement(insertTask);
                statement.setString(1, journaltext);
                statement.setString(2, DataStored.username);
                statement.setString(3, title);
                statement.executeUpdate();  
                
                undoStack.push(new UserJournalMemento(title, journaltext, DataStored.username, true));

                redoStack.clear();

                showJournalList();
              
                UndoButton1.setDisable(false);
                RedoButton1.setDisable(true);

                }   
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Display an error message if the input is empty
            AlertMaker.showSimpleAlert("Input Error", "Input Cannot be Blanked.");
        }
    }


    // public void deleteJournal(ActionEvent event) throws SQLException {
    //     UserJournal selectedJournal = displayJournalTable.getSelectionModel().getSelectedItem();
    
    //     if (selectedJournal != null) {
    //         connect = Database.DBConnect();
    //         statement = connect.createStatement();
    
    //         String deleteQuery = "DELETE FROM journal WHERE Title = ? AND JournalText = ? AND Username = ?"; // Include Username
    
    //         try (PreparedStatement preparedStatement = connect.prepareStatement(deleteQuery)) {
    //             preparedStatement.setString(1, selectedJournal.getTitle());
    //             preparedStatement.setString(2, selectedJournal.getJournalText());
    //             preparedStatement.setString(3, DataStored.username); // Set the username
    //             preparedStatement.executeUpdate();
    //             showJournalList();
    
    //             // Push the deleted journal entry into the undoStack
    //             undoStack.push(new UserJournalMemento(selectedJournal.getJournalText(), selectedJournal.getTitle(), DataStored.username, deleteQuery));
    
    //             // Clear the redo stack
    //             redoStack.clear();
    
    //             // Enable the "Undo" button
    //             UndoButton1.setDisable(false);
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     } else {
    //         AlertMaker.showSimpleAlert("Ooops!", "Select a journal to delete.");
    //     }
    
    //     myJournalShowData();
    // }

    public void deleteJournal()throws SQLException{
        UserJournal selectedJournal = displayJournalTable.getSelectionModel().getSelectedItem();

        if (selectedJournal != null) {

            try (Connection connection = Database.DBConnect()) {
            String deleteQuery = "DELETE FROM journal WHERE Title = ? AND JournalText = ? AND Username = ?";
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setString(1, selectedJournal.getTitle());
            statement.setString(2, selectedJournal.getJournalText());
            statement.setString(3, DataStored.username);
    
            int rowsAffected = statement.executeUpdate();
    
            if (rowsAffected > 0) {
                // Successfully deleted the task from the database
    
                // Push a memento onto the undoStack
                undoStack.push(new UserJournalMemento(selectedJournal.getTitle(), selectedJournal.getJournalText(), DataStored.username, false));

                // Remove the task from the TaskList
                JournalList.remove(selectedJournal);
    
                // Clear the redo stack, as a new action is performed
                redoStack.clear();
    
                // Disable the "Redo" button when a delete is performed
                RedoButton1.setDisable(true);
    
                // Refresh the task table view
                showJournalList();
    
                // Disable the "Undo" button when the undoStack is empty
                UndoButton1.setDisable(undoStack.isEmpty());
            } else {
                // Task deletion was not successful, display an error message
                AlertMaker.showSimpleAlert("Error", "Failed to delete the task.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
        
    }
    
    
    // public void undo1() throws SQLException {
    //     if (!undoStack.isEmpty()) {
    //         UserJournalMemento memento = undoStack.pop();
    //         redoStack.push(memento);
    
    //         connect = Database.DBConnect();
    //         statement = connect.createStatement();
    //         String deleteQuery = "DELETE FROM journal WHERE Title = ? AND JournalText = ?";
            
    //         try (PreparedStatement preparedStatement = connect.prepareStatement(deleteQuery)) {
    //             preparedStatement.setString(1, memento.getTitle());
    //             preparedStatement.setString(2, memento.getJournal());
    //             preparedStatement.executeUpdate();
    
    //             JournalList.removeIf(journal -> 
    //                 journal.getTitle().equals(memento.getTitle()) &&
    //                 journal.getJournalText().equals(memento.getJournal())
    //             );
    
    //             displayJournalTable.refresh();
    //             UndoButton1.setDisable(undoStack.isEmpty());
    //             RedoButton1.setDisable(false);
    //             showJournalList();
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     }
    // }

    public void undo1() throws SQLException {
        if (!undoStack.isEmpty()) {
            UserJournalMemento memento = undoStack.pop();

            if (DataStored.username.equals(memento.getAccount())) {
                redoStack.push(memento);

                if (memento.isInsert()) {
                    // If it was originally an insert, delete the task from the database
                    try (Connection connection = Database.DBConnect()) {
                        String deleteQuery = "DELETE FROM journal WHERE Title = ? AND JournalText = ? AND Username = ?";
                        PreparedStatement statement = connection.prepareStatement(deleteQuery);
                        statement.setString(1, memento.getTitle());
                        statement.setString(2, memento.getJournal());
                        statement.setString(3, DataStored.username);
                        statement.executeUpdate();
                    }

                    // Remove the task from TaskList
                    JournalList.removeIf(journal -> journal.getJournalText().equals(memento.getJournal()));
                } else {
                    // If it was originally a delete, insert the task back into the database
                    try (Connection connection = Database.DBConnect()) {
                        String insertQuery = "INSERT INTO journal (JournalText, Username, Title) VALUES (?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(insertQuery);
                        statement.setString(1, memento.getJournal());
                        statement.setString(2, DataStored.username);
                        statement.setString(3, memento.getTitle());
                        statement.executeUpdate();
                    }

                    // Check if the task is not already in the list before adding it
                    if (JournalList.stream().noneMatch(journal -> journal.getJournalText().equals(memento.getJournal()))) {
                        JournalList.add(new UserJournal(memento.getJournal(), memento.getTitle(), false));
                    }
                }

                showJournalList();

                UndoButton1.setDisable(undoStack.isEmpty());
                RedoButton1.setDisable(false);

            }
        }
    }
    

    // public void redo1() throws SQLException {
    //     if (!redoStack.isEmpty()) {
    //         UserJournalMemento memento = redoStack.pop();
    //         undoStack.push(memento);
    
    //         connect = Database.DBConnect();
    //         String insertQuery = "INSERT INTO journal (Title, Username, JournalText) VALUES (?, ?, ?)";
            
    //         try (PreparedStatement preparedStatement = connect.prepareStatement(insertQuery)) {
    //             preparedStatement.setString(1, memento.getTitle());  // Set the 'Title' from the memento
    //             preparedStatement.setString(2, DataStored.username);
    //             preparedStatement.setString(3, memento.getJournal());
    //             preparedStatement.executeUpdate();
    
    //             JournalList.add(new UserJournal(memento.getTitle(), memento.getJournal(), insertQuery));
    //             displayJournalTable.refresh();
    
    //             UndoButton1.setDisable(false);
    //             RedoButton1.setDisable(redoStack.isEmpty());
    //             myJournalShowData();
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         }
    //     }
    // }

    public void redo1() throws SQLException {
        if (!redoStack.isEmpty()) {
            UserJournalMemento memento = redoStack.pop();
            if (DataStored.username.equals(memento.getAccount())) {
                undoStack.push(memento);
    
                try (Connection connection = Database.DBConnect()) {
                    if (memento.isInsert()) {
                        // If it was originally an insert, insert the task back into the database
                        String insertQuery = "INSERT INTO journal (JournalText, Username, Title) VALUES (?, ?, ?)";
                        PreparedStatement statement = connection.prepareStatement(insertQuery);
                        statement.setString(1, memento.getJournal());
                        statement.setString(2, DataStored.username);
                        statement.setString(3, memento.getTitle());
                        statement.executeUpdate();
    
                        // Update the TaskList with the newly added task
                        JournalList.add(new UserJournal(memento.getJournal(), memento.getTitle(), false));
                    } else {
                        // If it was originally a delete, delete the task from the database
                        String deleteQuery = "DELETE FROM journal WHERE Title = ? AND JournalText = ? AND Username = ?";
                        PreparedStatement statement = connection.prepareStatement(deleteQuery);
                        statement.setString(1, memento.getTitle());
                        statement.setString(2, memento.getJournal());
                        statement.setString(3, DataStored.username);
                        statement.executeUpdate();
    
                        // Remove the task from TaskList
                        JournalList.removeIf(journal -> journal.getJournalText().equals(memento.getJournal()));
                    }
    
                } catch (SQLException e) {
                    e.printStackTrace();
                }
    
                showJournalList();

                UndoButton1.setDisable(false);
                RedoButton1.setDisable(redoStack.isEmpty());

               
            }
        }
    }
    

    public void seeMore(ActionEvent event) {
    UserJournal selectedJournal = displayJournalTable.getSelectionModel().getSelectedItem();

    if (selectedJournal != null) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle(selectedJournal.getTitle());

        TextArea textArea = new TextArea(selectedJournal.getJournalText());
        textArea.setWrapText(true);
        textArea.setEditable(false);

        Scene scene = new Scene(new Group(textArea), 400, 300);
        popupStage.setScene(scene);
        popupStage.show();
    } else {
        AlertMaker.showSimpleAlert("Ooops!", "Select a title to view the journal text.");
    }
}

    public ObservableList<UserJournal> dataJournalList() {
        ObservableList<UserJournal> dataList = FXCollections.observableArrayList();
        String retrieveData = "SELECT * FROM journal WHERE Username = ?";
        connect = Database.DBConnect();
        try (PreparedStatement preparedStatement = connect.prepareStatement(retrieveData)) {
            preparedStatement.setString(1, DataStored.username);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                String title = result.getString("Title");
                String journalText = result.getString("JournalText");
                System.out.println("Title: " + title); // Add this debug print statement
                System.out.println("Text: " + journalText);
                UserJournal userJournal = new UserJournal(journalText, title, false);
                dataList.add(userJournal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public void ToTask(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void toHome(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void toTaskView(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


}
