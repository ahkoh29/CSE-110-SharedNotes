package edu.ucsd.cse110.sharednotes.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import edu.ucsd.cse110.sharednotes.model.Note;
import edu.ucsd.cse110.sharednotes.model.NoteAPI;
import edu.ucsd.cse110.sharednotes.model.NoteDatabase;
import edu.ucsd.cse110.sharednotes.model.NoteRepository;

public class NoteViewModel extends AndroidViewModel {
    private LiveData<Note> note;
    private final NoteRepository repo;
    private NoteAPI noteAPI;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        var context = application.getApplicationContext();
        var db = NoteDatabase.provide(context);
        var dao = db.getDao();
        this.repo = new NoteRepository(dao);
        this.noteAPI = new NoteAPI();
    }

    public LiveData<Note> getNote(String title) throws ExecutionException, InterruptedException, TimeoutException {
        // TODO: use getSynced here instead?
        note = repo.getSynced(title);
    // noteAPI.getNote(title);
        // The returned live data should update whenever there is a change in
        // the database, or when the server returns a newer version of the note.
        // Polling interval: 3s.
        if (note == null) {
            note = repo.getLocal(title);
        }
        return note;
    }

    public void save(Note note) throws JSONException {
        // TODO: try to upload the note to the server.
        repo.upsertSynced(note);
        repo.upsertLocal(note);
    }
}
