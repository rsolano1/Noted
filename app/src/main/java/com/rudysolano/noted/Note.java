package com.rudysolano.noted;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A Note object. Uses Room annotations to work with SQLite database.
 */
@Entity(tableName ="notes_table")
public class Note {

    //Set the column tables
    @PrimaryKey(autoGenerate = true)    //set id as primary key
    private int id;
    @ColumnInfo(name = "note_text")
    private final String noteText;
    @ColumnInfo(name = "tag")
    private final String tag;

    //Constructor
    public Note(@NonNull String noteText, @NonNull String tag) {
        this.noteText = noteText;
        this.tag = tag;
    }

    //Getters and setters

    public int getId() {
        return id;
    }

    public String getNoteText() {
        return noteText;
    }

    public String getTag() {
        return tag;
    }

    public void setId(int id) {
        this.id = id;
    }
}