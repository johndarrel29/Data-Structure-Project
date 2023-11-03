package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import alert.AlertMaker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.DataStored;
import model.Database;
import model.ProgressModel;
import model.ProgressModelManager;
import model.StudentID;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class HomeController implements Initializable{

    @FXML
    private Label quoteLabel, lastname_label, givenname_label, program_label, id_label;

    @FXML
    private Button journalButton, taskButton, logout_btn, accountButton, submit_account;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Text displayUsername, dateLabel;

    @FXML
    private TextField LastNameField, GivenNameField, StudentIDField, ProgramField;

    private ProgressModel progressModel;
    


    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    
        // Font customFont = Font.loadFont(getClass().getResource("/fonts/AlfaSlabOne-Regular.ttf").toExternalForm(), 82);
        // Font customFont2 = Font.loadFont(getClass().getResource("/fonts/Shrikhand-Regular.ttf").toExternalForm(), 57);
        // Font customFont3 = Font.loadFont(getClass().getResource("/fonts/AlfaSlabOne-Regular.ttf").toExternalForm(), 20);
        // Font customFont4 = Font.loadFont(getClass().getResource("/fonts/AlfaSlabOne-Regular.ttf").toExternalForm(), 20);
        // Font customFont5 = Font.loadFont(getClass().getResource("/fonts/AlfaSlabOne-Regular.ttf").toExternalForm(), 20);
        // Font customFont6 = Font.loadFont(getClass().getResource("/fonts/AlfaSlabOne-Regular.ttf").toExternalForm(), 20);
       
        // dateLabel.setFont(customFont);
        // displayUsername.setFont(customFont2);
        // accountButton.setFont(customFont3);
        // taskButton.setFont(customFont4);
        // journalButton.setFont(customFont5);
        // logout_btn.setFont(customFont6);

        progressModel = ProgressModelManager.getSharedProgressModel();

        if (progressModel != null) {
            progressBar.progressProperty().bind(progressModel.progressProperty());
            progressIndicator.progressProperty().bind(progressModel.progressProperty());
        } else {
            System.out.println("BAT WALANG LAMANG TO");
        }

        setRandomQuote();
        updateDateLabel();
        displayUser();
    
        
    }

    


//-------Display user---------------

public void displayUser(){
    //display user
    String user = DataStored.username;
        displayUsername.setText(user.substring(0, 1).toUpperCase() + user.substring(1));
       

    StudentID student = getStudentData();

    String lastName = student.getLastName();
    if (lastName != null && !lastName.isEmpty()) {
        lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
    }
    lastname_label.setText(lastName);

    String givenName = student.getGivenName();
    if (givenName != null && !givenName.isEmpty()) {
        String[] givenNameParts = givenName.split(" ");
        StringBuilder givenNameFormatted = new StringBuilder();

        for (String part : givenNameParts) {
            if (!part.isEmpty()) {
                if (givenNameFormatted.length() > 0) {
                    givenNameFormatted.append(" ");
                }
                givenNameFormatted.append(part.substring(0, 1).toUpperCase() + part.substring(1));
            }
        }
        givenname_label.setText(givenNameFormatted.toString());
    } else {
        givenname_label.setText("");
    }

    
    id_label.setText(student.getIDNumber());

    String program = student.getProgram();
    if (program != null && !program.isEmpty()) {
        program = program.toUpperCase();
    }
    program_label.setText(program);
    
    
    
    
}
//--- QUOTE OF THE DAY-------------------------------------------------------------

public void setRandomQuote() {
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

//---------------------------Date Label----------------------------------//
public void updateDateLabel() {
  
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMMM dd");

    String formattedDate = dateFormat.format(new Date());
  
    dateLabel.setText(formattedDate);
    
}



public StudentID getStudentData() {
    StudentID student = new StudentID();

    try (Connection connection = Database.DBConnect()) {
        String query = "SELECT * FROM studentID WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, DataStored.username);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            student.setLastName(resultSet.getString("LastName"));
            student.setGivenName(resultSet.getString("GivenName"));
            student.setIDNumber(resultSet.getString("IDNumber"));
            student.setProgram(resultSet.getString("program"));
            // You can set other properties as well
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return student;
}


//--------------------BUTTONS-------------------------------------

    public void toAccount(ActionEvent event) throws IOException {
        // Parent root = FXMLLoader.load(getClass().getResource("/view/Account.fxml"));
        // Scene scene = new Scene(root);
        // Stage stage = new Stage();
        // stage.setScene(scene);
        // stage.initStyle(StageStyle.UTILITY);
        // stage.show();

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Account.fxml"));
        Parent root = loader.load();
        AccountController accountController = loader.getController();
        accountController.setHomeController(this);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }   

    
    public void gotoJournal(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Journal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    
    public void gotoTask(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TaskView.fxml"));
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