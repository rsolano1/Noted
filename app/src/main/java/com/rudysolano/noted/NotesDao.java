package com.rudysolano.noted;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;

/**
 * DAO class. Defines all database operations and provides a SQLite database abstraction layer.
 */
@Dao
public interface NotesDao {

    @Insert
    Completable insertNote(Note note);

    @Update
    Completable updateNote(Note note);

    @Query("DELETE FROM notes_table")
    Completable deleteAllNotes();

    @Query("DELETE FROM notes_table WHERE id IN (:ids)")
    Completable deleteNotesById(long[] ids);

    @Query("SELECT * FROM notes_table")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes_table ORDER BY id ASC")
    LiveData<List<Note>> getIdAscendingNotes();

    @Query("SELECT * FROM notes_table ORDER BY id DESC")
    LiveData<List<Note>> getIdDescendingNotes();

    @Query("SELECT * FROM notes_table ORDER BY tag DESC")
    LiveData<List<Note>> getTagDescendingNotes();

    @Query("SELECT * FROM notes_table ORDER BY tag ASC")
    LiveData<List<Note>> getTagAscendingNotes();
}
