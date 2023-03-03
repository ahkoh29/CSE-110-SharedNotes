package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import org.json.JSONException;

import java.sql.Time;

import okhttp3.MultipartBody;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
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
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Note getNote(String title) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");
        Log.i("title", title);
        Note note = null;
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("getNote", body);
            String content = jsonParser("content", body);
            String created = jsonParser("created_at", body);
            //Long updated = Long.parseLong(jsonParser("updated_at", body));
            title = title.replace("%20", " ");
            note = new Note(title, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return note;
    }

    public void putNote(Note note) throws JSONException {
        // URLs cannot contain spaces, so we replace them with %20.
        String title = note.title.replace(" ", "%20");

        Log.i("put note", title);
        String time = String.valueOf(note.updatedAt);
        Log.i("put note title" , title);
        Log.i("put note updated" , time);
        Log.i("put note content" , note.content);
        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("content", note.content)
                .addFormDataPart("updated_at", time)
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

    public String jsonParser(String tag, String response) {
        if(response.indexOf(tag)==-1){
            return "";
        }
        int colonInd = response.indexOf(':', response.indexOf(tag));
        int firstQuoteInd = response.indexOf('"', colonInd);
        int lastQuoteInd = response.indexOf('"', firstQuoteInd+1);
        Log.i (tag, response.substring(firstQuoteInd + 1, lastQuoteInd));
        return response.substring(firstQuoteInd + 1, lastQuoteInd);
    }

    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }

    @AnyThread
    public Future<Note> getNoteAsync(String title) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> getNote(title));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }

}
