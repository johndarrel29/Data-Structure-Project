package model;

public class UserTask {
    String task;
    String admin;

    public UserTask (String task){
        this.task = task;
    }

    public String getTask (){
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public UserTaskMemento saveToMemento(boolean isInsert) {
        return new UserTaskMemento(task, admin, isInsert);
    }

    public void restoreFromMemento(UserTaskMemento memento) {
        task = memento.getTask();
    }

}
