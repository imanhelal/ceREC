package eg.cu.fci.is.correlator.events;

public class LabeledEvent {

	public long eventID;
    public long caseID;
    public String activity;
    public long timestamp; // you may want to switch that to date time but usually timestamp in long is easier
    public double probability;
    public long sysTime;
    
    public LabeledEvent(long caseID, String activity, long timestamp, double probability) {
    	this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.probability = probability;	
        this.sysTime = System.currentTimeMillis();
    }
    
    public LabeledEvent(long eventID, long caseID, String activity, long timestamp, double probability) {
    	this.eventID = eventID;	//a serial that is read from the original file
    	this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.probability = probability;	
        this.sysTime = System.currentTimeMillis();
    }
    
    public long getEventID() {
        return eventID;
    }
    
    public long getCaseID() {
        return caseID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getActivity() {
        return activity;
    }

    public double getProbability() {
        return probability;
    }
    
    public long getSysTime() {
        return sysTime;
    }

}
