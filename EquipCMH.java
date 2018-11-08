import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class EquipCMH {

	//A String that will be altered and cut to find the desired information
	public static String equipLine = "";

	//an int that will count the total number of equipment that belongs to that company
	public static int numEquip = 0;

	public static String[] info;
	public static int equipIndex;
	
	
	public static String run(String equip) {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetEquipment"; //works 
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetHours/10-28-2018/11-04-2018"; //works but takes a very long time
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/FuelTransactions/100000000"; // works
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

			numEquip = count / 26;

			int counter = 0;
			for(String s : info) {
				if( s.contains(equip)){
					equipIndex = counter;
					//System.out.println("found it on line " + equipIndex);
					break;
				}			
				counter++;
			}
			//if counter is 0, equipment is not there. handle this

			long timeAfter = System.currentTimeMillis();

			long totalTime = (timeAfter - timeStart)/1000;
			System.out.println();
			System.out.println("it took: " + totalTime + " seconds");
			System.out.println();
			
			String cmh = info[equipIndex-7].split(":")[1];
			if(!cmh.equals(null)) {
				return cmh;
			}

			return "Information not available";
			
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
}
