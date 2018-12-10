package com.example.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;

public class LatestTransaction {

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

	public LatestTransaction() {
		equipLine = "";
		equipIndex = -1;
		numEquip = 0;
		info = new String[0];
	}	

	/**
	 * Calls the API Call Transactions with basic authentication and sets "equipLine" which is the string
	 * that holds all data from the GET. 
	 * The start date of the search is 3 months from the current date, the end date of the search is current date
	 * @param  equip - equipment's name
	 */
	public void run(String equip) {
		String[] timePeriod = getDate();

		String path = "https://service.equipchat.com/EquipchatTransactionService.svc//Transactions/"+timePeriod[0]
				+"%2012:00:00%20AM/"+timePeriod[1]+"%2011:59:59%20PM";
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
					System.out.println("Break!: " + counter);
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
	 * Extracting information (date, time, transaction type, username and jobsite) of 
	 * the lastest transaction found and return as a string array
	 * @return allInfo - a string array of size 5: 0-date, 1-time, 2-transaction type, 3-user, 4-jobsite
	 */
	public String[] basicTransactionInfo() {
		String[] allInfo = new String[5];

		if (equipIndex != -1) {
			String date = info[equipIndex-1].split("\\s+")[0];
			date = date.split(":\"")[1];
			String time = info[equipIndex-1].split("\\s+")[1] + info[equipIndex-1].split("\\s+")[2].split("\"")[0];
			String tType = info[equipIndex+9].split(":")[1].split("\"")[1];
			String pdaUsername = info[equipIndex+11].split(":")[1];
			String jobsite = info[equipIndex+16].split(":")[1].split("\"")[1];

			if( pdaUsername.equals("null")) {
				pdaUsername = "No PDAUsername assigned";
			}

			if( jobsite.equals("null")) {
				jobsite = "No jobsite assigned";
			}

			//		System.out.println("date is "+date);
			//		System.out.println("time is "+time);
			//		System.out.println("transaction type is "+tType);
			//		System.out.println("PDAUsername is "+pdaUsername);
			//		System.out.println("Jobsite is "+jobsite);

			allInfo[0] = date;
			allInfo[1] = time;
			allInfo[2] = tType;
			allInfo[3] = pdaUsername;
			allInfo[4] = jobsite;

			return allInfo;
		}
		else {
			return new String[0];
		}
	}

	/**
	 * Return a correct start date and end date for the API Call 
	 * start date: 3 months before the current date
	 * end date: the current date
	 * @return The start and end date string representation of "yyyy-MM-dd"
	 */	
	public static String[] getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();

		String start = dateFormat.format(cal.getTime());

		cal.add(Calendar.MONTH, -3);

		String end = dateFormat.format(cal.getTime());

		return new String[] {start, end};
	}

	//	public static void main(String[] args) {
	//	LatestTransaction apicall = new LatestTransaction();
	//	apicall.run("abc");
	//	String[] result = apicall.basicTransactionInfo();
	//	String speechText = " ";
	//	if (result.length != 0){
	//		//0: date  1: time  2: transactionType   3: pdaUsername  4: jobSite
	//		String date = result[0];
	//		String time = result[1];
	//		String type = result[2];
	//		String user = result[3];
	//		String jobSite = result[4];
	//		
	//		speechText =  "abc last transaction is " + type + " at jobsite " + jobSite + " on " + date + " at " + time;
	//		//if there is no user available 
	//		if (user.contains("No"))
	//			speechText += " with no available username assigned";
	//		else {
	//			speechText += " operated by " + user;
	//		}
	//	}
	//	//IF API RETURN AN ERROR
	//	else {
	//		speechText = 
	//				"There's an error from request to the API with equipment or the equipment does not exist. please try again";
	//	}
	//	System.out.println(speechText);
	//}
}