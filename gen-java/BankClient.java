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

      public Servers(int port, String host){
          portnumber = port;
          hostname = host;
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

      int balanceRequest(ReplicatedBankService.Client client, PrintWriter writer, int id) throws TException{

        return client.multi_getBalance(id,0);
      }

      void transferRequest(ReplicatedBankService.Client client, PrintWriter writer, int src, int target, int amount) throws TException{


        String s= client.multi_transfer(src,target,amount,0);

        if (s.equals("FAILED")) {

            writer.println("Request: Transfer " + "Parameters: " + src + " "+target + " " + amount + " Request Status: FAILED");
            writer.flush();
        }


      }

      public void listServers(String filename){
          try{     
            Scanner scan = new Scanner (new File(filename));
            scan.nextLine();
        
            while (scan.hasNext()) {
                String hostname = scan.next();
                int fid = scan.nextInt();
                int portnumber = scan.nextInt();
                serverlist.add(new Servers(portnumber,hostname));

                
            }
            scan.close();
            }catch(FileNotFoundException e){}
            //Rest of server logic
        
      }

      public Servers getRandomServer(){
              
            return serverlist.get(rand.nextInt(serverlist.size())); 
      }


    public static void main(String[] args){


      try{
            final BankClient bc = new BankClient();
            final String hostname = args[0];
            int threadCount = Integer.parseInt(args[2]);
            final int iterationCount = 100;
            final String filename = args[1];
            bc.listServers(filename);
            final PrintWriter writer = new PrintWriter("clientLog.txt", "UTF-8");


            
      for(int i=0; i < 100; i++)
      {

            Servers serverObject = bc.getRandomServer();
            final String host = serverObject.hostname;
            final int port = serverObject.portnumber;
   
           //ExecutorService threads = Executors.newFixedThreadPool(threadCount);
            
            for (int j = 0; j < threadCount; j++) {

                  Thread t = new Thread( new Runnable() 
                {               
                        public void run () {
                
                        try {           
                                TTransport transport;
                                transport = new TSocket(host, port);
                                transport.open();

                                TProtocol protocol = new  TBinaryProtocol(transport);
                                ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);


                                  for(int i=0; i < iterationCount; i++){

                                    int a = rand.nextInt(10);
                                    int b = rand.nextInt(10);                                                                
                                    bc.transferRequest (client, writer, a, b, 10);  
                                }

                        transport.close();



                        } catch (TTransportException e) {

                            e.printStackTrace();
                        }catch (TException e){ e.printStackTrace();}
                          
                    }
                });
                t.start();
                th.add(t);
            
            }

        
            for(Thread t : th){
                try{t.join();}catch(InterruptedException e){}
            }
         
        }
            int sum = 0;

            for (int i = 0; i < 10; i++) {
                int accid=i;
                int bal = bc.balanceRequest(client, writer, accid);

                if (bal == -1)
                    bal = 0;

                sum += bal;
            }

            System.out.println("Sum of balances = " + sum);
            writer.println("Sum of balances = " + sum);
            writer.println();
            writer.flush();          
            writer.close();
    
      }catch(TException e){
                e.printStackTrace();
      }catch(FileNotFoundException e){
                e.printStackTrace(); 
      }catch(UnsupportedEncodingException e){ 
                e.printStackTrace();
      }
      
      

    }
}
