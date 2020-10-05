/**
 * 
 */
package eg.cu.fci.is.correlator.events;

/**
 * @author imanh
 *
 */
public class TempEvent {
	public long caseID;
	public String activity;
	public long timestamp; 
		
	public TempEvent(long caseID, String activity, long timestamp) {
    	this.caseID = caseID;
        this.activity = activity;
        this.timestamp = timestamp;
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

    
}
