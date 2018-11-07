import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class HttpGet {
	//A String that will be altered and cut to find the desired information
	public static String equipLine = "";

	//an int that will count the total number of equipment that belongs to that company
	public static int numEquip = 0;

	public static String[] info;
	public static int equipIndex;

	/*
	 * Calls the HTTP GET with basic auth and sets "equipLine" which is the string
	 * that holds all data from the GET
	 */
	public static void run(String equip) {
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetEquipment"; //works 
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetHours/10-28-2018/11-04-2018"; //works but takes a very long time
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/FuelTransactions/100000000"; // works
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
//				System.out.println(count + "    " + s);
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
			System.out.println("it took: " + totalTime + " seconds");
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


	/**
	 * Returns the number of equipment as an int
	 * @return --> the number of equipment as an int
	 */
	public static int equipTotal() {
		return numEquip;
	}

	/**
	 * Checks if the desired equipment is available or not
	 * @return --> String containing either true or false
	 */
	public static String equipStatus(){
		String avail = info[equipIndex-10].split(":")[1];
		if(!avail.equals(null)) {
			return avail;
		}
		return "Information not available";
	}

	/**
	 * Gets the CMH (cumulative machine hours) and returns it as a String
	 * @return --> The CMH represented as a String
	 */
	public static String equipCMH(){
		String cmh = info[equipIndex-7].split(":")[1];
		if(!cmh.equals(null)) {
			return cmh;
		}

		return "Information not available";
	}

	/**
	 * Gets the last date that information was pulled from the sensor and put onto the portal
	 * @return --> The date of last transaction as a String
	 */
	public static String lastTransaction() {
		String transaction = info[equipIndex+5].split(":")[1];
		if(!transaction.equals("\"\"")) {
			return transaction.split("\"")[1].split(" ")[0];
		}

		return "Information not available";
	}

	/**
	 * Returns the transaction number of latest transaction
	 * @return --> A String containing the transaction number for the latest transaction
	 */
	public static String transactionInfo() {
		String transactionInfo = info[equipIndex+6].split(":")[1];
		if(!transactionInfo.equals("\"\"")) {
			return transactionInfo;
		}

		return "Information not available";
	}

	/**
	 * Gets the make model and jobsite of the equipment.
	 * NEED TO HAVE MAKE AND MODEL IN EQUIPCHAT
	 * @return --> returns a String array of the make, model and jobsite of the equipment
	 */
	public static String[] equipInfo() {
		String[] allInfo = new String[3];

		String make = info[equipIndex+7].split(":")[1].split("\"")[1];
		String model = info[equipIndex+8].split(":")[1].split("\"")[1];
		String jobsite = info[equipIndex+3].split(":")[1].split("\"")[1];

		System.out.println("Make is "+make);
		System.out.println("Model is "+model);
		System.out.println("Jobsite is "+jobsite);

		allInfo[0] = make;
		allInfo[1] = model;
		allInfo[2] = jobsite;

		return allInfo;
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

	/*
	 * Write a method that take the date as year/month/day 
	 * switch to month/day/year
	 */

	/*
	 * number of transactions in a given period
	 */

	public static String dateChange(String date) {
		String[] dateNums = date.split("-");

		return dateNums[1] + "-" + dateNums[2] + "-" + dateNums[0];
	}

	public static void main(String[] args) {
		run("bulldozer");
//				System.out.println("Equip status: "+equipStatus());
//				System.out.println("Equip CMH: "+equipCMH());
//				System.out.println("Last transaction date: "+lastTransaction());
//				System.out.println("Last transaction number: "+transactionInfo());
//				equipInfo();
		basicTransactionInfo();
		
				System.out.println();
				run("digger");
//				System.out.println("Equip status: "+equipStatus());
//				System.out.println("Equip CMH: "+equipCMH());
//				System.out.println("Last transaction date: "+lastTransaction());
//				System.out.println("Last transaction number: "+transactionInfo());
//				equipInfo();
		basicTransactionInfo();
		
				System.out.println();
				run("longboard");
//				System.out.println("Equip status: "+equipStatus());
//				System.out.println("Equip CMH: "+equipCMH());
//				System.out.println("Last transaction date: "+lastTransaction());
//				System.out.println("Last transaction number: "+transactionInfo());
//				equipInfo();
				basicTransactionInfo();
		
				System.out.println();
		
//				System.out.println(dateChange("2018-10-30"));
//				System.out.println(dateChange("1997-7-20"));

		//System.out.println(equipTotal());
	}
}