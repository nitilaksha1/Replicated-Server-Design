package requests;

public class BalanceRequest extends Request {
	private int accountUID;

	public BalanceRequest (String name, int accUID, TimeStamp timestamp, int count, String requestID) {
		super(name, timestamp, count, requestID);
		accountUID = accUID;
	}

	public int getAccUID () {
		return accountUID;
	}
}
