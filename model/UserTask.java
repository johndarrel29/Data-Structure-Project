package model;

public class UserTask {
    String task;

    public UserTask (String task){
        this.task = task;
    }

    public String getTask (){
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public UserTaskMemento saveToMemento() {
        return new UserTaskMemento(task);
    }

    public void restoreFromMemento(UserTaskMemento memento) {
        task = memento.getTask();
    }

}
