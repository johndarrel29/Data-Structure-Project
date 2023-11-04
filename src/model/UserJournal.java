package model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserJournal {
    private final StringProperty journalText;
    private final StringProperty title;
    private BooleanProperty completed;

    public UserJournal(String journalText, String title, boolean completed) {
        this.journalText = new SimpleStringProperty(journalText);
        this.title = new SimpleStringProperty(title);
        this.completed = new SimpleBooleanProperty(completed);
    }

    public String getJournalText() {
        return journalText.get();
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty journalTextProperty() {
        return journalText;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setJournalText(String journalText) {
        this.journalText.set(journalText);
    }

    public void setTitle(String title) {
        this.title.set(title);
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
}