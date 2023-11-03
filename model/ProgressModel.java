package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class ProgressModel {
    private final DoubleProperty progressProperty = new SimpleDoubleProperty();

    public DoubleProperty progressProperty() {
        return progressProperty;
    }

    public double getProgress() {
        return progressProperty.get();
    }

    public void setProgress(double progress) {
        progressProperty.set(progress);
    }
}

