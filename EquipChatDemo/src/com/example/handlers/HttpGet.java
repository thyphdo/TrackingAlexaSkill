package com.example.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class HttpGet {
	
	/**
	 * A String that will be altered and cut to find the desired information
	 */
	public String equipLine;

	/**
	 * An integer that will count the total number of equipment that belongs to that company
	 */
	public int numEquip;
	
	/**
	 * An index indicated the start position of the requested equipment in the response
	 */
	public int equipIndex;
	
	/**
	 * A data structure stores each line of the return response 
	 */
	public String[] info;
	
	public HttpGet(){
		equipIndex = -1;
		equipLine = "";
	}

	/**
	 * Calls the HTTP GET with basic authentication and sets "equipLine" which is the string
	 * that holds all data from the GET
	 */
	public void run(String equip) {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetEquipment"; //works 

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

	/**
	 * Basic authorization set up
	 * @return the connection url with basic authorization set up
	 */
	private URLConnection setUsernamePassword(URL url) throws IOException {
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
	public int equipTotal() {
		return numEquip;
	}

	/**
	 * Checks if the desired equipment is available or not
	 * @return --> String containing either true or false
	 */
	public String equipStatus(){
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
	public String equipCMH(){
		// he had -7
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
	public String lastTransaction() {
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
	public String transactionInfo() {
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
	public String[] equipInfo() {
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

	/**
	 * Gets jobsite and return it as a string
	 * @return --> the jobsite represented as a string
	 */
	public String getJobSite() {
		// he had +3
		return info[equipIndex+3].split(":")[1].split("\"")[1];
	}

	/**
	 * This method gets the info of the designated transaction
	 * @return -> result[0] = tType result[1] = pdaUsername result[2] = jobsite
	 */ 
	public String[] basicTransactionInfo() {
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

	public static String dateChange(String date) {
		String[] dateNums = date.split("-");

		return dateNums[1] + "-" + dateNums[2] + "-" + dateNums[0];
	}
	
	/**
	 * Returns whether the request equipment ID exists in the API response
	 * @return --> true if the equipment ID exists, false otherwise
	 */
	public boolean equipExists() {
		if(equipIndex == -1) {
			return false;
		}
		else {
			return true;
		}
	}

//	public static void main(String[] args) {
		//		run("bulldozer");
		//				System.out.println("Equip status: "+equipStatus());
		//				System.out.println("Equip CMH: "+equipCMH());
		//				System.out.println("Last transaction date: "+lastTransaction());
		//				System.out.println("Last transaction number: "+transactionInfo());
		//				equipInfo();
		//basicTransactionInfo();

		//		System.out.println();
		//		run("digger");
		//				System.out.println("Equip status: "+equipStatus());
		//				System.out.println("Equip CMH: "+equipCMH());
		//				System.out.println("Last transaction date: "+lastTransaction());
		//				System.out.println("Last transaction number: "+transactionInfo());
		//				equipInfo();
		//basicTransactionInfo();

		//		System.out.println();
		//		run("longboard");
		//				System.out.println("Equip status: "+equipStatus());
		//				System.out.println("Equip CMH: "+equipCMH());
		//				System.out.println("Last transaction date: "+lastTransaction());
		//				System.out.println("Last transaction number: "+transactionInfo());
		//				equipInfo();
		//basicTransactionInfo();

		//		System.out.println();

		//				System.out.println(dateChange("2018-10-30"));
		//				System.out.println(dateChange("1997-7-20"));

		//System.out.println(equipTotal());
//	}
}