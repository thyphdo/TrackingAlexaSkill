package com.example.handlers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;

public class NumOnJobsite {
	
	//A String that will be altered and cut to find the desired information
		public static String equipLine;

		//an int that will count the total number of equipment that belongs to that company
		public static int numEquip = 0;
		public static int numOnSite = 0;
		
		public static String stringOnSite = "";

		public static String[] info;
		public static int equipIndex;

		public NumOnJobsite() {
			equipLine = "";
			equipIndex = -1;
			numOnSite = 0;
			numEquip = 0;
			stringOnSite = "";
		}	
	
	public static void run(String jobsite) {
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
			//System.out.println("The number of equipment on this jobsite is: " + numOnSite);

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
	
	public static String numOnSite() {
		return stringOnSite;
	}
	
	public static boolean jobsiteExists() {
		if(equipIndex == -1) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static void main(String[] args) {
		//run("gettysburg");
		
		run("dickinson");
		System.out.println(numOnSite());
	}
}