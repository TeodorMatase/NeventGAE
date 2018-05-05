import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.cloud.Date;
import com.google.cloud.datastore.LatLng;
import com.google.protobuf.TextFormat.ParseException;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
//TODO update event name, description, time, calendar, attendee list, weather information, event location
public class EventDataObj {
	//ALL GAE INTERACTION IS DONE THROUGH THE EVENT CLASS AND ITS METHODS
    String EventName;
    String Description;
    @Id Long id;
    //TODO MAKE DATE AND TIME A GREGORIAN CALENDAR
    String datetime;
    String date;
    String time;
    Date eventDate;
    int lastWeatherUpdate;
    String weatherCondition;
    String weatherTemperature;
    String weatherPOP;
    String creatorEmail;
    String eventKey;
    ArrayList<String> attendeeEmails;
    MyLatLng EventLocation;


    public EventDataObj(String creator, String Desc, String name, MyLatLng newLat, String time, String date){
        this.EventLocation = newLat;
        this.time = time;
        this.date = date;
        this.lastWeatherUpdate = (int) (System.currentTimeMillis()/1000);
        this.EventName = name;
        this.creatorEmail = creator;
        this.Description = Desc;
        attendeeEmails = new ArrayList<>();
        attendeeEmails.add(creatorEmail);
        this.datetime = "new";
      //  updateWeather();
        //Create Event JSON Object
        //Send event JSON Object
        //Verify event created successfully
        //Update eventKey with response from GAE
    }
    
    public EventDataObj() {
    	this.EventLocation = new MyLatLng(30.0,30.0);
    	this.time = "";
    	this.date = "";
    	this.EventName = "";
    	this.creatorEmail = "sample@email.com";
    	this.datetime = "new";
    	this.Description = "";
    	this.attendeeEmails = new ArrayList<>();
    	this.lastWeatherUpdate = (int)(System.currentTimeMillis()/1000);
    }

    public void updateEvent(EventDataObj newDetails){
        //Similar to constructor - different method on GAE to signify overwriting old event data with new event data, DO NOT MAKE A NEW EVENT OBJECT)
        this.EventLocation = newDetails.EventLocation;
        this.Description = newDetails.Description;
        this.date = newDetails.date;
        this.time = newDetails.time;
        this.attendeeEmails = newDetails.attendeeEmails;
        this.datetime = "new";
    }
}
