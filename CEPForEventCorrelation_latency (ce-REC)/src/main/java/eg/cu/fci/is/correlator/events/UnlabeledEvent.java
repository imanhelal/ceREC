package eg.cu.fci.is.correlator.events;

public class UnlabeledEvent {

	public long eventID;
    public long caseID;
    public String activity;
    public long timestamp;
    public double probability;	// needed for Union later
    public long sysTime;
    
    public UnlabeledEvent(long caseID, String activity, long timestamp, double probability) {
    	this.caseID = caseID;	//given -1 to indicate null value
        this.activity = activity;
        this.timestamp = timestamp;
        this.probability = probability;  //given -1 to indicate null value	
        this.sysTime = System.currentTimeMillis();
    }
    
    public UnlabeledEvent(long eventID, long caseID, String activity, long timestamp, double probability) {
    	this.eventID = eventID;	//a serial that is read from the original file
    	this.caseID = caseID;	//given -1 to indicate null value
        this.activity = activity;
        this.timestamp = timestamp;
        this.probability = probability;  //given -1 to indicate null value	
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
