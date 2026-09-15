package server.model.history;

public class Action {

    private String description;
    private long timestamp;

    public Action(String description) {
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    public String getDescription(){
        return description;
    }

    public String getTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}
