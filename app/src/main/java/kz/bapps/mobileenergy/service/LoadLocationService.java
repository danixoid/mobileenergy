package kz.bapps.mobileenergy.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kz.bapps.mobileenergy.JSONParser;
import kz.bapps.mobileenergy.MobileEnergy;
import kz.bapps.mobileenergy.model.Location;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LoadLocationService extends IntentService {
    private static final String TAG = "LoadLocationService";
    private static final String ACTION_GET_ALL = "kz.bapps.mobileenergy.service.action.GET_ALL";

    private static final String EXTRA_PARAMS = "kz.bapps.mobileenergy.service.extra.PARAMS";


    public static final String BROADCAST_RECEIVER = "kz.bapps.mobileenergy.service.BROADCAST_RECEIVER";
    public static final String EXTRA_SUCCESS = "kz.bapps.mobileenergy.service.SUCCESS";
    public static final String EXTRA_MESSAGE = "kz.bapps.mobileenergy.service.MESSAGE";
    public static final String EXTRA_DATA = "kz.bapps.mobileenergy.service.DATA";


    public LoadLocationService() {
        super("LoadLocationService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetAll(Context context, ContentValues params) {
        Intent intent = new Intent(context, LoadLocationService.class);
        intent.setAction(ACTION_GET_ALL);
        intent.putExtra(EXTRA_PARAMS, params);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_ALL.equals(action)) {
                final ContentValues params = intent.getParcelableExtra(EXTRA_PARAMS);
                handleActionGetAll(params);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetAll(ContentValues params) {
        JSONParser jPar = new JSONParser(this);
        jPar.setMethod(JSONParser.METHOD_GET);
        jPar.setResource("location");
        jPar.setParams(params);

        boolean success = jPar.execute();
        String json = "[]";

        if(success) {
            json = jPar.getJson();
        }

        final Intent intent = new Intent(BROADCAST_RECEIVER);
        intent.putExtra(EXTRA_DATA, json);

        new Runnable() {
            public void run() {
                sendBroadcast(intent);
            }
        }.run();
    }

}
