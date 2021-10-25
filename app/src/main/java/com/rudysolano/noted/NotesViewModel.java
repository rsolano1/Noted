package com.rudysolano.noted;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.reactivex.Completable;

public class NotesViewModel extends AndroidViewModel {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SORT_TAG_ASC, SORT_TAG_DESC, SORT_ID_ASC, SORT_ID_DESC})
    public @interface SortChoices {}
    // Initialize data validation constants
    public static final String SORT_TAG_ASC = "sortTagAsc";
    public static final String SORT_TAG_DESC = "sortTagDesc";
    public static final String SORT_ID_ASC = "sortIdAsc";
    public static final String SORT_ID_DESC = "sortIdDesc";

    private LiveData<List<Note>> mNotesList;
    private MutableLiveData<String> mSortChoice;    //holds the current sort choice
    private final NotesRepository mNotesRepository;

    public NotesViewModel(@NonNull Application application) {
        super(application);

        //Instantiate repository, Note list, and sort choice.
        mNotesRepository = new NotesRepository(application);
        mNotesList = mNotesRepository.getAllNotes();
        mSortChoice = new MutableLiveData<>();
        mSortChoice.setValue(SORT_ID_ASC);

        //Use switchMap to reassign the value of Notes list when sort choice is updated.
        mNotesList = Transformations.switchMap(mSortChoice,
                (Function<String, LiveData<List<Note>>>) input -> {
            switch (input) {
                case SORT_ID_ASC:
                    return mNotesRepository.getIdAscendingNotes();
                case SORT_ID_DESC:
                    return mNotesRepository.getIdDescendingNotes();
                case SORT_TAG_ASC:
                    return mNotesRepository.getTagAscendingNotes();
                case SORT_TAG_DESC:
                    return mNotesRepository.getTagDescendingNotes();
                default:
                    return mNotesRepository.getAllNotes();
            }
        });
    }

    public LiveData<List<Note>> getAllNotes() {
        return mNotesList;
    }

    public Completable insertNote(Note note) {
        return mNotesRepository.insertNote(note);
    }

    public Completable updateNote(Note note) {
        return mNotesRepository.updateNote(note);
    }

    public Completable deleteNotesById(long[] ids) {return mNotesRepository.deleteNotesById(ids);}

    public Completable deleteAllNotes() {
        return mNotesRepository.deleteAllNotes();
    }

    public void setSortOption(@SortChoices String sortChoice) {
        mSortChoice.setValue(sortChoice);
    }
}
