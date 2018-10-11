import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * This will test the HTTP GET from the REST API 
 */
public class RESTapi {

	public static void main(String[] args) {
		try {

			//possible compnany id d1849d70-70d6-4cc0-bad2-b39258995ba0

//			URL url = new URL("http://devservice.equipchat.com/EquipchatTransactionService.svc/Equipment/1732/RR328 HTTP/1.1 ");
//
//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod("GET");
//			conn.setRequestProperty("Accept", "application/json");
//
//			if (conn.getResponseCode() != 200) {
//				throw new RuntimeException("Failed : HTTP error code : "
//						+ conn.getResponseCode());
//			}
//
//			BufferedReader br = new BufferedReader(new InputStreamReader(
//					(conn.getInputStream())));
//
//			String output;
//			System.out.println("Output from Server .... \n");
//			while ((output = br.readLine()) != null) {
//				System.out.println(output);
//			}
//
//			conn.disconnect();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
