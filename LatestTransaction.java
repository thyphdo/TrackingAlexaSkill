import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class LatestTransaction {
	//A String that will be altered and cut to find the desired information
	public static String equipLine;

	//an int that will count the total number of equipment that belongs to that company
	public static int numEquip = 0;

	public static String[] info;
	public static int equipIndex;

	public LatestTransaction() {
		equipLine = "";
		equipIndex = -1;
	}	

	/*
	 * Calls the HTTP GET with basic auth and sets "equipLine" which is the string
	 * that holds all data from the GET
	 */
	public static void run(String equip) {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc//Transactions/10-28-2018%2012:00:00%20AM/11-04-2018%2011:59:59%20PM";
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

	public static String[] basicTransactionInfo() {
		String[] allInfo = new String[3];
		//tType + 9
		//pdaUsername + 11
		//jobsite + 16

		String tType = info[equipIndex+9].split(":")[1].split("\"")[1];
		String pdaUsername = info[equipIndex+11].split(":")[1];
		String jobsite = info[equipIndex+16].split(":")[1].split("}")[0];

		if( pdaUsername.equals("null")) {
			pdaUsername = "No PDAUsername assigned";
		}

		if( jobsite.equals("null")) {
			jobsite = "No jobsite assigned";
		}

		System.out.println("transaction type is "+tType);
		System.out.println("PDAUsername is "+pdaUsername);
		System.out.println("Jobsite is "+jobsite);

		allInfo[0] = tType;
		allInfo[1] = pdaUsername;
		allInfo[2] = jobsite;

		return allInfo;
	}

	public static void main(String[] args) {
		run("bulldozer");
		basicTransactionInfo();
	}
}
