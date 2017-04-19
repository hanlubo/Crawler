/*
 * Single thread web crawler, it will do the following:
 *
 * 1. Choose root web page as source s.
 * 2. Maintain a Queue of websites to explore.
 * 3. Maintain a SET of discovered websites.
 * 4. Dequeue the next website and enqueue websites to which it links (provided you haven't done so before).
 *
 */
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleWebCrawler {
    private static final int MAX_SITES = 100;
    
    private Set<String> discoveredSites;
    private Deque<String> uncrawledSites;
    
    private String source;
    private String http;
    private String https;
    private Pattern pattern; 

    private ToolSet tool = new ToolSet();

    public SimpleWebCrawler(String s) {
        discoveredSites = new HashSet<String>();
        uncrawledSites = new ArrayDeque<String>();
        source = s;
        http  = "http://(\\w+\\.)+(\\w+)";
        https  = "http(s?)://(\\w+\\.)+(\\w+)";
        pattern = Pattern.compile(http);
    }
    
    public void begin() throws IOException {
        if (source == null) return;
        discoveredSites.add(source);
        uncrawledSites.offer(source);
        int cnt = 0;
        while (!uncrawledSites.isEmpty() && discoveredSites.size() < MAX_SITES) {
            String site = uncrawledSites.poll();
            System.out.println((cnt++)+": "+site);
            String text = tool.readAll(site);
            
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String url = matcher.group();
                if (!discoveredSites.contains(url)) {
                    // System.out.println("-- "+cnt+": "+url);
                    discoveredSites.add(url);
                    uncrawledSites.offer(url);
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        // String source = args.length != 0 ? args[0] : "http://www.princeton.edu";
        String source = args.length != 0 ? args[0] : "http://www.purdue.edu";
        SimpleWebCrawler crawler = new SimpleWebCrawler(source);
        try {
            crawler.begin();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
}
