package com.rudysolano.noted;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Completable;

/**
 * Provides a DAO abstraction layer.
 */
public class NotesRepository {

    private final com.rudysolano.noted.NotesDao notesDao;
    private final LiveData<List<Note>> allNotes;

    /**
     * Gets an instance of {@link NotesDatabase} to get an instance of {@link NotesDao}. Uses DAO
     * to cache all notes from the database.
     * @param application the base class for this application
     */
    public NotesRepository(Application application) {
        NotesDatabase database = NotesDatabase.getInstance(application);
        notesDao = database.notesDao();
        allNotes = notesDao.getAllNotes();

    }

    /**
     * The following methods call the DAO to perform database operations. The operations are
     * explained by the name of the methods.
     */

    public Completable insertNote(Note note) {return notesDao.insertNote(note);}

    public Completable updateNote(Note note) {return notesDao.updateNote(note);}

    public Completable deleteNotesById(long[] ids) {return notesDao.deleteNotesById(ids);}

    public Completable deleteAllNotes() {return notesDao.deleteAllNotes();}

    public LiveData<List<Note>> getAllNotes() {return allNotes;}

    public LiveData<List<Note>> getIdAscendingNotes() {
        return notesDao.getIdAscendingNotes();
    }

    public LiveData<List<Note>> getIdDescendingNotes() {
        return notesDao.getIdDescendingNotes();
    }

    public LiveData<List<Note>> getTagAscendingNotes() {
        return notesDao.getTagAscendingNotes();
    }

    public LiveData<List<Note>> getTagDescendingNotes() {
        return notesDao.getTagDescendingNotes();
    }

}