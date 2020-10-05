package eg.cu.fci.is.correlator.events;

public class FailCorrelation {

	public String activity;
	public long timestamp; 
		
	public FailCorrelation( String activity, long timestamp) {
		
        this.activity = activity;
        this.timestamp = timestamp;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public String getActivity() {
        return activity;
    }

}
