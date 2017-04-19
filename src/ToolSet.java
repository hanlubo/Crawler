import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
// import java.util.Locale;
// import java.util.Scanner;
// import java.util.regex.Pattern;

public class ToolSet {
    // assume Unicode UTF-8 encoding
    // private static final String CHARSET_NAME = "UTF-8";
    // assume language = English, country = US for consistency with System.out.
    // private static final Locale LOCALE = Locale.US;
    // used to read the entire input. source:
    // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
    // private static final Pattern EVERYTHING_PATTERN = Pattern.compile("\\A");
    // private Scanner scanner;
    
    // public ToolSet() {}
    
    // Read a url directly
    public String readAll(String name) {
        try {
            URL url = new URL(name);
            URLConnection urlconn = url.openConnection();
            urlconn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            
            InputStream _is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(_is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            return result;
        } catch (IOException e) {
            System.err.println(e);
            return null;
        } 
    }
    
    public static void writeToFile(String fileName, String content) {
        if (content == null) {
            return;
        }
        if (fileName == null || fileName == "") {
            fileName = "string.html";
        }
        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            fw.close();
            System.out.println("Write to file: "+fileName);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public static List<String> readFile(String fileName) {
        if (fileName == null || fileName == "") {
            return null;
        }
        List<String> result = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            br.close();
            fr.close();
            System.out.println("Read file: "+fileName);
            return result;
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }
    }
}
