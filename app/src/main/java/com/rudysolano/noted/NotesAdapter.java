package com.rudysolano.noted;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotesAdapter extends ListAdapter<Note, NotesAdapter.ViewHolder> {

    private static OnNoteClickListener mListener;
    private Context context;
    final com.rudysolano.noted.MultiChoiceHelper multiChoiceHelper;

    protected NotesAdapter(AppCompatActivity activity, NotesViewModel notesViewModel) {
        super(DIFF_CALLBACK);

        setHasStableIds(true);

        //Initialize class that makes multi-choice functionality possible
        multiChoiceHelper = new MultiChoiceHelper(activity, this);
        //Implement a listener to define what happens in multi-choice mode
        multiChoiceHelper.setMultiChoiceModeListener(new MultiChoiceHelper.MultiChoiceModeListener() {
            /**
             * Called when action mode is first created. The menu supplied will be used to
             * generate action buttons for the action mode.
             *
             * @param mode ActionMode being created
             * @param menu Menu used to populate action buttons
             * @return true if the action mode should be created, false if entering this
             *              mode should be aborted.
             */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //Inflate the menu for multi choice mode
                mode.getMenuInflater().inflate(R.menu.menu_contextual, menu);
                return true;
            }

            /**
             * Set the current ActionMode's menu title to display the number of items that are
             * currently activated.
             * @param mode The current ActionMode
             */
            private void updateSelectedCountDisplay(ActionMode mode) {
                int count = multiChoiceHelper.getActivatedItemCount();
                mode.setTitle(Integer.toString(count));
            }

            /**
             * Called to refresh an action mode's action menu whenever it is invalidated. In this
             * case, the count of activated items, which is shown in the menu, is updated.
             * @param mode ActionMode being prepared
             * @param menu Menu used to populate action buttons
             * @return true if the menu or action mode was updated, false otherwise.
             */
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                updateSelectedCountDisplay(mode);
                return true;
            }

            /**
             * Called to handle menu button clicks in the current ActionMode. The only menu
             * option is a delete option, the the callback handles deleting all activated notes.
             * @param mode The current ActionMode
             * @param item The item that was clicked
             * @return true if this callback handled the event, false if the standard MenuItem
             *         invocation should continue.
             */
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {
                    //Get the ids for all records that were selected
                    long[] selectedRecordIds = multiChoiceHelper.getActivatedItemIds();

                    //Call ViewModel to delete records
                    notesViewModel.deleteNotesById(selectedRecordIds)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CompletableObserver() {

                                @Override
                                public void onSubscribe(
                                        @io.reactivex.annotations.NonNull Disposable d) {
                                    //do nothing
                                }

                                @Override
                                public void onComplete() {
                                    Toast.makeText(context, R.string.selected_notes_deleted,
                                            Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                                    //do nothing
                                }
                            });

                    mode.finish();
                    return true;
                }

                return false;
            }

            /**
             * Called when an item is activated or deactivated during selection mode. This
             * implementation will update the number of activated notes, which is shown in the menu.
             * @param mode     The {@link ActionMode} providing the selection startSupportActionMode
             * @param position Adapter position of the item that was activated or deactivated
             * @param id       Adapter ID of the item that was activated or deactivated
             * @param activated  true if the item is now activated, false if the item is now
             *                   deactivated.
             */
            @Override
            public void onItemActivatedStateChanged(ActionMode mode, int position, long id,
                                                    boolean activated) {
                updateSelectedCountDisplay(mode);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
    }

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Note>() {

                @Override
                public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.getNoteText().equals(newItem.getNoteText())
                            && oldItem.getTag().equals(newItem.getTag());
                }
            };

    class ViewHolder extends MultiChoiceHelper.ViewHolder implements View.OnClickListener {

        private final TextView mTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.note_text);
            setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (mListener != null && position != RecyclerView.NO_POSITION) {
                mListener.onNoteClick(getItem(position));
            }
        }
    }

    /**
     * Create a view from the item view layout file, and use it to create and return a new
     * ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list_item,
                parent, false);

        return new NotesAdapter.ViewHolder(itemView);
    }

    /**
     * Get the Note item whose position in the data set is equal to the argument "position," and
     * populate the ViewHolder Views with data from that Note item.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Get the next Note item and it's column values.
        Note note = getItem(position);
        String noteText = note.getNoteText();
        String tag = note.getTag();

        //Populate ViewHolder TextView with note text, with a formatted tag.
        holder.mTextView.setText(ProcessTextUtils.formatSubstring(noteText, tag, context));

        // Enable MultiChoice selection and update checked state
        holder.bind(multiChoiceHelper, position);
    }

    /**
     * Override to obtain context.
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
    }

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        mListener = listener;
    }

    public Parcelable onSaveInstanceState() {
        return multiChoiceHelper.onSaveInstanceState();
    }

    public void onRestoreInstanceState(Parcelable state) {
        multiChoiceHelper.onRestoreInstanceState(state);
    }

    public void onDestroyView() {
        multiChoiceHelper.clearChoices();
    }

    /**
     * Return the position within the adapter when requesting id.
     */
    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
}