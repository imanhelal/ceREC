/**
 * 
 */
package eg.cu.fci.is.correlator.events;

/**
 * @author imanh
 *
 */
public class TempEvent {
	public long eventID;
	public long caseID;
	public String activity;
	public long timestamp; 
    public long sysTime;

	public TempEvent(long caseID, String activity, long timestamp) {
		this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
        this.sysTime = System.currentTimeMillis();
    }
	
	public TempEvent(long eventID, long caseID, String activity, long timestamp) {
		this.eventID = eventID;	//a serial that is read from the original file
    	this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
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
    
    public long getSysTime() {
        return sysTime;
    }

    
}
