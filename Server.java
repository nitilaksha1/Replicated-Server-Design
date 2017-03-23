import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class Server {
	public static void main (String [] args) throws FileNotFoundException {
		
		if (args.length != 2) {
			System.exit(1);
		}
		
		int id = Integer.parseInt(args[0]);
		int serverportnumber = 9999;
		String filename = args[1];
		
		Scanner scan = new Scanner (new File(filename));
		scan.nextLine();
		
		while (scan.hasNext()) {
			String hostname = scan.next();
			int fid = scan.nextInt();
			int portnumber = scan.nextInt();
			
			if (id == fid) {
				serverportnumber = portnumber;
				break;
			}
				
		}
		
		//Rest of server logic
		scan.close();
		
	}
}
