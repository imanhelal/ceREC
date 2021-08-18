package eg.cu.fci.is.correlator.events;

public class FailCorrelation {
	
	public long eventID;
	public String activity;
	public long timestamp; 
    public long initTime;
    public long sysTime;
    
	public FailCorrelation(String activity, long timestamp, long initTime) {
		this.activity = activity;
        this.timestamp = timestamp;
        this.initTime = initTime;	
        this.sysTime = System.currentTimeMillis();
    }
 
	public FailCorrelation( long eventID, String activity, long timestamp, long initTime) {
		this.eventID = eventID;	//a serial that is read from the original file
        this.activity = activity;
        this.timestamp = timestamp;
        this.initTime = initTime;	
        this.sysTime = System.currentTimeMillis();
    }
	
    public long getEventID() {
        return eventID;
    }
    
    
    public long getTimestamp() {
        return timestamp;
    }

    public String getActivity() {
        return activity;
    }
    
    public long getInitTime() {
        return initTime;
    }
    
    public long getSysTime() {
        return sysTime;
    }

}
