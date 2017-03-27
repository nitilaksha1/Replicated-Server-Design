package clock;

public class LamportClock {
	
	private int clockvalue;

	public LamportClock () {
		clockvalue = 0;
	}

	public void SetClockValueForSend () {
		clockvalue += 1;
	}

	public void SetClockValueForReceive (TimeStamp timestamp) {
		if (timestamp == 0) {

			clockvalue += 1;
			return;
		}

		if (timestamp > clockvalue)
			clockvalue = timestamp + 1;
		else
			clockvalue += 1;
	}
	
	public int getClockValue(){
		return clockvalue;
	}
	
}
