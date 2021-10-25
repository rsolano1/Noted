package com.rudysolano.noted;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Database(entities = {Note.class}, version = 1)
public abstract class NotesDatabase extends RoomDatabase {

    //An instance of the database. Used in getInstance(), where an instance of the database is
    // created
    private static NotesDatabase instance;

    //Abstract method used to access DAO. Room library will take care of code behind this method.
    public abstract NotesDao notesDao();

    /**
     * Create an instance of the database. If the version is updated, the old database will simply
     * be destroyed and a new database created (fallbackToDestructiveMigration). Synchronized means
     * that only one thread at a time can access this method, so multiple instances of the database
     * will not be accidentally created.
     */
    public static synchronized NotesDatabase getInstance(Context context) {
        //If an instance of the database does not exist, create a new instance
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NotesDatabase.class, "notes_database")
                    //Add callback that will populate database asynchronously
                    .addCallback(roomCallback)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        //Return the newly created or already existing instance of the database
        return instance;
    }

    /**
     * A RoomDatabase.Callback which will populate the database upon creation.
     */
    private final static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            populateDatabase(instance);
        }
    };

    private static void populateDatabase(NotesDatabase db) {
        NotesDao notesDao = db.notesDao();
        ArrayList<Note> initialNotes = new ArrayList<>();

        //Populate ArrayList with notes that will be used to populate database. Make sure that each
        // note has a hashtag, and that the tag matches the hashtag.
        Note note1 = new Note("Include one #hashtag per note to categorize it",
                "hashtag");
        note1.setId(1);

        Note note2 = new Note("For example: I need to #buy new t-shirts", "buy");
        note2.setId(2);

        Note note3 = new Note("You can #organize your notes using the sort " +
                "icon at the top", "organize");
        note3.setId(3);

        initialNotes.add(note1);
        initialNotes.add(note2);
        initialNotes.add(note3);

        for (Note note : initialNotes) {
            //Call the dao to insert the note, using Completable.
            notesDao.insertNote(note).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {

                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                            //do nothing
                        }

                        @Override
                        public void onComplete() {
                            //do nothing
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            //do nothing
                        }
                    });
        }
    }
}