import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.codec.binary.Base64;

public class Test {

	public static void run() {
		String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetEquipment";
		//String path = "https://service.equipchat.com/EquipchatTransactionService.svc/GetHours/09-15-2018/10-15-2018";
        try {
            URL url = new URL(path);
            URLConnection urlConnection = setUsernamePassword(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.print(line);
            }
            reader.close();
 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
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
	
	public static void main(String[] args) {
		run();
	}

}
