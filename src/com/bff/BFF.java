package com.bff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class BFF extends Activity {

    View submitButton;
    View VWbusNo;
    View VWisHighFreq;
    View VWisLowFreq;
    View VWtiming;
    String frequency=null;
    String busNo=null;
    String range =null;
    AlertDialog alertDialog;
    private int statusCode=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        alertDialog = new AlertDialog.Builder(this).create();
        submitButton = findViewById(R.id.submit);
        VWbusNo = findViewById(R.id.busNoSearch);
        VWisHighFreq = findViewById(R.id.HighRadio);
        VWisLowFreq = findViewById(R.id.LowRadio);
        VWtiming = findViewById(R.id.timingCombo);
        final Context applicationContext = getApplicationContext();
        InitiazeTimingComboBox();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFieldsFromView();
                if (!validateInput())
                    showAlertDialog("Error!", "All fields must be given.");
                else {
                    AsyncTask<Void, Void, Void> execute = new RequestItemsServiceTask().execute();
                    Toast toast = Toast.makeText(applicationContext, "Thanks for your feedback",2000);
                    toast.show();
                }

            }
        });
    }

    private void InitiazeTimingComboBox() {
        ArrayAdapter<CharSequence> timingArray= ArrayAdapter.createFromResource(this,
                R.array.Timing, android.R.layout.simple_spinner_item);
        timingArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)VWtiming).setAdapter(timingArray);
    }

    private boolean validateInput() {
        if(frequency==null || busNo==null || busNo.isEmpty()||range==null)
            return false;
        return true;
    }

    private void showAlertDialog(String title, String message) {
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void setFieldsFromView() {
        if(((RadioButton)VWisHighFreq).isChecked()){
            frequency="high";
        }
        if(((RadioButton)VWisLowFreq).isChecked()){
            frequency="low";
        }
        busNo = ((EditText) VWbusNo).getText().toString();
        Object selectedItem = ((Spinner) VWtiming).getSelectedItem();
        if(selectedItem!=null)
            range = selectedItem.toString();

    }

    private class RequestItemsServiceTask
            extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... unused) {
             requestWebService();
            return null;
        }

        public void requestWebService() {
            disableConnectionReuseIfNecessary();

            HttpURLConnection urlConnection = null;
            try {
                // create connection
                 String postParams="bus_no="+busNo+"&frequency="+frequency+"&range="+range;
                URL urlToRequest = new URL("http://boiling-everglades-9445.herokuapp.com/bus_frequencies/register");
                urlConnection = (HttpURLConnection)
                        urlToRequest.openConnection();
                urlConnection.setConnectTimeout(50000);
                urlConnection.setReadTimeout(50000);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(postParams.getBytes().length));
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                DataOutputStream wr = new DataOutputStream (
                        urlConnection.getOutputStream ());
                wr.writeBytes (postParams);
                wr.flush ();
                wr.close ();

                // handle issues
                statusCode = urlConnection.getResponseCode();

            }
            catch (MalformedURLException e) {
            }
            catch (SocketTimeoutException e) {
            }
            catch (IOException e) {
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        private void disableConnectionReuseIfNecessary() {
            // see HttpURLConnection API doc
            if (Integer.parseInt(Build.VERSION.SDK)
                    < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }
        }
    }


}