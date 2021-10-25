package com.rudysolano.noted;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A dialog fragment used to add a new note or edit an existing note. There are two helper methods
 * used to create a new instance of this fragment: newInstance() and
 * newInstance(int id, String noteText). The former is used to create a fragment instance in
 * "new note" mode. The latter is used for "edit" mode and the arguments supplied to this method
 * contain information about the note being edited. In onCreate(), the fragment decides whether to
 * operate in "edit" or "new note" mode based on whether arguments were supplied to the fragment. In
 * edit mode, this information is displayed in the fragment's EditText field in onCreateDialog().
 * When the user attempts to save either a new note or an edited note via the Save button, the
 * user entry is validated. If the entry is invalid, an error is displayed. Otherwise, the entry
 * is saved using a shared ViewModel. Note that a listener is constructed to process a Save
 * button click, and this listener must be defined onResume() to allow the dialog fragment to
 * remain open in instances where a user entry is invalid.
 */
public class AddEditNoteDialogFragment extends DialogFragment {

    //Variables to indicate what mode the dialog fragment is operating in - either editing a note,
    // or adding a new note.
    private static final String EDIT_MODE = "editMode";
    private static final String NEW_NOTE_MODE = "newNoteMode";
    //Keys to be used in Bundle to store data that was passed in (used in edit mode).
    private static final String KEY_NOTE_ID = "keyNoteId";
    private static final String KEY_NOTE_TEXT = "keyNoteText";

    private int editNoteId;
    private String editNoteText;
    private String mode;
    private EditText editText;

    /**
     * Required empty constructor.
     */
    public AddEditNoteDialogFragment() {
        //empty constructor
    }

    /**
     * Helper method used to create a new instance of this fragment in "new note" mode. The method
     * simply calls this fragment's empty constructor.
     *
     * @return new instance of this fragment
     */
    public static AddEditNoteDialogFragment newInstance() {
        return new AddEditNoteDialogFragment();
    }

    /**
     * Helper method used to create a new instance of this fragment in "edit" mode. This fragment's
     * empty constructor is called, and the fragment is supplied with arguments, which will contain
     * information about the note being edited.
     *
     * @param id       the note id for the note being edited
     * @param noteText the note text for the note being edited
     * @return a new instance of this fragment
     */
    public static AddEditNoteDialogFragment newInstance(@NonNull int id, @NonNull String noteText) {
        AddEditNoteDialogFragment fragment = new AddEditNoteDialogFragment();
        //Store the note id and text in a Bundle.
        Bundle args = new Bundle();
        args.putInt(KEY_NOTE_ID, id);
        args.putString(KEY_NOTE_TEXT, noteText);
        //Supply the Bundle to the fragment.
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Sets the mode variable to indicate whether this fragment is in New Note mode or Edit mode by
     * checking if arguments were supplied. If arguments were supplied, fragment is in Edit mode,
     * and the id and text for the note being edited are stored in variables.
     *
     * @param savedInstanceState the state used when recreating the fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            //Fragment is in New Note mode.
            mode = NEW_NOTE_MODE;
        } else {
            //Fragment is in Edit mode.
            mode = EDIT_MODE;

            //Get the note data from the bundle.
            editNoteId = getArguments().getInt(KEY_NOTE_ID);
            editNoteText = getArguments().getString(KEY_NOTE_TEXT);
        }
    }

    /**
     * Set up an alert dialog to allow the user to add a new note or edit an existing note.
     *
     * @param savedInstanceState the state used for recreation.
     * @return the dialog being shown to the user
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Inflate a custom layout. Get its EditText to display the note to be edited, if the
        // fragment is in Edit mode.
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.fragment_add_edit_note_dialog, null,
                false);
        editText = layout.findViewById(R.id.edit_text);

        //Create and configure an alert dialog to allow user to edit/create new note.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout);
        //Other configurations based on current mode.
        if (mode.equals(EDIT_MODE)) {
            editText.setText(editNoteText);
            builder.setTitle(R.string.edit_note_title);
            builder.setMessage(R.string.edit_note_explanation);
        } else {
            builder.setTitle(R.string.add_note_title);
            builder.setMessage(R.string.add_new_note_explanation);
        }

        //Set dialog buttons (Save and Cancel). Both button listeners are set to null. Nothing needs
        // to be done upon negative button click, so setting it to null is ok. For positive button,
        // click listener will be placed in onResume() to prevent dialog from automatically
        // dismissing upon an event (For example, if user presses Save with an invalid entry, the
        // dialog needs to stay open. If the listener was not set to null here, the dialog would
        // simply close).
        builder.setPositiveButton(R.string.save_button, null);
        builder.setNegativeButton(R.string.cancel_button, null);

        return builder.create();
    }

    /**
     * Defines the listener for alert dialog's Save button. The listener needs to be defined here so
     * that the dialog is not simply closed when the user presses save. The listener validates the
     * user's entry and, if the entry is valid, either creates a new note or edits the current note
     * (depending on the current mode) using a shared ViewModel and the dialog is closed. If the
     * entry is invalid, the dialog remains open and an error message is shown to the user.
     */
    @Override
    public void onResume() {
        super.onResume();

        //Get the currently shown alert dialog.
        final AlertDialog dialog = (AlertDialog) getDialog();

        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            //Define the positive (save) button listener.
            positiveButton.setOnClickListener((view) -> {
                boolean closeDialog = false;

                //Get EditText's text and validate it.
                String noteEntry = editText.getText().toString().trim();
                int entryValidity = ProcessTextUtils.validateEntry(noteEntry);

                //Switch statements handles entry validity cases.
                switch (entryValidity) {
                    case ProcessTextUtils.ENTRY_VALID:
                        //Get the hashtag from the note and remove the hash. Will allow the tag
                        // to be saved in the database without a hash.
                        String hashTag = ProcessTextUtils.getHashtag(noteEntry);
                        String tag = ProcessTextUtils.removeHash(hashTag);

                        //Set the note as a new note, regardless of mode. If dialog is in Edit
                        // mode, the note's id will be set later.
                        Note newNote = new Note(noteEntry, tag);

                        NotesViewModel notesViewModel =
                                new ViewModelProvider(getActivity()).get(NotesViewModel.class);

                        if (mode.equals(EDIT_MODE)) {
                            //Set the new note's id to the id of the note being edited.
                            newNote.setId(editNoteId);

                            //Call the shared ViewModel to update the note, using Completable.
                            notesViewModel.updateNote(newNote).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CompletableObserver() {

                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                                            //do nothing
                                        }

                                        @Override
                                        public void onComplete() {
                                            Toast.makeText(getActivity(), R.string.note_updated,
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            //do nothing
                                        }
                                    });
                        } else {
                            //Call the ViewModel to insert the note, using Completable.
                            notesViewModel.insertNote(newNote).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new CompletableObserver() {

                                        @Override
                                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                                            //do nothing
                                        }

                                        @Override
                                        public void onComplete() {
                                            Toast.makeText(getActivity(), R.string.note_inserted,
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                            //do nothing
                                        }
                                    });
                        }

                        closeDialog = true;

                        break;
                    //Handle all invalid entry cases.
                    case ProcessTextUtils.ENTRY_EMPTY:
                        editText.setError(getString(R.string.error_note_text_entry_blank));
                        break;
                    case ProcessTextUtils.ENTRY_MISSING_HASH:
                        editText.setError(getString(R.string.error_hashtag_missing));
                        break;
                    case ProcessTextUtils.ENTRY_HAS_MULTIPLE_HASHES:
                        editText.setError(getString(R.string.error_multiple_hashtags));
                        break;
                    case ProcessTextUtils.INVALID_HASHTAG:
                        editText.setError(getString(R.string.error_invalid_hashtag));
                    default:
                        editText.setError(getString(R.string.error_invalid_entry));
                        break;
                }

                if (closeDialog) {
                    dialog.dismiss();
                }
            });
        }
    }
}