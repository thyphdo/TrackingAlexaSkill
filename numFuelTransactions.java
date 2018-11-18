import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class numFuelTransactions {

	//A String that will be altered and cut to find the desired information
	public static String equipLine;

	//an int that will count the total number of equipment that belongs to that company
	public static int numFTrans = 0;

	public static String[] info;
	public static int equipIndex;

	public numFuelTransactions() {
		equipLine = "";
		equipIndex = -1;
	}

	public static void run(String date1, String date2) {
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetEquipment"; //works 
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetHours/10-28-2018/11-04-2018"; //works but takes a very long time
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/FuelTransactions/100000000"; // works
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc//FuelTransactions/10-31-2018%12:00:00%AM/11-01-2018%11:59:00%PM"; // doesn't work
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc//Transactions/10-28-2018%2012:00:00%20AM/11-04-2018%2011:59:59%20PM";

		String line = "";
		ArrayList<String> allLines = new ArrayList<String>();
		try {
			long timeStart = System.currentTimeMillis();
			URL url = new URL(path);
			URLConnection urlConnection = setUsernamePassword(url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				equipLine = line;
				allLines.add(line);
				//System.out.println(line);
			}
			reader.close();

			info = equipLine.split(",");

			int count = 0;
			for(String s : info) {
				System.out.println(count + "    " + s);
				count++;
			}


			int counter = 0;
			for(String s : info) {
				if( s.contains(date2)){
					equipIndex = counter;
					//System.out.println("found it on line " + equipIndex);
					break;
				}			
				counter++;
			}
			
			String currLine = info[equipIndex];
			while( !currLine.contains(date1)) {
				equipIndex+=26;
				
				currLine = info[equipIndex];
				
				numFTrans++;
			}

			//if counter is 0, equipment is not there. handle this

			long timeAfter = System.currentTimeMillis();

			long totalTime = (timeAfter - timeStart)/1000;
			System.out.println();
			System.out.println("it took: " + totalTime + " seconds");
			System.out.println();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Over time period
	 */

	//Basic authentication set up
	private static URLConnection setUsernamePassword(URL url) throws IOException {
		String username = "rtosten";
		String password = "BTedu18";
		URLConnection urlConnection = url.openConnection();
		String authString = username + ":" + password;
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
		return urlConnection;
	}

	public static int totalFuelTransactions() {
		return numFTrans;
	}

	public static void main(String[] args) {
		run("2018-10-31", "2018-11-18");
		System.out.println(totalFuelTransactions());
	}
}
