import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.TException;
import java.util.*;
import java.io.*;
import clock.*;
import java.util.Comparator;
import java.util.PriorityQueue;


//Main Request Class that will define the basic structure of a request from the client
class Request {
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

//Different parameters specified as per the types of the request
class DepositRequest extends Request {
	private int accountUID;
	private int amount;

	public DepositRequest (String name, int accUID, int amt, TimeStamp timestamp, int count, String requestID) {

		super(name, timestamp, count, requestID);

		this.accountUID = accUID;

		if (amt < 0)
			this.amount = 0;
		else
			this.amount = amt;
	}

	public int getAccUID () {
		return accountUID;
	}

	public int getAmount() {
		return amount;
	}
}

class BalanceRequest extends Request {
	private int accountUID;

	public BalanceRequest (String name, int accUID, TimeStamp timestamp, int count, String requestID) {
		super(name, timestamp, count, requestID);
		accountUID = accUID;
	}

	public int getAccUID () {
		return accountUID;
	}
}

class TransferRequest extends Request {
	private int sourceAccUID;
	private int targetAccUID;
	private int amount;

	public TransferRequest (String name, int srcAccUID, int targAccUID, int amt, TimeStamp timestamp, int count, String requestID) {
		super(name, timestamp, count, requestID);
		sourceAccUID = srcAccUID;
		targetAccUID = targAccUID;

		if (amt < 0)
			amount = 0;
		else
			amount = amt;
	}

	public int getSourceAccUID () {
		return sourceAccUID;
	}

	public int gettargetAccUID () {
		return targetAccUID;
	}

	public int getAmount () {
		return amount;
	}
}

class ServerInfo{
      public int portnumber;
      public String hostname;

      public ServerInfo(int port, String host){
          portnumber = port;
          hostname = host;
      }
}

//This class is used in conjunction with the priority queue to impose ordering based on time stamps
class TimeStampComparator implements Comparator<Request>
{
	@Override
	public int compare(Request x, Request y)
	{
		int res = -1;

		TimeStamp xtimestamp = x.getTimestamp();
		TimeStamp ytimestamp = y.getTimestamp();

		// Assume neither string is null. Real code should
		// probably be more robust
		// You could also just return x.length() - y.length(),
		// which would be more efficient.
		if (xtimestamp.getTimestamp() < ytimestamp.getTimestamp())
		{
			res = -1;
		}
		else if (xtimestamp.getTimestamp() > ytimestamp.getTimestamp())
		{
			res = 1;
		}
		else if (xtimestamp.getTimestamp() == ytimestamp.getTimestamp()) {
			if (xtimestamp.getServerid() > ytimestamp.getServerid())
				res = 1;
			else
				res = -1;
		}

		return res;
	}
}

//This class provides methods to manage and maintain the lamport clock for every process
class LamportClock {

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

//This class implements the Service Interface defined by thrift
public class ReplicatedServerHandler implements ReplicatedBankService.Iface {
	
	private LamportClock clock;
	private volatile PriorityQueue<Request> reqQueue;
	private TimeStampComparator comparator = new TimeStampComparator();
	private HashMap<Integer, ServerInfo> servermap;
	private ArrayList<ServerInfo> serverlist;
	private int id;
	private int nodeCount;
	private BankHandler bankhandler;
	private volatile int reqID;
	private PrintWriter writer;
	private long totalresponsetime;
	private volatile long reqcount;
	private ArrayList<Integer> accountList;

	public ReplicatedServerHandler () {

		clock = new LamportClock();
		reqQueue = new PriorityQueue<Request>(100, comparator);
		bankhandler = new BankHandler();
		serverlist = new ArrayList<>();
		servermap = new HashMap<>();
		nodeCount = 0;
		reqcount = 0;
		totalresponsetime = 0;
		reqID = 0;
		accountList = new ArrayList<>();
	}

	public void setNodeCountAndList (String filename) throws FileNotFoundException{

		Scanner scan = new Scanner (new File(filename));
		scan.nextLine();
		
		while (scan.hasNext()) {
			String hostname = scan.next();
			int id = scan.nextInt();
			int portnumber = scan.nextInt();
			
			servermap.put(id, new ServerInfo(portnumber, hostname));

			if (id != this.id)
				serverlist.add(new ServerInfo(portnumber, hostname));

			nodeCount += 1;
				
		}

		scan.close();
	}

	public void setAccountList(ArrayList<Integer> serverList){

		accountList = serverList;
	}

	public ArrayList<Integer> getAccountList(){

		return accountList;
	}

	public void initLogFile (String filename) throws FileNotFoundException {

		writer = new PrintWriter (filename + "_" + getID());
	}

	public void closeLogFile () {

		writer.close();
	}
	
    public void setID(int id){

		this.id = id;
    }

    public int getID(){

		return this.id;
    }
		
	@Override
	public String multi_deposit (int uID, int amount, TimeStamp timestamp, int serverid) {

		//Meant for future implementation
		return "NA";
	}

	@Override
	public int multi_getBalance (int uID, TimeStamp timestamp, int serverid) {

		clock.SetClockValueForReceive(timestamp);
		clock.SetClockValueForSend();
		BalanceRequest balreq = new BalanceRequest ("BalanceRequest", uID, new TimeStamp(0,0), 0, "dummy");
		reqQueue.add((Request)balreq);

		//TODO: Check if condition for checking if head node has the same timestamp is required
		BalanceRequest balreq1 = (BalanceRequest)reqQueue.remove();
		int uid = balreq1.getAccUID();
		
		int res = bankhandler.getBalance(uid);

		return res;

	}

	@Override
	public void stop_execution(TimeStamp timestamp){

		synchronized (clock){
			clock.SetClockValueForReceive(timestamp);
		}
		int sum=0;
		ArrayList<Integer> list = getAccountList();
		for(int i =0 ; i < list.size(); i++){
			int bal = bankhandler.getBalance(list.get(i));
			sum = sum + bal;
			System.out.println("Account id: " + list.get(i) + " Balance: " + bal);

		}

		System.out.println("Total Balance :  "+ sum);

		System.out.println("Total number of Client Requests = " + reqcount);

		long resptime = 0;

		if (reqcount == 0) {
			System.out.println("Average response time = " + resptime);
		} else {
			System.out.println("Average response time = " + (totalresponsetime/reqcount));
		}

		writer.println("The pending requests in the Queue: ");

		Object[] requestArray;

		synchronized (reqQueue) {

			requestArray = reqQueue.toArray();

			for (int i = 0; i < reqQueue.size(); i++) {
				TransferRequest req = (TransferRequest) requestArray[i];
				writer.println("Request Id: " + req.getReqID());
			}
		}

		System.exit(0);

	}

	@Override
	public void halt() {

		try{
			for (int i = 0; i < nodeCount - 1; i++) {
				String hostname = serverlist.get(i).hostname;
				int portnumber = serverlist.get(i).portnumber;

				TTransport transport;
				transport = new TSocket(hostname, portnumber);
				transport.open();

				TProtocol protocol = new  TBinaryProtocol(transport);

				ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);
				clock.SetClockValueForSend();
				client.stop_execution(new TimeStamp(clock.getClockValue(),getID()));

				//System.out.println("serverid: " + "0" + " multi_halt complete");
				transport.close();

			}

		} catch (TTransportException e) {

		} catch (TException e) {

		}

		int sum = 0;
		ArrayList<Integer> list = getAccountList();
		for(int i =0 ; i < list.size(); i++){
			int bal = bankhandler.getBalance(list.get(i));
			sum = sum + bal;
			System.out.println("Account id: " + list.get(i) + " Balance: " + bal);
		}

		System.out.println("Total Balance :  "+ sum);
		System.out.println("Total number of Client Requests = " + reqcount);

		long resptime = 0;

		if (reqcount == 0) {
			System.out.println("Average response time = " + reqcount);
		} else {
			System.out.println("Average response time = " + (totalresponsetime/reqcount));
		}

		writer.println("The pending requests in the Queue: ");

		Object[] requestArray;

		synchronized (reqQueue) {

			requestArray = reqQueue.toArray();

			for (int i = 0; i < reqQueue.size(); i++) {
				TransferRequest req = (TransferRequest) requestArray[i];
				writer.println("Request Id: " + req.getReqID());
			}
		}

		System.exit(0);

	}

	//This is a dedicated function to handle the requests sent from another server
	//serverID is used for getting the value of sender server
	@Override
	public void multi_transfer_server (int uID, int targuID, int amount, TimeStamp timestamp, int serverid, String requestID, long clientid) {
			//System.out.println("Multicast received");
			TransferRequest transreq;

			synchronized (reqQueue){

				writer.println(clientid + "	SRV_REQ		" + serverid + "	" + System.currentTimeMillis() + "	[" + clock.getClockValue() + "," + getID() + "]" + "	Transfer Operation" + "	Source ID: " + uID + "	Target ID: " + targuID + "	Amount " + amount);
				writer.flush();
				transreq = new TransferRequest("TransferRequest", uID, targuID, amount, timestamp, 0, requestID);
				reqQueue.add((Request)transreq);
				clock.SetClockValueForReceive(timestamp);
			}

			ServerInfo info = servermap.get(serverid);
			String hostname = info.hostname;
			int portnumber = info.portnumber;

			try {
				TTransport transport;

				transport = new TSocket(hostname, portnumber);
				transport.open();

				TProtocol protocol = new TBinaryProtocol(transport);
				ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);

				synchronized (clock) {
					clock.SetClockValueForSend();
				}

				client.multi_transfer_ack(requestID, id, new TimeStamp(clock.getClockValue(),getID()));

			}catch(TTransportException e){}
			 catch(TException e){}

		    //System.out.println("Multicast done");

			while(true){
				synchronized (reqQueue){
					TransferRequest temp = (TransferRequest) reqQueue.peek();
					try {

						while (!temp.getReqID().equals(requestID) || (temp.getAckCount() != nodeCount - 1) ){
							//System.out.println("Head of the queue: "+ temp.getReqID());
							//System.out.println("Current thread request: "+requestID);
							//System.out.println("Head Ack count: "+ temp.getAckCount());
							reqQueue.wait();
							temp = (TransferRequest) reqQueue.peek();
						}

					}catch(InterruptedException e){}

					TransferRequest headReq = (TransferRequest) reqQueue.remove();

					int src = headReq.getSourceAccUID();
					int target = headReq.gettargetAccUID();
					int amt = headReq.getAmount();

					String res = bankhandler.transfer(src,target,amt);
					reqQueue.notifyAll();
					break;
				}
			}
	}


	//This is a dedicated function to handle the requests sent from the clients
	@Override
	public String multi_transfer (int uID, int targuID, int amount, TimeStamp timestamp, int serverid, long clientid) {

		long starttime = System.currentTimeMillis();
		//System.out.println("Inside multi_transfer of server: " + serverid);
		String currentRequest;
		TransferRequest transreq;
		String res = "Failed";

		synchronized (reqQueue) {
			clock.SetClockValueForReceive(timestamp);
			writer.println(clientid + "	CLIENT_REQ	" + System.currentTimeMillis() + "	[" + clock.getClockValue() + "," + getID() + "]" + "	Transfer Operation" + "	Source ID: " + uID + "	Target ID: " + targuID + "	Amount " + amount);
			writer.flush();
			reqID += 1;
			reqcount += 1;
			currentRequest = getID() + "_" + reqID;
			transreq = new TransferRequest("TransferRequest", uID, targuID, amount, new TimeStamp(clock.getClockValue(),getID()), 0, currentRequest);
			reqQueue.add((Request) transreq);
		}
		try{
				for (int i = 0; i < nodeCount - 1; i++) {
					String hostname = serverlist.get(i).hostname;
					int portnumber = serverlist.get(i).portnumber;

					TTransport transport;
					transport = new TSocket(hostname, portnumber);
					transport.open();

					TProtocol protocol = new  TBinaryProtocol(transport);

					ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);
					clock.SetClockValueForSend();
					client.multi_transfer_server(uID, targuID, amount, new TimeStamp(clock.getClockValue(),getID()) , id, currentRequest, clientid);

					//System.out.println("serverid: " + serverid + " multi_transfer complete");
					transport.close();

					}
		} catch (TTransportException e) {

				} catch (TException e) {

		}

		while(true){
			synchronized (reqQueue){

				TransferRequest temp = (TransferRequest) reqQueue.peek();
				try {

					while (!temp.getReqID().equals(currentRequest) || (temp.getAckCount() != nodeCount - 1) ){
						reqQueue.wait();
						//System.out.println("Head of the queue: "+ temp.getReqID());
						//System.out.println("Current thread request: "+currentRequest);
						//System.out.println("Head Ack count: "+ temp.getAckCount());
						temp = (TransferRequest) reqQueue.peek();
					}

				}catch(InterruptedException e){}

				TransferRequest headReq = (TransferRequest) reqQueue.remove();
				writer.println(clientid + "	PROCESS	" + System.currentTimeMillis() + "	[" + clock.getClockValue() + "," + getID() + "]");
				writer.flush();
				int src = headReq.getSourceAccUID();
				int target = headReq.gettargetAccUID();
				int amt = headReq.getAmount();

				res = bankhandler.transfer(src,target,amt);

				try{
					for (int i = 0; i < nodeCount - 1; i++) {
						String hostname = serverlist.get(i).hostname;
						int portnumber = serverlist.get(i).portnumber;

						TTransport transport;
						transport = new TSocket(hostname, portnumber);
						transport.open();

						TProtocol protocol = new  TBinaryProtocol(transport);

						ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);
						clock.SetClockValueForSend();
						client.release(currentRequest);

						//System.out.println("serverid: " + serverid + " multi_transfer complete");
						transport.close();

					}
				} catch (TTransportException e) {

				} catch (TException e) {

				}

				reqQueue.notifyAll();
				break;
			}
		}
		//System.out.println("Client response sent");
		long endtime = System.currentTimeMillis();
		totalresponsetime += (endtime - starttime);

		return res;
	}


	@Override
	public void multi_deposit_ack(TimeStamp reqtimeStamp, int serverid) {
		//Meant for future implementation
	}

	@Override
	public void multi_getBalance_ack (TimeStamp reqTimeStamp, int serverid) {
		//Meant for future implementation
	}

	@Override
	public void multi_transfer_ack	(String requestID, int serverid, TimeStamp timestamp) {
		//System.out.println("Ack received");

		Object[] requestArray;

		synchronized (reqQueue) {
//			clock.SetClockValueForReceive(timestamp);
			requestArray = reqQueue.toArray();

			for (int i = 0; i < reqQueue.size(); i++) {
				TransferRequest req = (TransferRequest) requestArray[i];

				if (req.getReqID().equals(requestID)) {
					req.incrementActCount();
					//System.out.println("Iteration ack count: "+ req.getAckCount());

					if (req.getAckCount() == nodeCount - 1) {
						//System.out.println("Notified all");
						reqQueue.notifyAll();
					}

					break;
				}
			}
		}
	}

	@Override
	public void release (String requestID) {

		Object[] requestArray;

		synchronized (reqQueue) {
//			clock.SetClockValueForReceive(timestamp);
			requestArray = reqQueue.toArray();

			for (int i = 0; i < reqQueue.size(); i++) {
				TransferRequest req = (TransferRequest) requestArray[i];

				if (req.getReqID().equals(requestID)) {
					req.setAckCount(nodeCount - 1);
					reqQueue.notifyAll();

					break;
				}
			}
		}
	}
}
