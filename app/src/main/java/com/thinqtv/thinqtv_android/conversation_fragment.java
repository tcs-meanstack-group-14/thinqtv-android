package com.thinqtv.thinqtv_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.thinqtv.thinqtv_android.data.DataSource;
import com.thinqtv.thinqtv_android.data.UserRepository;

import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

public class conversation_fragment extends Fragment {
    private ActionBarDrawerToggle mDrawerToggle; //toggle for sidebar button shown in action bar

    public conversation_fragment() {
        // Required empty public constructor
    }

    public static conversation_fragment newInstance() {
        conversation_fragment fragment = new conversation_fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.conversation_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeEvents();
    }

    // Button listener for "Join Conversation" button that connects to default ThinQ.TV chatroom
    public void onJoinClick(View v, String roomName) {
        JitsiMeetConferenceOptions.Builder optionsBuilder
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom(roomName);

        Bundle userInfoBundle = new Bundle();
        userInfoBundle.putString("displayName", UserRepository.getInstance().getLoggedInUser().getUserInfo().get("name"));
        optionsBuilder.setUserInfo(new JitsiMeetUserInfo(userInfoBundle));

        JitsiMeetConferenceOptions options = optionsBuilder.build();

        // build and start intent to start a jitsi meet conference
        Intent intent = new Intent(getContext().getApplicationContext(), ConferenceActivity.class);
        intent.setAction("org.jitsi.meet.CONFERENCE");
        intent.putExtra("JitsiMeetConferenceOptions", options);
        startActivity(intent);
    }

    // listener for when a user clicks an event to go to its page
    private class goToWebview_ClickListener implements View.OnClickListener{
        private Context mContext;
        private String webviewLink;

        public goToWebview_ClickListener(Context context, String address){
            mContext = context;
            webviewLink = address;
        }

        @Override
        public void onClick(View v){
            Intent i = new Intent(mContext, AnyWebview.class);
            i.putExtra("webviewLink", webviewLink); //Optional parameters
            startActivity(i);
        }
    }

    // Use EventsJSON file to fill in ScrollView
    public void setUpcomingEvents(ArrayList<JSONObject> json)
    {
        Collections.sort(json, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                // TODO Auto-generated method stub

                try {
                    return (lhs.getString("start_at").toLowerCase().compareTo(rhs.getString("start_at").toLowerCase()));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return 0;
                }
            }
        });


        try {
            //link layout and JSON file
            LinearLayout linearLayout = getView().findViewById(R.id.upcoming_events_linearView);

            // Get the selected event filter text
            Spinner eventFilter_spinner = (Spinner)getView().findViewById(R.id.eventsSpinner);
            String eventFilter_selection = eventFilter_spinner.getSelectedItem().toString();

            //For each event in the database, create a new item for it in ScrollView
            for(int i=0; i < json.size(); i++)
            {
                // get the time of the event in local time
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
                dateFormat.setTimeZone(TimeZone.getTimeZone("MST"));
                Date date = new Date();
                try {
                    date = dateFormat.parse(json.get(i).getString("start_at"));
                } catch (ParseException e) { e.printStackTrace(); }
                dateFormat.setTimeZone(TimeZone.getDefault());
                SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, MMM dd - h:mm aa");
                String eventTimeString = displayFormat.format(date);

                // get the title of the event
                String eventTitleString = json.get(i).getString("name");

                // get the host name and permalink
                String eventHostString = json.get(i).getString("username");
                String eventHostPerma = json.get(i).getString("permalink");

                // gets the name and sets its values
                TextView newEvent_textView = new TextView(getContext());
                newEvent_textView.setId(View.generateViewId());
                newEvent_textView.setTextSize(22);
                newEvent_textView.setTextColor(getResources().getColor(R.color.colorPrimary));
                newEvent_textView.setLayoutParams(new LinearLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                newEvent_textView.setGravity(Gravity.CENTER_HORIZONTAL);
                newEvent_textView.setText(Html.fromHtml("<b>" + eventTitleString + "</b><br> <font color=#7F7F7F>" + eventHostString + "</font>" + "<br> <br>" + eventTimeString));

                // add listener to the name, so when the user clicks an event it will bring them to the event page
                //newEvent_textView.setOnClickListener(new goToWebview_ClickListener(getContext(),
                //"http://www.thinq.tv/" + json.getJSONObject(i).getString("permalink")));

                // Now that you have your textview, create a container for it and add it
                ConstraintLayout constraintLayout = new ConstraintLayout(getContext());
                constraintLayout.addView(newEvent_textView);
                constraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent i = new Intent(getContext().getApplicationContext(), AnyWebview.class);
                        i.putExtra("webviewLink", "https://thinq.tv/" + eventHostPerma);
                        startActivity(i);
                    }
                });

                // Add simple divider to put in between ConstraintLayouts (ie events)
                View viewDivider = new View(getContext());
                viewDivider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
                viewDivider.setBackgroundColor(Color.LTGRAY);

                //get current date and what week it is
                Calendar mCalendar = Calendar.getInstance();

                String roomName = getRoomNameFromTopic(json.get(i).getString("topic"));

                switch(eventFilter_selection)
                {
                    case ("All Events \u25bc") :
                    {
                        Date end_time = new Date();
                        try {
                            end_time = dateFormat.parse(json.get(i).getString("end_at"));
                        } catch (ParseException e) { e.printStackTrace(); }

                        Date current_time = mCalendar.getTime();

                        if (date.before(current_time) && end_time.after(current_time))
                        {
                            Button happening_now = new Button(getContext());
                            happening_now.setId(View.generateViewId());
                            happening_now.setBackground(getActivity().getDrawable(R.drawable.rounded_button));
                            happening_now.setTextSize(15);
                            happening_now.setTextColor(Color.WHITE);
                            happening_now.setText(R.string.happening_now);
                            happening_now.setPadding(100,0,100,0);
                            happening_now.setAllCaps(false);

                            happening_now.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if (UserRepository.getInstance().isLoggedIn()) {
                                        onJoinClick(v, roomName);
                                    }
                                    else {
                                        Toast.makeText(getContext().getApplicationContext(),
                                                "Please login to join a conversation", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            // Remove the event time from the textView and add the Happening Now Button
                            newEvent_textView.setText(Html.fromHtml("<b>" + eventTitleString + "</b> <br>"));
                            constraintLayout.addView(happening_now);

                            // Center the Happening Now button under the textView
                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(constraintLayout);
                            constraintSet.connect(happening_now.getId(),ConstraintSet.TOP,newEvent_textView.getId(),ConstraintSet.BOTTOM,0);
                            constraintSet.connect(happening_now.getId(),ConstraintSet.START,newEvent_textView.getId(),ConstraintSet.START,0);
                            constraintSet.connect(happening_now.getId(),ConstraintSet.END,newEvent_textView.getId(),ConstraintSet.END,0);
                            constraintSet.applyTo(constraintLayout);
                        }

//                        if (json.get(i).getString("topic").equals("DropIn"))
                        {
                            try {
                                linearLayout.addView(constraintLayout);
                                linearLayout.addView(viewDivider);
                            } catch (NullPointerException e) {}
                        }
                        break;
                    }
                    case ("This Week \u25bc") :
                    {
                        Date end_time = new Date();
                        try {
                            end_time = dateFormat.parse(json.get(i).getString("end_at"));
                        } catch (ParseException e) { e.printStackTrace(); }

                        Date current_time = mCalendar.getTime();

                        if (date.before(current_time) && end_time.after(current_time))
                        {
                            Button happening_now = new Button(getContext());
                            happening_now.setId(View.generateViewId());
                            happening_now.setBackground(getActivity().getDrawable(R.drawable.rounded_button));
                            happening_now.setTextSize(15);
                            happening_now.setTextColor(Color.WHITE);
                            happening_now.setText(R.string.happening_now);
                            happening_now.setPadding(100,0,100,0);
                            happening_now.setAllCaps(false);

                            happening_now.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if (UserRepository.getInstance().isLoggedIn()) {
                                        onJoinClick(v, roomName);
                                    }
                                    else {
                                        Toast.makeText(getContext().getApplicationContext(),
                                                "Please login to join a conversation", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            // Remove the event time from the textView and add the Happening Now Button
                            newEvent_textView.setText(Html.fromHtml("<b>" + eventTitleString + "</b> <br>"));
                            constraintLayout.addView(happening_now);

                            // Center the Happening Now button under the textView
                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(constraintLayout);
                            constraintSet.connect(happening_now.getId(),ConstraintSet.TOP,newEvent_textView.getId(),ConstraintSet.BOTTOM,0);
                            constraintSet.connect(happening_now.getId(),ConstraintSet.START,newEvent_textView.getId(),ConstraintSet.START,0);
                            constraintSet.connect(happening_now.getId(),ConstraintSet.END,newEvent_textView.getId(),ConstraintSet.END,0);
                            constraintSet.applyTo(constraintLayout);
                        }

                        mCalendar.set(Calendar.WEEK_OF_MONTH, (mCalendar.get(Calendar.WEEK_OF_MONTH) + 1));
                        Date filterDate = mCalendar.getTime();

                        if (date.before(filterDate))
                        {
//                            if (json.get(i).getString("topic").equals("DropIn"))
                            {
                                try {
                                    linearLayout.addView(constraintLayout);
                                    linearLayout.addView(viewDivider);
                                } catch (NullPointerException e) {}
                            }
                        }
                        break;
                    }
                    case ("Next Week \u25bc") :
                    {
                        mCalendar.set(Calendar.WEEK_OF_MONTH, (mCalendar.get(Calendar.WEEK_OF_MONTH) + 1));
                        Date filterDate = mCalendar.getTime();

                        if (date.after(filterDate))
                        {
                            mCalendar.set(Calendar.WEEK_OF_MONTH, (mCalendar.get(Calendar.WEEK_OF_MONTH) + 1));
                            filterDate = mCalendar.getTime();

                            if (date.before(filterDate))
                            {
//                                if (json.get(i).getString("topic").equals("DropIn"))
                                {
                                    try {
                                        linearLayout.addView(constraintLayout);
                                        linearLayout.addView(viewDivider);
                                    } catch (NullPointerException e) {}
                                }
                            }
                        }
                        break;
                    }
                    case ("Future \u25bc") :
                    {
                        mCalendar.set(Calendar.WEEK_OF_MONTH, (mCalendar.get(Calendar.WEEK_OF_MONTH) + 2));
                        Date filterDate = mCalendar.getTime();

                        if (date.after(filterDate))
                        {
//                            if (json.get(i).getString("topic").equals("DropIn"))
                            {
                                try {
                                    linearLayout.addView(constraintLayout);
                                    linearLayout.addView(viewDivider);
                                } catch (NullPointerException e) {}
                            }
                        }
                        break;
                    }
                }

                constraintLayout.setPadding(50,50,50,50);
                constraintLayout.setLayoutParams(new LinearLayout.LayoutParams (ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private String getRoomNameFromTopic(String topic) {
        return topic.equals("DropIn") ? getString(R.string.drop_in_room_name) : getString(R.string.conversation_room_name);
    }

    public void getEventsJSONfile()
    {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getString(R.string.events_url), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                // If you receive a response, the JSON data is saved in response
                // Clear the linearLayout
                try {
                    LinearLayout layout = (LinearLayout) getView().findViewById(R.id.upcoming_events_linearView);
                    layout.removeAllViews();
                } catch (NullPointerException e) { return; }

                //fill it back in with the response data
                ArrayList<JSONObject> array = new ArrayList<JSONObject>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        array.add(response.getJSONObject(i));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                setUpcomingEvents(array);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LinearLayout layout = (LinearLayout) getView().findViewById(R.id.upcoming_events_linearView);
                layout.removeView(getView().findViewById(R.id.loading_events));

                TextView loadingError = getView().findViewById(R.id.loading_placeholder);
                loadingError.setVisibility(View.VISIBLE);
            }
        });
        DataSource.getInstance().addToRequestQueue(request, getContext());
    }

    public void initializeEvents()
    {

        // get the spinner filter and the layout that's inside of it
        Spinner eventFilter = (Spinner) getView().findViewById(R.id.eventsSpinner);

        // add listener for whenever a user changes filter
        eventFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) adapterView.getChildAt(0)).setTextSize(18);
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                getEventsJSONfile();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                // this doesn't ever happen but i need to override the virtual class
            }
        });
    }
}
