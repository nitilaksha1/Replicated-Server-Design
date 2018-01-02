package requests;

//Main Request Class that will define the basic structure of a request from the client
public class Request {
	private TimeStamp 	timestamp;
	private String 	reqName;
	private int ackCount;
	private String reqID;

	public Request (String name, TimeStamp timestamp, int ackCount, String reqID) {
		reqName = name;
		this.timestamp = timestamp;
		this.ackCount = ackCount;
		this.reqID = reqID;
	}

	public String getReqName () {

		return reqName;
	}

	public int getAckCount() {

		return ackCount;
	}

	public void incrementActCount() {
		ackCount++;
	}

	public void setAckCount(int count) {

		ackCount = count;
	}

	public TimeStamp getTimestamp() {
		return timestamp;
	}

	public String getReqID() {
		return reqID;
	}
	
}