package model;

public class UserJournalMemento{
    private String journal;
    private String title;
    private String account;
    private boolean isInsert;

    public UserJournalMemento(String title, String journal, String account, boolean isInsert) {
        this.journal = journal;
        this.title = title;
        this.account = account;
        this.isInsert = isInsert;
    }

   

    public String getJournal() {
        return journal;
    }

    public String getTitle() {
        return title;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public String getAccount(){
        return account;
    }

    
}