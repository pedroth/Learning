package comunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class SimpleBrowser {

    public static void main(String[] args) throws IOException {
        String urlStr;
        BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("introduce website :");
            urlStr = sysIn.readLine();
            URL url = new URL(urlStr);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String str;

            while ((str = in.readLine()) != null) {
                System.out.println(str);
            }

            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
