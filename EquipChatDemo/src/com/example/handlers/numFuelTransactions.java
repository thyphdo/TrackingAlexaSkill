package com.example.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class numFuelTransactions {
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

	/**
	 * An integer that counts the total number of fuel transactions returned by the API call
	 */	
	public int numFTrans;

	public numFuelTransactions() {
		equipLine = "";
		equipIndex = -1;
		numEquip = -1;
		numFTrans = 0;
	}

	/**
	 * Calls the API Call FuelTransactions with basic authentication and sets "equipLine" which is the string
	 * that holds all data from the GET
	 * @param  date1 - start date
	 * 		   date2 - end date
	 */
	public  void run(String date1, String date2) {
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


			int counter = 0;
			for(String s : info) {
				if( s.contains(date2)){
					equipIndex = counter;
					//System.out.println("found it on line " + equipIndex);
					break;
				}			
				counter++;
			}

			if (equipIndex != - 1) {
				String currLine = info[equipIndex];
				while( !currLine.contains(date1)) {
					equipIndex+=26;

					currLine = info[equipIndex];

					numFTrans++;
				}
			}
			else {
				numFTrans = 0;
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
	private  URLConnection setUsernamePassword(URL url) throws IOException {
		String username = "rtosten";
		String password = "BTedu18";
		URLConnection urlConnection = url.openConnection();
		String authString = username + ":" + password;
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
		return urlConnection;
	}

	/**
	 * Return the total number of fuel transactions within the provided period 
	 * @return numFTrans - the total number of fuel transactions within the provided period
	 */
	public  int totalFuelTransactions() {
		return numFTrans;
	}

//	public static void main(String[] args) {
//		numFuelTransactions r = new numFuelTransactions();
//		r.run("2018-11-01", "2018-11-20");
//		System.out.println(r.totalFuelTransactions());
//	}
}
