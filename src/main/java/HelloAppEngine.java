import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.cloud.datastore.LatLngValue;
import com.google.gson.Gson;
import com.google.cloud.Date;
import com.google.cloud.datastore.LatLng;
import com.googlecode.objectify.*;
import com.googlecode.objectify.annotation.Id;

import static com.googlecode.objectify.ObjectifyService.ofy;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet(
    name = "HelloAppEngine",
    urlPatterns = {"/hello"}
)

public class HelloAppEngine extends HttpServlet {
	
	List<EventDataObj> masterList;
	int eventIndex;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {

	  	ObjectifyService.register(EventDataObj.class);
	  	
	  	EventDataObj incomingEvent = (EventDataObj)request.getAttribute("EventDataObj");
	  	//MyLatLng tmp = new MyLatLng(30.0,30.0);
	  	
	  	//EventDataObj incomingEvent = new EventDataObj("time@email.com", null, "date", tmp, "20:00", "05/08/2018");
	  	
	  	masterList = ofy().load().type(EventDataObj.class).list();
	  	
	  	eventIndex = 0;
	  	boolean contains = false;
	  	for(int i = 0; i < masterList.size(); i++ ) {
	  		if(masterList.get(i).creatorEmail.equals(incomingEvent.creatorEmail) && masterList.get(i).EventName.equals(incomingEvent.EventName)) {
	  			eventIndex = i;
	  			contains = true;
	  		}
	  	}
	  	if(!contains) {
	  		masterList.add(incomingEvent);
	  		eventIndex = masterList.indexOf(incomingEvent);
	  	}
	  	boolean delete = (boolean) request.getAttribute("delete");
	  	//boolean delete = false;
	  	if(delete) {
	  		String user = request.getParameter("user");
	  		if(user.equals(masterList.get(eventIndex).creatorEmail)) {
	  			masterList.remove(eventIndex);
	  			ofy().save().entities(masterList).now();
	  		}
	  		else if(masterList.get(eventIndex).attendeeEmails.contains(user)) {
	  			masterList.get(eventIndex).attendeeEmails.remove(user);
	  			ofy().save().entities(masterList).now();
	  		}
	  		return;
	  	}
	  	boolean update_event = (boolean)request.getAttribute("update");
	  	//boolean update_event = false;
	  	if(update_event) {
	  		String user = request.getParameter("user");
	  		if(user.equals(masterList.get(eventIndex).creatorEmail)) {
	  			masterList.get(eventIndex).updateEvent(incomingEvent);
	  			ofy().save().entities(masterList).now();
	  		}
	  	}
	  	boolean weather = (boolean)request.getAttribute("weather");
	  	//boolean weather = false;
	  	if(weather || masterList.get(eventIndex).datetime.equals("new")) {
	  			try {
					updateWeather(request, response);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	  	} 		
	}
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
	  ObjectifyService.register(EventDataObj.class);
	  String user = request.getParameter("user");
	  masterList = ofy().load().type(EventDataObj.class).list();
	  ArrayList<EventDataObj> toRet = new ArrayList<>();
	  for(int i = 0; i < masterList.size(); i++) {
		  for(int j = 0; j < masterList.get(i).attendeeEmails.size(); j++) {
			  if(user.equals(masterList.get(i).attendeeEmails.get(j))) {
				  toRet.add(masterList.get(i));
				  break;
			  }
		  }
	  }
	  String jsonRes = new Gson().toJson(toRet);
	  response.setContentType("application/json");
	  response.setCharacterEncoding("UTF-8");
	  response.getWriter().write(jsonRes);
	  response.getWriter().flush();
  }
	  	
  private void updateWeather(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
	  	double lati = masterList.get(eventIndex).EventLocation.latitude;
	  	double longi = masterList.get(eventIndex).EventLocation.longitude;
	  	
	  	SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	  	java.util.Date eventDate = f.parse(masterList.get(eventIndex).date + " " + masterList.get(eventIndex).time);
	  	long eventSecs = eventDate.getTime()/1000;
	  	int gcsecs = masterList.get(eventIndex).lastWeatherUpdate;
	  	
	  	if((eventSecs - gcsecs) <= 864000) {
	  		int currentTime = (int) (System.currentTimeMillis()/1000);
	  		if(((currentTime - gcsecs) >= 21600) || masterList.get(eventIndex).datetime.equals("new")) {
	  			int secondsToHours = (int)(eventSecs - currentTime)/60/60;
	  			URL url = new URL("http://api.wunderground.com/api/f964f5e53aa6a94e/hourly10day/q/"+lati+","+longi+".json");
	  		  			
	  			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	  			
	  			conn.setDoOutput(true);
	  			
	  			conn.setRequestMethod("GET");

	  			int respCode = conn.getResponseCode(); // New items get NOT_FOUND on PUT
	  			if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_FOUND) {
	  			  request.setAttribute("error", "");
	  			  StringBuffer resp = new StringBuffer();
	  			  String line;

	  			  BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	  			  while ((line = reader.readLine()) != null) {
	  			    resp.append(line);
	  			  }
	  			  reader.close();
	  			  request.setAttribute("response", resp.toString());
	  			  try {
	  				JSONObject weatherupdate = new JSONObject(resp.toString());
	  				String weather0 = weatherupdate.get("hourly_forecast").toString();
	  				JSONArray wupdate = new JSONArray(weather0);
	  				JSONObject weatherHour = wupdate.getJSONObject(secondsToHours);
	  				JSONObject weatherTemp = weatherHour.getJSONObject("temp");
	  				masterList.get(eventIndex).weatherCondition = weatherHour.getString("wx");
	  				masterList.get(eventIndex).weatherPOP = weatherHour.getString("pop");
	  				masterList.get(eventIndex).weatherTemperature = weatherTemp.getString("english");
	  				masterList.get(eventIndex).datetime = "old";
	  				masterList.get(eventIndex).lastWeatherUpdate = currentTime;
	  				ofy().save().entities(masterList).now();
	  				response.getWriter().println("After: Location: "+ masterList.get(eventIndex).EventLocation.latitude+","+masterList.get(eventIndex).EventLocation.longitude +" Weather Condition: " +masterList.get(eventIndex).weatherCondition + " Weather Temperature: "+masterList.get(eventIndex).weatherTemperature + " Weather POP: "+masterList.get(eventIndex).weatherPOP );
	  				
	  			} catch (JSONException e) {
	  				// TODO Auto-generated catch block
	  				e.printStackTrace();
	  			}			  
	  			} else {
	  			  request.setAttribute("error", conn.getResponseCode() + " " + conn.getResponseMessage());
	  			}
	  		}
	  		
	  	}
		
	}

}