package model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.beans.property.BooleanProperty;

public class UserTask {
    private String task;
    private String admin;
    private String day;
    private BooleanProperty completed; // Boolean property for the checkbox
    private Button retrieveButton;
    // private BooleanProperty completed = new SimpleBooleanProperty(false);

    public UserTask(String task, String day, boolean completed) {
        this.task = task;
        this.retrieveButton = new Button("Retrieve");
        this.day = day;
        this.completed = new SimpleBooleanProperty(completed);

         // Set an action for the "Retrieve" button
         this.retrieveButton.setOnAction(event -> {
            retrieveData();
        });
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day){
        this.day = day;
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    public UserTaskMemento saveToMemento(boolean isInsert) {
        return new UserTaskMemento(task, admin, isInsert, isCompleted());
    }

    public void restoreFromMemento(UserTaskMemento memento) {
        task = memento.getTask();
        setCompleted(memento.isCompleted());
    }

    public Object selectedProperty() {
        return null;
    }

   // Other getters and setters

   public Button getRetrieveButton() {
    return retrieveButton;
}

public void retrieveData() {
    // Implement your data retrieval logic here
    // You can use the 'task' variable to identify the task for which you want to retrieve data
    // This method will be called when the "Retrieve" button is clicked
}

}