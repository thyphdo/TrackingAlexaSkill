package com.example.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class FuelTransactions {

	//A String that will be altered and cut to find the desired information
	public String equipLine;

	//an int that will count the total number of equipment that belongs to that company
	public int numEquip = 0;

	public String[] info;
	public int equipIndex;

	public FuelTransactions() {
		equipLine = "";
		equipIndex = -1;
	}

	//***When User Provide only ID
	public void run(String transID) {	
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/FuelTransactions/100000000"; // works
		
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
				if( s.contains(transID)){
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

	//***When User provide date range
	public void run(String startDate, String endDate) {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc//FuelTransactions/"+startDate+"%2012:00:00%20AM/"+endDate+"%2011:59:00%20PM"; // doesn't work

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
			equipIndex = 0;
			//			int counter = 0;
			//			for(String s : info) {
			//				if( s.contains(transID)){
			//					equipIndex = counter;
			//					//System.out.println("found it on line " + equipIndex);
			//					break;
			//				}			
			//				counter++;
			//			}

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
	private URLConnection setUsernamePassword(URL url) throws IOException {
		String username = "rtosten";
		String password = "BTedu18";
		URLConnection urlConnection = url.openConnection();
		String authString = username + ":" + password;
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
		return urlConnection;
	}

	//***Call this when user input only transactionID
	//Return array of 3 info: type,volume,oum
	public String[] fuelInfo() {
		String[] allInfo = new String[5];

		String name = info[equipIndex-6].split(":")[1].split("\"")[1];
		String type = info[equipIndex+7].split(":")[1].split("\"")[1];
		String volume = info[equipIndex+5].split(":")[1].split("\"")[0];
		String oum = info[equipIndex+6].split(":")[1].split("\"")[1];
		String site = info[equipIndex+9].split(":")[1].split("\"")[1];

		System.out.println("name is " + name);
		System.out.println("type is " + type);
		System.out.println("volume is " + volume);
		System.out.println("oum is " + oum);
		System.out.println("jobsite is " + site);

		allInfo[0] = name;
		allInfo[1] = type;
		allInfo[2] = volume;
		allInfo[3] = oum;
		allInfo[4] = site;

		return allInfo;	
	}

	//***Call this when user input startDate and endDate
	//Return array of n equipments that have transaction within that period: name-fuel type-volume-uom-job site
	public String[] listOfFuelInfo() {
		try {
			String[] allInfo = new String[numEquip];
			for (int i = 0; i < numEquip; i++ ) {
				String str = "";
				str += info[equipIndex+1].split(":")[1].split("\"")[1] + " "; //name
				str += info[equipIndex+14].split(":")[1].split("\"")[1]+ " "; //fuel type
				str += info[equipIndex+12].split(":")[1].split("\"")[0]+ " "; //volume
				str += info[equipIndex+13].split(":")[1].split("\"")[1]+ " "; //uom
				str += info[equipIndex+16].split(":")[1].split("\"")[1]; //jobsite
				equipIndex += 26;
				allInfo[i] = str;
			}
			return allInfo;
		}
		catch (Exception e) {
			return new String[0];
		}
	}

//	public static void main(String[] args) {
//		FuelTransactions apicall = new FuelTransactions();
//		apicall.run("11-01-2018","11-20-2018");
//		apicall.run("2018-11-01","2018-11-20");
//
//		String[] r = apicall.listOfFuelInfo();
//		//System.out.println(r.length);
//		
//		String numTransaction = r.length + "";
//		String speechText = "There are " + numTransaction + "transactions within the provided period. ";
//		for (String trans : r) {
//			String[] transInfo = trans.split("\\s+"); //0: name, 1: transaction type, 2: volume, 3: uom, 4: jobsite
//			speechText += "Equipment " + transInfo[0] + " has a transaction of " + transInfo[1] + " with " + transInfo[2] 
//					+ " " + transInfo[3]  + " at job site " + transInfo[4] + ". ";
//		}
//		System.out.println(speechText);
//
//		apicall.run("149503934");
//		String[] results = apicall.fuelInfo();
//		String speechText = "";
//		//IF API RETURN AN ERROR
//		if (results.length == 0) {
//			speechText = 
//					"There's an error from request to the API with ID: 149503934 or there is no"
//							+ "transaction under this ID. please try again";
//		}
//		else {
//			String name = results[0];
//			String type = results[1];
//			String volume = results[2];
//			String uom = results[3];
//			String jobsite = results[4];
//
//			//IF API RETURN POSITIVE MESSAGE 	
//			speechText = "This transaction belongs to equipment " + name + " .Fuel type is " + type + " with a volume of " + volume + " " + uom + " at jobsite " +jobsite;
//		}
//		System.out.println(speechText);
//
//		
//	}
}