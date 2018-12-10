package com.example.handlers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class NumOnJobsite {

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
	 * An integer that counts the total number of equipments on a jobsite
	 */
	public int numOnSite;

	/**
	 * A string representation of the total number of equipments on a jobsite
	 */
	public String stringOnSite;

	public NumOnJobsite() {
		equipLine = "";
		equipIndex = -1;
		numOnSite = 0;
		numEquip = 0;
		stringOnSite = "";
	}	

	/**
	 * Calls the API Call GetEquipment with basic authentication and sets "equipLine" which is the string
	 * that holds all data from the GET
	 * @param jobsite - the jobsite that user wants to check
	 */
	public void run(String jobsite) {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetEquipment";
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
				if( s.contains(jobsite)){
					equipIndex = counter;
					numOnSite++;
				}			
				counter++;
			}

			stringOnSite = "" + numOnSite;

			System.out.println();

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
	 * Return the total number of equipments available on the given jobsite 
	 * @return stringOnSite - string representation of the total number of equipments available on the given jobsite 
	 */
	public String numOnSite() {
		return stringOnSite;
	}

	/**
	 * Return if a jobsite exists on the system
	 * @return true if the API call returns a result, false otherwise 
	 */
	public boolean jobsiteExists() {
		if(equipIndex == -1) {
			return false;
		}
		else {
			return true;
		}
	}

	//	public static void main(String[] args) {
	//		//run("gettysburg");
	//		
	//		run("dickinson");
	//		System.out.println(numOnSite());
	//	}
}