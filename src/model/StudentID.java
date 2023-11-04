package model;

public class StudentID {
    public  String LastName;
    public  String GivenName;
    public  String IDNumber;
    public  String program;

    // public StudentID(String LastName, String GivenName, String IDNumber, String program) {
       
    //     this.LastName = LastName;
    //     this.GivenName = GivenName;
    //     this.IDNumber = IDNumber;
    //     this.program = program;
    // }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String LastName) {
        this.LastName = LastName;
    }

    public String getGivenName() {
        return GivenName;
    }

    public void setGivenName(String GivenName) {
        this.GivenName = GivenName;
    }

    public String getIDNumber() {
        return IDNumber;
    }

    public void setIDNumber(String IDNumber) {
        this.IDNumber = IDNumber;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
}