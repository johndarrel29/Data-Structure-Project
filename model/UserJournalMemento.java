package model;

public class UserJournalMemento{
    private String journal;
    private String title;

    public UserJournalMemento(String title, String journal) {
        this.journal = journal;
        this.title = title;
    }

    public UserJournalMemento(String journalText, String insertJournal, String title, String username) {
    }

   

    public String getJournal() {
        return journal;
    }

    public String getTitle() {
        return null;
    }

    
}
