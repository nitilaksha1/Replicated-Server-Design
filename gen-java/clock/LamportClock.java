package clock;

//This class provides methods to manage and maintain the lamport clock for every process
public class LamportClock {

	private int clockvalue;

	public LamportClock () {
		clockvalue = 0;
	}

	public void SetClockValueForSend () {
		clockvalue += 1;
	}

	public void SetClockValueForReceive (TimeStamp timestamp) {
		if (timestamp.getTimestamp() == 0) {

			clockvalue += 1;
			return;
		}

		if (timestamp.getTimestamp() > clockvalue)
			clockvalue = timestamp.getTimestamp() + 1;
		else
			clockvalue += 1;
	}

	public int getClockValue(){
		return clockvalue;
	}

}