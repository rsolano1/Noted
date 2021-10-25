package com.rudysolano.noted;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A dialog fragment used to confirm that all notes should be deleted. If the user confirms, then
 * a shared ViewModel is called to process the delete operation.
 */
public class DeleteAllNotesDialogFragment extends DialogFragment {

    public DeleteAllNotesDialogFragment() {
        //empty constructor
    }

    /**
     * Helper method to create a new instance of this fragment by calling the constructor.
     * @return new instance of this fragment.
     */
    public static DeleteAllNotesDialogFragment newInstance() {
        return new DeleteAllNotesDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_all_confirm_title)
                .setMessage(R.string.delete_all_confirm_message)
                .setPositiveButton(R.string.delete_button, (dialogInterface, i) -> {
                        NotesViewModel notesViewModel =
                                new ViewModelProvider(getActivity()).get(NotesViewModel.class);

                        //Call a shared ViewModel to delete all notes, using a Completable.
                        notesViewModel.deleteAllNotes().subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new CompletableObserver() {

                                    @Override
                                    public void onSubscribe(
                                            @io.reactivex.annotations.NonNull Disposable d) {
                                        //do nothing
                                    }

                                    @Override
                                    public void onComplete() {
                                        Toast.makeText(getActivity(), R.string.all_notes_deleted,
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(
                                            @io.reactivex.annotations.NonNull Throwable e) {
                                        //do nothing
                                    }
                                });
                })
                .setNegativeButton(R.string.cancel_button, null);

        return builder.create();
    }
}