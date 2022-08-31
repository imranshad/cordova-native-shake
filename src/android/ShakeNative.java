package org.apache.cordova;


import static android.content.Context.SENSOR_SERVICE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.SensorManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.squareup.seismic.ShakeDetector;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;


import androidx.lifecycle.Lifecycle;

import by.chemerisuk.cordova.support.CordovaMethod;

/**
 * This class echoes a string called from JavaScript.
 */
public class ShakeNative extends CordovaPlugin implements ShakeDetector.Listener {
    private CallbackContext context;
    private String url = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.context=callbackContext;
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK,"");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
        return true;
    }

    @CordovaMethod
    private void coolMethod(String message, CallbackContext callbackContext) {
        this.url=message;
        SensorManager sensorManager = (SensorManager) cordova.getActivity().getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @Override
    public void hearShake() {
//        HttpRequest request = new HttpRequest(this.url,"POST");

        Log.i("SHAKE DETECTED","ShakeNative.java--->"+url);
        JSONObject object = new JSONObject();
        try {
            object.put("SHAKEN","YES");
            sendUpdate(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @CordovaMethod
    private void sendUpdate(JSONObject object) {
        new CallAPI().execute(url);
        showAlert();
        //---plugin result
        PluginResult result = new PluginResult(PluginResult.Status.OK,"SHAKEN-TAKEN");
        result.setKeepCallback(true);
        this.context.sendPluginResult(result);
    }

    private void showAlert(){
        boolean isActive= cordova.getActivity().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
        if(isActive){
            AlertDialog alertDialog = new AlertDialog.Builder(cordova.getActivity()).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("Caida detectada, avisos enviados");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }
}

class CallAPI extends AsyncTask<String, String, String> {

    public CallAPI(){
        //set context variables if required
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        AndroidNetworking.get(params[0])
                .addPathParameter("pageNumber", "0")
                .addQueryParameter("limit", "3")
                .addHeaders("token", "1234")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("response----->",response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.i("error response----->",anError.getMessage());
                    }
                });
        return "";
    }
}