/*
 * Multi-thread crawler
 * Dependencies: ToolSet.java
 * 
 * Compile:
 * javac WebCrawler.java
 *
 * Run:
 * java WebCrawler URL_list.txt
 *
 * Description:
 *
 * This java application takes a list of source URL as input, invoke multiple
 * threads as workers. Each thread will take a URL from the URL list (BlockingQueue)
 * , crawl the website, parse the content to find more URLs and put them into 
 * the discovered URL list and uncrawled URL list.
 *
 * Upto the MAX_LIMIT number of website was found, threads are no longer putting more
 * URLs into the URL list. The main thread will wait until threads compelete crawling 
 * all URLs in the uncrawled list.
 * 
 * All URL content will be written in the "./crawledPages/" folder in current run dir:
 * ./crawledPages/*.txt
 *
 * Sample URL_list.txt:
 *
 * $ cat URL_list.txt
 * http://www.xxx.com
 * http://www.yyy.org
 * http://www.zzz.edu
 *
 */
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler implements Runnable{
    private static final int MAX_LIMIT = 100;
    private static int threadNumber = 4;
    private static int waitThread   = threadNumber;
    private static boolean stopCrawl = false;
    private static Map<String, UrlSet> map = new ConcurrentHashMap<String, UrlSet>();
    private static BlockingQueue<String> uncrawled = new ArrayBlockingQueue<String>(1024);
    
    private String http  = "http://(\\w+\\.)+(\\w+)";
    private String https  = "http(s?)://(\\w+\\.)+(\\w+)";
    private Pattern pattern = Pattern.compile(http);
    private ToolSet tool = new ToolSet();
    
    public WebCrawler() { }
    
    public void run() {
        while (true) {
            try {
                /*synchronized (this.getClass()) {
                    waitThread++;
                }*/
                String site = uncrawled.take();
                synchronized (this.getClass()) {
                    waitThread--;
                }
                
                UrlSet curr = map.get(site);
                int depth = curr.getDepth();
                System.out.println(Thread.currentThread().getName()+" "+depth+": "+site);
                // send request to url and get response
                String text = tool.readAll(site);
                curr.setResponse(text);
                if (text == null || stopCrawl) continue;
                // find strings that match url style
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String url = matcher.group();
                    if (!map.containsKey(url)) {
                        map.put(url, new UrlSet(url, depth + 1));
                        uncrawled.put(url);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(e);
            } finally {
                synchronized (this.getClass()) {
                    waitThread++;
                }
            }
	    if (stopCrawl && uncrawled.isEmpty()) break;
        }
    }
    /*
    private static synchronized void add(int value) {
        waitThread += value;
    }
    */
    public static void main(String[] args) throws Exception {
        List<String> source = null;
        // read URL list from file: 
        // http://www.xxx.com
        // http://www.yyy.org
        // http://www.zzz.edu
        if (args.length > 0) {
            source = ToolSet.readFile(args[0]);
        }
        if (source == null) {
            source = Arrays.asList("http://www.purdue.edu");
        }
        // Add source URLs to uncrawled URL pool
        for (String s : source) {
            map.put(s, new UrlSet(s, 0));
            try {
                uncrawled.put(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Start thread pool
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadNumber; i++) {
            new Thread(new WebCrawler(), "thr-"+i).start();
        }
        // Wait for job completion
        while (true) {
            System.out.println("Status: Active Thread:"+Thread.activeCount()+", Waiting Thread:"+waitThread+", " +
                    "Uncrawled:"+uncrawled.size()+", Map size:"+map.size());
            
            if (map.size() >= MAX_LIMIT) {
                // System.exit(1);
                stopCrawl = true;
                if (uncrawled.isEmpty() && waitThread + 1 == Thread.activeCount()) { 
                    break;
                }
            }
            /*
            Thread[] listOfThread = new Thread[Thread.activeCount()];
            Thread.enumerate(listOfThread);
            for (Thread i : listOfThread) {
                System.out.println(i.getName());
            }
            */
            Thread.sleep(2000);
        }
        long end = System.currentTimeMillis();
        System.out.println("Total url crawled: "+map.size());
        System.out.println("Time elapsed: " + ((end - start) / 1000) + "s");
        // Make folder
        String folderName = "./crawledPages";
        File folder = new File(folderName);
        folder.mkdirs();
        // Write URL content to folder/file.txt
        for (Map.Entry<String, UrlSet> entry : map.entrySet()) {
            String website = entry.getKey();
            int index = website.indexOf("://");
            website = website.substring(index + 3);
            String filename = folder + "/" + website + ".txt";
            String content = entry.getValue().getResponse();
            ToolSet.writeToFile(filename, content);
        }
	System.exit(1);
    }
}

class UrlSet {
    private String url; // url
    private int depth;  // dfs crawl depth
    private String response; // url response
    
    public UrlSet(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }
    
    public void setResponse(String re) {
        response = re;
    }
    
    public String getResponse() {
        return response;
    }
    
    public int getDepth() {
        return depth;
    }
}
