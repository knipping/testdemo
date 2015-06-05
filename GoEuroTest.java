import java.io.*;
import java.net.*;
import java.util.*;
// library json-simple-1.1.1.jar from https://code.google.com/p/json-simple/
import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * Saves selected entries of JSON answer from API URL in csv-format.
 * String entries are quoted. Double quotes in string entries are repeated
 * (following RFC 4180).
 *
 * See <a href="https://github.com/goeuro/dev-test">https://github.com/goeuro/dev-test</a> for details of the task.
 *
 * @author Lars Knipping
 * @version 2015-06-05
 */
public class GoEuroTest {
  // Development notes:
  // - For usage beyond this small exercise consider using a reader object 
  //   instead of the purely static approach here.
  // - Add a commandline flag for force-overwrite for an existing file? 
  // - Check if file output should force a special encoding.
  // - Make it part of a custom package.

  /** Usage string for call by jar file. */
  public final static String USAGE_INFO = 
    "usage> java -jar GoEuroTest.jar [<file>] <city name>\n"
    +"-f <file>\tspecify output file name, defaults to <city name>\n"
    +"<city name>\tcity to be queried for";
  /** Protocol for the API url. */
  public final static String API_PROTOCOL = "http";
  /** Hostname for the API url. */
  public final static String API_HOST = "api.goeuro.com";
  /** Path head for the API url. */
  public final static String API_BASEPATH = "/api/v2/position/suggest/en/";

  /** Read content string from api URL. */ 
  protected static String read(String entry) throws IOException {
    // savely encoded query url:
    final String string = URLEncoder.encode(entry, "UTF-8");
    final URL url = new URL(API_PROTOCOL, API_HOST, API_BASEPATH+string);
    // read all input line-by-line
    final StringBuilder sb = new StringBuilder();
    try (final BufferedReader in = 
         new BufferedReader(new InputStreamReader(url.openStream()))) {
        String line;
        while ((line = in.readLine()) != null)
          sb.append(line).append("\n");
      }
    return sb.toString();
  }

  /** Return JSON element value as string, return empty string null. */
  protected static String simpleEntryToString(Object o) {
    if (o==null)
      // used as a safeguard (spec does not guarantee all entries to exist)
      return ""; // empty entry
    if (o instanceof String) { // string entry: quote it
      final String s = (String) o; 
      final StringBuilder sb = new StringBuilder("\""); // quote output
      for (int i=0; i<s.length(); ++i) {
        final char c = s.charAt(i);
        sb.append(c);
        if (c == '"')
          sb.append('"'); // repeat "-chars in quoted entry 
      }
      sb.append('"'); // end quote
      return sb.toString();
    } else {  // JSON entry != string: should be a number or a boolean
      assert(o instanceof Number || o instanceof Boolean);
      return o.toString(); // non-localized string representation
    }
  }

  /** Returns a single line of the csv output for a given array entry. */
  protected static String arrayEntryToString(JSONObject jsonObj) {
    assert(jsonObj != null);
    // output _id, name, type, latitude, longitude
    // the latter two are from geo_position entry
    final StringBuilder sb = new StringBuilder();
    sb.append(simpleEntryToString(jsonObj.get("_id"))).append(',');
    // name entry:  guaranteed to exist
    sb.append(simpleEntryToString(jsonObj.get("name"))).append(',');
    // geo_position: guaranteed to exist
    final JSONObject pos = (JSONObject) jsonObj.get("geo_position"); 
    sb.append(simpleEntryToString(pos.get("latitude"))).append(',');
    // RFC 4180 requires MS DOS style lines breaks
    sb.append(simpleEntryToString(pos.get("longitude"))).append("\r\n");
    return sb.toString();
  }

  public static void main(String[] args) {
    // parse command line arguments
    if (args.length < 1 || args.length > 2) {
      System.err.println(USAGE_INFO);
      System.exit(1);
    }
    final String city = args[args.length-1];
    final File file = new File((args.length>1) ? args[0] : (city+".csv")); 
    if (file.exists()) {
      System.err.println("Failed to write to '"+file+"': file exists already");
      System.exit(1);
    }
    try (BufferedWriter buffwrite = new BufferedWriter(new FileWriter(file))) {
      final String string = read(city);
      final JSONParser parser = new JSONParser();
      final JSONArray array = (JSONArray) parser.parse(string);
      assert(array != null);
      for (Object jsonObj: array)
        buffwrite.write(arrayEntryToString((JSONObject) jsonObj));
    } catch(IOException  e){
      System.out.println("IO error");
      e.printStackTrace();
    } catch(ClassCastException  e){
      System.out.println("Parse error (unexpected data type)"); 
      e.printStackTrace(); 
    } catch(ParseException e){
      System.out.println("Parse error, position: "+e.getPosition());
      e.printStackTrace();
    }
  }
}
