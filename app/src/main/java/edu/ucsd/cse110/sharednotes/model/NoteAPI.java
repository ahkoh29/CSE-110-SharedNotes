package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     */
    public void echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        msg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + msg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Note getNote(String title) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");

        Note note = null;
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            String content = jsonParser("content", body);
            String created = jsonParser("created_at", body);
            Long updated = Long.parseLong(jsonParser("updated_at", body));
            note = new Note(title, content, updated);
            Log.i("getNote", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return note;
    }

    public void putNote(String title, String content, Time time) throws JSONException {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");

        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("content", content)
                .addFormDataPart("updated_at", time.toString())
                .build();
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("PUT", requestBody)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("putNote", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String jsonParser(String tag, String response){
        int colonInd = response.indexOf(':', response.indexOf(tag));
        int firstQuoteInd = response.indexOf('"', colonInd);
        int lastQuoteInd = response.indexOf('"', firstQuoteInd);
        return response.substring(firstQuoteInd + 1, lastQuoteInd);

    }
}
