package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UserJournal {
    private final StringProperty journalText;
    private final StringProperty title;

    public UserJournal(String journalText, String title, String string) {
        this.journalText = new SimpleStringProperty(journalText);
        this.title = new SimpleStringProperty(title);
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
}