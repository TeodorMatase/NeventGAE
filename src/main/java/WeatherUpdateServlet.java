import javax.servlet.http.HttpServlet;
import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;

@WebServlet(
	    name = "WeatherUpdate",
	    urlPatterns = {"/weather"}
	)
@SuppressWarnings("unused")
public class WeatherUpdateServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		JSONObject g = new JSONObject();
		String loc = req.getParameter("LatLng");
		String id = "30.0,30.0";
		URL url = new URL("http://api.wunderground.com/api/f964f5e53aa6a94e/hourly10day/q/" + id + ".json");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");

		int respCode = conn.getResponseCode(); // New items get NOT_FOUND on PUT
		if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_FOUND) {
		  req.setAttribute("error", "");
		  StringBuffer response = new StringBuffer();
		  String line;

		  BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		  while ((line = reader.readLine()) != null) {
		    response.append(line);
		  }
		  reader.close();
		  req.setAttribute("response", response.toString());
		  resp.getWriter().print(response.toString());
		} else {
		  req.setAttribute("error", conn.getResponseCode() + " " + conn.getResponseMessage());
		
		}
	}
}