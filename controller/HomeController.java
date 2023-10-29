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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.DataStored;

public class HomeController implements Initializable{

    @FXML
    private Label dateLabel, quoteLabel;

    @FXML
    private Button journalButton, taskButton, logout_btn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ProgressIndicator progressIndicator;



    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
      
        setRandomQuote();
        updateDateLabel();
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
  
    dateLabel.setText("Today is" + "\n" + formattedDate);
    
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
