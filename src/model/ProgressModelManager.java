package model;

public class ProgressModelManager {
    private static ProgressModel sharedProgressModel;

    public static ProgressModel getSharedProgressModel() {
        if (sharedProgressModel == null) {
            sharedProgressModel = new ProgressModel(); // Initialize it here or using a factory method
        }
        return sharedProgressModel;
    }
}
