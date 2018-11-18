import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class EquipHour {

	public static String equipLine;

	//an int that will count the total number of equipment that belongs to that company
	public static int numEquip = 0;

	public static String[] info;
	public static int equipIndex;
	
	public static String firstCMH, lastCMH;

	public EquipHour() {
		equipLine = "";
		equipIndex = -1;
	}	

	public static void run(String equip, String date1, String date2) {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetHours/" + date1 + "/" + date2; //works but takes a very long time
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
					System.out.println();
					firstCMH = info[equipIndex+9].split(":")[1].split("\"")[0];
					System.out.println(firstCMH);
					break;
				}			
				counter++;
			}
			
			for(int i = info.length-1; i > 0; i--) {
				String s = info[i];
				
				if( s.contains(equip)){
					equipIndex = i;
					System.out.println();
					lastCMH = info[equipIndex+8].split(":")[1].split("\"")[0];
					System.out.println(lastCMH);
					break;
				}			
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
	
	public static int cmhChange() {
		int firstIntCmh = Integer.parseInt(firstCMH);
		int lastIntCmh = Integer.parseInt(lastCMH);
		
		int result = lastIntCmh - firstIntCmh;
		
		//System.out.println(result);
		
		return result;
	}

	public static void main(String[] args) {
//		run("Bulldozer", "10-28-2018", "11-04-2018");
//		System.out.println(cmhChange());
		
		run("Longboard", "10-28-2018", "11-04-2018");
		System.out.println(cmhChange());
	}
}
