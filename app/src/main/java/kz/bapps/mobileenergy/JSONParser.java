package kz.bapps.mobileenergy;

/**
 * Created by user on 04.06.15.
 *
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.CookieManager;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class JSONParser {

    final static public String USER_AGENT = "MobileEnergy/1.0";
//    final static public String HOST = "192.168.100.5";
    final static public String HOST = "mobile-energy.kz";
//    final static public String HOST = "powermat.bapps.kz";
//    final static public String HOST = "tvbox.bapps.kz";
    final static public String PORT = "80";
    final static public String URL_ROOT = "http://" + HOST + ":" + PORT + "/";

    final static public String PREF_ARG_COOKIE = "cookies";
    final static public String METHOD_POST = "POST";
    final static public String METHOD_GET = "GET";
    final static public String METHOD_HEAD = "HEAD";
    final static public String METHOD_PUT = "PUT";
    final static public String METHOD_DELETE = "DELETE";


    final static private String CRLF = "\r\n";
    final static private String TWO_HYPHENS = "--";

    public static CookieManager cookieManager;
    private String json = "";
    private Boolean auth = false;
    private String resource = null;
    private String method = METHOD_GET;
    private ContentValues params = new ContentValues();
    private String cookies = "";
    private Context context;
    private SharedPreferences prefs;
    private Map<String,File> files;
    private String boundary =  "----SLhLSENiESJNFLSNf";
    private String queryString = "";

    private Map<String,List<String>> headers = new HashMap<>();

    // constructor
    // function get json from resource
    // by making HTTP POST or GET method

    public JSONParser(Context context) {
        this.context = context;
        cookieManager = CookieManager.getInstance();
        files = new HashMap<>();
        // Настройки приложения
        prefs = context.getSharedPreferences(MobileEnergy.appName, Context.MODE_PRIVATE);
        //this.setCookies(prefs.getString(ARG_COOKIE, ""));
    }

    public Boolean execute() {

        HttpURLConnection conn = null;

        try
        {
            //
            Set<Map.Entry<String, Object>> sets = this.getParams().valueSet();
            Iterator itr = sets.iterator();

            while (itr.hasNext()) {
                Map.Entry me = (Map.Entry) itr.next();
                String key = URLEncoder.encode(me.getKey().toString(), Charset.forName("UTF-8").name());
                Object value = URLEncoder.encode((String) me.getValue(), Charset.forName("UTF-8").name());
                queryString += key + "=" + value;
                if (itr.hasNext()) queryString += "&";
            }

            this.setCookies(cookieManager.getCookie(URL_ROOT));

            URL url;
            if (this.getMethod().equals(METHOD_GET) && !queryString.isEmpty()) {
                url = new URL(URL_ROOT + this.getResource() + "?" + queryString);
                Log.d("JSON Resource",URL_ROOT + this.getResource() + "?" + queryString);
            } else {
                url = new URL(URL_ROOT + this.getResource());
                Log.d("JSON Resource",URL_ROOT + this.getResource());
            }

            conn = (HttpURLConnection) url.openConnection();

            if(files.isEmpty()) {
                send(conn);
                Log.d("JSON DATA","NO Files.");
            } else {
                sendMultipart(conn);
                Log.d("JSON FILES","Files!!!");

            }

            receive(conn);

        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e("JSON ERROR","Ошибка ProtocolException");
            return Boolean.FALSE;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("JSON ERROR", "Ошибка MalformedURLException");
            return Boolean.FALSE;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("JSON ERROR", "Ошибка IOException");
            return Boolean.FALSE;
        } finally {
            if(conn != null) conn.disconnect();
        }

        return Boolean.TRUE;
    }


    /**
     * ОТПРАВКА ЗАПРОСА
     * @param conn HttpUrlConnection
     * @throws IOException
     */
    public void send(HttpURLConnection conn) throws IOException {

        if(!this.getMethod().equals(METHOD_POST) &&
                !this.getMethod().equals(METHOD_GET)) {
            conn.setRequestMethod(this.getMethod());
        }

        byte[] queryBytes = queryString.getBytes(Charset.forName("UTF-8").name());

        if(this.getMethod().equals(METHOD_POST) || this.getMethod().equals(METHOD_PUT)) {
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="
                    + Charset.forName("UTF-8").name());
            conn.setRequestProperty("Content-Length", Integer.toString(queryBytes.length));
        } else {
            conn.setRequestProperty("Content-Type", "plain/text");
        }

        conn.setInstanceFollowRedirects(false);

        conn.setRequestProperty("Host", HOST + ":" + PORT);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("X-CSRF-TOKEN", prefs.getString("_token", ""));
//        conn.setRequestProperty("Connection", "keep-alive");
//        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("Accept-Charset", Charset.forName("UTF-8").name());
        conn.setRequestProperty("Cookie", this.getCookies());
        conn.setRequestProperty("Cache-Control", "no-cache");

        for(Map.Entry<String, List<String>> entry: headers.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            for(String value : values) {
                conn.setRequestProperty(key, value);
            }
        }

        if (this.getMethod().equals(METHOD_POST) ||
                this.getMethod().equals(METHOD_PUT)) {
            DataOutputStream request = new DataOutputStream(conn.getOutputStream());
            request.writeBytes(queryString);
            request.close();
        } else {
            conn.connect();
        }

    }

    /**
     * ОТПРАВКА МУЛЬТИПАРТ ФОРМЫ
     * @param conn HttpUrlConnection
     * @throws IOException
     */
    public void sendMultipart(HttpURLConnection conn) throws IOException {

        DataOutputStream outputStream;

        generateBoundary();

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        conn.setRequestMethod(METHOD_POST);
        conn.setRequestProperty("Host", HOST + ":" + PORT);
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setRequestProperty("X-CSRF-TOKEN", prefs.getString("_token", ""));
        conn.setRequestProperty("Accept-Charset", Charset.forName("UTF-8").name());
        conn.setRequestProperty("Cookie", this.getCookies());

        if(!this.getMethod().equals(METHOD_POST)) {
            this.getParams().put("_method",this.getMethod());
        }

        outputStream = new DataOutputStream(conn.getOutputStream());

        for(Map.Entry<String, File> entry : files.entrySet()) {
            outputStream.writeBytes(TWO_HYPHENS + boundary + CRLF);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() +
                    "\"; filename=\"" + entry.getValue().getName() + "\"" + CRLF);
            outputStream.writeBytes("Content-Type: application/octet-stream" + CRLF);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + CRLF);
            outputStream.writeBytes(CRLF);

            InputStream is = new FileInputStream(entry.getValue());
            byte[] data = new byte[1024];
            int blocks;
            while ((blocks = is.read(data, 0, data.length)) != -1)
            {
                outputStream.write(data, 0, blocks);
            }

            outputStream.writeBytes(CRLF);
            outputStream.flush();
        }

        Set<Map.Entry<String, Object>> params = this.getParams().valueSet();

        for (Map.Entry<String, Object> entry : params) {
            outputStream.writeBytes(TWO_HYPHENS + boundary + CRLF);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF);
            outputStream.writeBytes("Content-Type: text/plain" + CRLF);
            outputStream.writeBytes(CRLF);
            outputStream.writeBytes((String)entry.getValue());
            outputStream.writeBytes(CRLF);
            outputStream.flush();
        }

        outputStream.writeBytes(TWO_HYPHENS + boundary + TWO_HYPHENS + CRLF);

        outputStream.close();
    }

    /**
     * ПОЛУЧЕНИЕ ОТВЕТА
     * @param conn HttpUrlConnection
     * @throws IOException
     */
    public void receive(HttpURLConnection conn) throws IOException {

        if(conn.getHeaderFields() != null) {
            List<String> cookieList = conn.getHeaderFields().get("Set-Cookie");

            if (cookieList != null) {
                for (String cookieTemp : cookieList) {
                    cookieManager.setCookie(URL_ROOT, cookieTemp);
                }

                prefs
                        .edit()
                        .putString(PREF_ARG_COOKIE, this.getCookies())
                        .apply();
            }
        }

        int status = conn.getResponseCode();
        InputStream is;

        Log.d("JSON RESPONSE URL", "==================RESPONSE HEADERS================");
        if(status >= HttpURLConnection.HTTP_BAD_REQUEST) {
            is = conn.getErrorStream();
            Log.d("JSON RESPONSE ERROR", Integer.toString(status) + " - " + conn.getResponseMessage());
        } else {
            is = conn.getInputStream();
            Log.d("JSON RESPONSE MESSAGE", Integer.toString(status) + " - " + conn.getResponseMessage());
        }

        if(!conn.getHeaderField("Content-Type").contains("application/json")) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null) {
                Log.e("JSON ANSWER", line);
            }

            throw new ProtocolException();
        }

        this.setJson(IOUtils.toString(is, Charset.forName("UTF-8").name()));

        Log.d("JSON JSON STRING", this.getJson());

        is.close();
    }


    /**
     *
     * @return JSONObject
     */
    public JSONObject toJsonObject() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(this.getJson());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data object " + e.toString());
        }
        return jsonObject;
    }

    /**
     *
     * @return JSONArray
     */
    public JSONArray toJsonArray() {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(this.json);

            for (int i = 0; i < jsonArray.length(); i++) {
                Log.d("JSON", jsonArray.getJSONObject(i).toString(1));
            }
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data array " + e.toString());
        }
        return jsonArray;
    }

    /**
     * ПРОСИМ _token и COOKIE
     */
    public void ping() {

        Log.d("Auth JSON", "ПРОСИМ _token");
        Log.d("JSON", "===============BEGIN ASK TOKEN===================");

        this.setMethod(METHOD_GET);
        this.setResource("home");

        if (!this.execute()) return;

        try {
            this.auth = this.toJsonObject().getBoolean("success");
            String token = this.toJsonObject().getString("_token");
            prefs.edit()
                    .putString("_token", token)
                    .apply();
        } catch (JSONException e) {
            Log.e("JSON AUTH", e.getMessage());
        }
        Log.d("JSON", "===============END ASK TOKEN===================");
    }

    /**
     * ОТПРАВЛЯЕМ АВТОРИЗАЦИЮ
     */
    public Boolean makeAuth() {
        String mPhone = prefs.getString("phone","");
        String mPassword = prefs.getString("password","");

        Log.d("JSON", "===============BEGIN===================");

        this.setResource("auth/login");

        ContentValues param = new ContentValues();
        param.put("phone", mPhone);
        param.put("password", mPassword);
        param.put("remember", "true");
        this.setParams(param);
        this.setMethod(METHOD_POST);

        Log.d("AUTH JSON", mPhone + " - " + mPassword);

        Log.d("JSON", "================END====================");

        return this.execute();
    }

    public void addFile(String filename,File file) {
        files.put(filename, file);
    }

    public void generateBoundary()
    {
        this.boundary = "--------------------" + UUID.randomUUID().toString();
    }

    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return this.resource;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }

    public void setParams(ContentValues params) {
        this.params = params;
    }

    public ContentValues getParams() {
        return this.params;
    }

    public void setHeaders(Map<String,List<String>> headers) {
        this.headers = headers;
    }

    public String getCookies() {
        return this.cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public Boolean isAuth() {
        return auth;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}