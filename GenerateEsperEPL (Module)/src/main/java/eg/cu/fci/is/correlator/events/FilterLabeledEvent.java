package eg.cu.fci.is.correlator.events;

import java.util.Date;

public class FilterLabeledEvent {
	
	public long eventID;
    public long caseID;
    public String activity;
    public long timestamp; // you may want to switch that to date time but usually timestamp in long is easier
    public double probability;
    public long initTime;
    public long sysTime;
    
    public FilterLabeledEvent(long caseID, String activity, long timestamp, double probability, long initTime) {
    	this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.probability = probability;
        this.initTime = initTime;	
        this.sysTime = System.currentTimeMillis();
    }

    public FilterLabeledEvent(long eventID, long caseID, String activity, long timestamp, double probability, long initTime) {
    	this.eventID = eventID;	//a serial that is read from the original file
    	this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.probability = probability;
        this.initTime = initTime;	
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
    
    public long getInitTime() {
        return initTime;
    }
    
    public long getSysTime() {
        return sysTime;
    }

}
