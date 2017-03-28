import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

class Servers{
      public int portnumber;
      public String hostname;
      public int serverid;

      public Servers(int port, String host, int serverid){
          portnumber = port;
          hostname = host;
          this.serverid = serverid;
      }
}

public class BankClient {
      private ArrayList<Integer> accountlist = new ArrayList<Integer>();
      public static Random rand = new Random();
      public static Object lock = new Object();
      public static ReplicatedBankService.Client client;
      public static boolean isUsed = false;
      public static ArrayList<Servers> serverlist = new ArrayList<>();
      public static List<Thread> th = new ArrayList<Thread>();
      ArrayList<Integer> getAccountList () 
      {
        return accountlist;
      }


      void transferRequest(long threadid, ReplicatedBankService.Client client, PrintWriter writer, int src, int target, int amount, int srvid) throws TException{

		System.out.println("Inside transferRequest");
        String s= client.multi_transfer(src,target,amount,new TimeStamp(0,-1), -1, threadid);
		System.out.println("Request Processed: Result: " + s);
        writer.println(threadid + " " + srvid + "   RSP   " + System.currentTimeMillis() + "  " + s);
        writer.flush();
      }

      public void listServers(String filename){
          try{     
            Scanner scan = new Scanner (new File(filename));
            scan.nextLine();
        
            while (scan.hasNext()) {
                String hostname = scan.next();
                int fid = scan.nextInt();
                int portnumber = scan.nextInt();
                serverlist.add(new Servers(portnumber,hostname, fid));

                
            }
            scan.close();
            }catch(FileNotFoundException e){}
            //Rest of server logic
        
      }

      public Servers getRandomServer(){
              
            return serverlist.get(rand.nextInt(serverlist.size())); 
      }


    public static void main(String[] args) {


      try{
            final BankClient bc = new BankClient();
            int threadCount = Integer.parseInt(args[0]);
            final int iterationCount = 100;
            final String filename = args[1];
            bc.listServers(filename);
            final PrintWriter writer = new PrintWriter("clientLog.txt", "UTF-8");


           //ExecutorService threads = Executors.newFixedThreadPool(threadCount);
            
            for (int j = 0; j < threadCount; j++) {
                Servers serverObject = bc.getRandomServer();
                final String host = serverObject.hostname;
                final int port = serverObject.portnumber;
                final int srvid = serverObject.serverid;


                Thread t = new Thread( new Runnable()
                {               
                        public void run () {
                
                        try {           
                                TTransport transport;
                                transport = new TSocket(host, port);
                                transport.open();
								System.out.println("Open.transport sucess!!");

                                TProtocol protocol = new  TBinaryProtocol(transport);
                                ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);
								System.out.println("ReplicatedBankService Client instance created!!");


                                  for(int i=0; i < iterationCount; i++){

                                    int a = rand.nextInt(10);
                                    int b = rand.nextInt(10);
									System.out.println("Transfeering from account: " + a + " to account: "+b);
                                    writer.println(Thread.currentThread().getId() + "   " + srvid + "   REQ   " + System.currentTimeMillis() + "	Transfer Operation" + "	Source ID: " + a + "	Target ID: " + b + "	Amount : 10");
                                    writer.flush();
                                    bc.transferRequest (Thread.currentThread().getId(),client, writer, a, b, 10, srvid);
                                }

								transport.close();



                        } catch (TTransportException e) {


                        }catch (TException e){

                        }
                          
                    }
                });
                t.start();
                th.add(t);
            
            }

        
            for(Thread t : th){
                try{t.join();}catch(InterruptedException e){}
            }


      String halthost = "localhost";
      int haltport = 9000;

      for (int i = 0; i < serverlist.size(); i++) {
          Servers serverobj = serverlist.get(i);
          if (serverobj.serverid == 0) {
              halthost = serverobj.hostname;
              haltport = serverobj.portnumber;
              break;
          }
      }

      TTransport transport;
      transport = new TSocket(halthost, haltport);
      transport.open();
      System.out.println("Open.transport sucess!!");

      TProtocol protocol = new  TBinaryProtocol(transport);
      ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);

      client.halt();

      transport.close();

      writer.close();
    
      }//catch(TException e){
         //       e.printStackTrace();}
		catch(FileNotFoundException e){

      }catch(UnsupportedEncodingException e){ 

      } catch (TTransportException e) {

      }
        catch (TException e) {

        }
      
    }
}
