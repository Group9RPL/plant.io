package id.sample.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String PREF_IP = "PREF_IP_ADDRESS";
    public final static String PREF_PORT = "PREF_PORT_NUMBER";
    public final static String RESPONSE = "SERVER_RESPONSE";
    private EditText editTextIPAddress, editTextPortNumber;
    private Button connect;

    // shared preferences objects used to save the IP address and port so that the user doesn't have to
    // type them next time he/she opens the app.
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        // declare buttons and text inputs
        connect = (Button) findViewById(R.id.connect);
        editTextIPAddress = (EditText) findViewById(R.id.ipedit);
        editTextPortNumber = (EditText) findViewById(R.id.portedit);
        sharedPreferences = getSharedPreferences("HTTP_HELPER_PREFS",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        // set button listener (this class)
        connect.setOnClickListener(this);
        // get the IP address and port number from the last time the user used the app,
        // put an empty string "" is this is the first time.
        editTextIPAddress.setText(sharedPreferences.getString(PREF_IP,""));
        editTextPortNumber.setText(sharedPreferences.getString(PREF_PORT,""));
    }

    @Override
    public void onClick(View view) {
        // get the ip address
        String ipAddress = editTextIPAddress.getText().toString().trim();
        // get the port number
        String portNumber = editTextPortNumber.getText().toString().trim();

        // save the IP address and port for the next time the app is used
        editor.putString(PREF_IP,ipAddress); // set the ip address value to save
        editor.putString(PREF_PORT,portNumber); // set the port number to save
        editor.commit(); // save the IP and PORT

        // execute HTTP request
        if(ipAddress.length()>0 && portNumber.length()>0) {
            new HttpRequestAsyncTask(view.getContext(),ipAddress, portNumber).execute();
        }
    }
    /**
     * Description: Send an HTTP Get request to a specified ip address and port.
     * @param ipAddress the ip address to send the request to
     * @param portNumber the port number of the ip address
     * @return The ip address' reply text, or an ERROR message if it fails to receive one
     */
    public String sendRequest(String ipAddress, String portNumber) {
        String serverResponse = "ERROR";
        try {

            HttpClient httpclient = new DefaultHttpClient(); // create an HTTP client
            // define the URL e.g. http://myIpaddress:myport/?pin=13 (to toggle pin 13 for example)
            URI website = new URI("http://"+ipAddress+":"+portNumber);
            HttpGet getRequest = new HttpGet(); // create an HTTP GET object
            getRequest.setURI(website); // set the URL of the GET request
            HttpResponse response = httpclient.execute(getRequest); // execute the request
            // get the ip address server's reply
            InputStream content = null;
            content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    content
            ));
            serverResponse = in.readLine();
            // Close the connection
            content.close();
        } catch (ClientProtocolException e) {
            // HTTP error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            // IO error
            serverResponse = e.getMessage();
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // URL syntax error
            serverResponse = e.getMessage();
            e.printStackTrace();
        }
        editor.putString(RESPONSE,serverResponse);
        // return the server's reply/response text
        return serverResponse;
    }
    /**
     * An AsyncTask is needed to execute HTTP requests in the background so that they do not
     * block the user interface.
     */
    private class HttpRequestAsyncTask extends AsyncTask <Void, Void, Void> {

        // declare variables needed
        private String requestReply,ipAddress, portNumber;
        private Context context;
        private AlertDialog alertDialog;


        /**
         * Description: The asyncTask class constructor. Assigns the values used in its other methods.
         * @param context the application context, needed to create the dialog
         * @param ipAddress the ip address to send the request to
         * @param portNumber the port number of the ip address
         */
        public HttpRequestAsyncTask(Context context,String ipAddress, String portNumber)
        {
            this.context = context;

            alertDialog = new AlertDialog.Builder(this.context)
                    .setTitle("The Plant.io pot says:")
                    .setCancelable(true)
                    .create();

            this.ipAddress = ipAddress;
            this.portNumber = portNumber;
        }
        /**
         * Name: doInBackground
         * Description: Sends the request to the ip address
         * @param voids
         * @return
         */
        @Override
        protected Void doInBackground(Void... voids) {
            alertDialog.setMessage("Waiting for Plant.io pot to notice you...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
            requestReply = sendRequest(ipAddress,portNumber);
            return null;
        }

        /**
         * Name: onPostExecute
         * Description: This function is executed after the HTTP request returns from the ip address.
         * The function sets the dialog's message with the reply text from the server and display the dialog
         * if it's not displayed already (in case it was closed by accident);
         * @param aVoid void parameter
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            alertDialog.setMessage(requestReply);
            if(!alertDialog.isShowing())
            {
                alertDialog.show(); // show dialog
            }
        }

        /**
         * Name: onPreExecute
         * Description: This function is executed before the HTTP request is sent to ip address.
         * The function will set the dialog's message and display the dialog.
         */
        @Override
        protected void onPreExecute() {
            alertDialog.setMessage("Trying to talk with the pot, please wait...");
            if(!alertDialog.isShowing())
            {
                alertDialog.show();
            }
        }

    }
}
