package com.rudysolano.noted;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.RecyclerView;

public class MultiChoiceHelper {
    /**
     * A handy ViewHolder base class which works with the MultiChoiceHelper
     * and reproduces the default behavior of a ListView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        View.OnClickListener clickListener;
        MultiChoiceHelper multiChoiceHelper;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(view -> {
                if (isMultiChoiceActive()) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        multiChoiceHelper.toggleItemActivated(position, false);
                        updateActivatedState(position);
                    }
                } else {
                    if (clickListener != null) {
                        clickListener.onClick(view);
                    }
                }
            });
            itemView.setOnLongClickListener(view -> {
                if ((multiChoiceHelper == null) || isMultiChoiceActive()) {
                    return false;
                }
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    multiChoiceHelper.setItemActivated(position, true, false);
                    updateActivatedState(position);
                }
                return true;
            });
        }

        void updateActivatedState(int position) {
            final boolean isActivated = multiChoiceHelper.isItemActivated(position);
            if (isActivated) {
                itemView.setBackgroundResource(R.drawable.background_selected_note);
            } else {
                itemView.setBackgroundResource(0);
            }
        }

        public void setOnClickListener(View.OnClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public void bind(MultiChoiceHelper multiChoiceHelper, int position) {
            this.multiChoiceHelper = multiChoiceHelper;
            if (multiChoiceHelper != null) {
                updateActivatedState(position);
            }
        }

        public boolean isMultiChoiceActive() {
            return (multiChoiceHelper != null) && (multiChoiceHelper.getActivatedItemCount() > 0);
        }
    }

    public interface MultiChoiceModeListener extends ActionMode.Callback {
        /**
         * Called when an item is activated or deactivated during selection mode.
         *
         * @param mode     The {@link ActionMode} providing the selection startSupportActionMode
         * @param position Adapter position of the item that was activated or deactivated
         * @param id       Adapter ID of the item that was activated or deactivated
         * @param activated  <code>true</code> if the item is now activated, <code>false</code>
         *                 if the item is now deactivated.
         */
        void onItemActivatedStateChanged(ActionMode mode, int position, long id, boolean activated);
    }

    private static final int ACTIVATED_POSITION_SEARCH_DISTANCE = 20;

    private final AppCompatActivity activity;
    private final RecyclerView.Adapter adapter;
    private SparseBooleanArray activatedPositions;
    private LongSparseArray<Integer> activatedIdStates;
    private int activatedItemCount = 0;
    private MultiChoiceModeWrapper multiChoiceModeCallback;
    ActionMode choiceActionMode;

    /**
     * Make sure this constructor is called before setting the adapter on the RecyclerView
     * so this class will be notified before the RecyclerView in case of data set changes.
     */
    public MultiChoiceHelper(@NonNull AppCompatActivity activity,
                             @NonNull RecyclerView.Adapter adapter) {
        this.activity = activity;
        this.adapter = adapter;
        adapter.registerAdapterDataObserver(new AdapterDataSetObserver());
        activatedPositions = new SparseBooleanArray(0);
        if (adapter.hasStableIds()) {
            activatedIdStates = new LongSparseArray<>(0);
        }
    }

    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (listener == null) {
            multiChoiceModeCallback = null;
            return;
        }
        if (multiChoiceModeCallback == null) {
            multiChoiceModeCallback = new MultiChoiceModeWrapper();
        }
        multiChoiceModeCallback.setWrapped(listener);
    }

    public int getActivatedItemCount() {
        return activatedItemCount;
    }

    public boolean isItemActivated(int position) {
        return activatedPositions.get(position);
    }

    public long[] getActivatedItemIds() {
        if (activatedIdStates == null) {
        }
        final LongSparseArray<Integer> idStates = activatedIdStates;
        if (idStates == null) {
            return new long[0];
        }

        final int count = idStates.size();
        final long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }

        return ids;
    }

    public void clearChoices() {
        if (activatedItemCount > 0) {
            final int start = activatedPositions.keyAt(0);
            final int end = activatedPositions.keyAt(activatedPositions.size() - 1);
            activatedPositions.clear();
            if (activatedIdStates != null) {
                activatedIdStates.clear();
            }
            activatedItemCount = 0;

            adapter.notifyItemRangeChanged(start, end - start + 1);

            if (choiceActionMode != null) {
                choiceActionMode.finish();
            }
        }
    }

    public void setItemActivated(int position, boolean value, boolean notifyChanged) {
        // Start selection mode if needed. We don't need to if we're deselecting something.
        if (value) {
            startSupportActionModeIfNeeded();
        }

        boolean oldValue = activatedPositions.get(position);
        activatedPositions.put(position, value);

        if (oldValue != value) {
            final long id = adapter.getItemId(position);

            if (activatedIdStates != null) {
                if (value) {
                    activatedIdStates.put(id, position);
                } else {
                    activatedIdStates.remove(id);
                }
            }

            if (value) {
                activatedItemCount++;
            } else {
                activatedItemCount--;
            }

            if (notifyChanged) {
                adapter.notifyItemChanged(position);
            }

            if (choiceActionMode != null) {
                multiChoiceModeCallback.onItemActivatedStateChanged(choiceActionMode,
                        position, id, value);
                if (activatedItemCount == 0) {
                    choiceActionMode.finish();
                }
            }
        }
    }

    public void toggleItemActivated(int position, boolean notifyChanged) {
        setItemActivated(position, !isItemActivated(position), notifyChanged);
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState();
        savedState.activatedItemCount = activatedItemCount;
        savedState.isPositionActivated = clone(activatedPositions);
        if (activatedIdStates != null) {
            savedState.activatedIdStates = activatedIdStates.clone();
        }
        return savedState;
    }

    private static SparseBooleanArray clone(SparseBooleanArray original) {
        final int size = original.size();
        SparseBooleanArray clone = new SparseBooleanArray(size);
        for (int i = 0; i < size; ++i) {
            clone.append(original.keyAt(i), original.valueAt(i));
        }
        return clone;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if ((state != null) && (activatedItemCount == 0)) {
            SavedState savedState = (SavedState) state;
            activatedItemCount = savedState.activatedItemCount;
            activatedPositions = savedState.isPositionActivated;
            activatedIdStates = savedState.activatedIdStates;

            if (activatedItemCount > 0) {
                // Empty adapter is given a chance to be populated before
                // completeRestoreInstanceState()
                if (adapter.getItemCount() > 0) {
                    confirmActivatedPositions();
                }
                activity.getWindow().getDecorView().post(this::completeRestoreInstanceState);
            }
        }
    }

    void completeRestoreInstanceState() {
        if (activatedItemCount > 0) {
            if (adapter.getItemCount() == 0) {
                // Adapter was not populated, clear the selection
                confirmActivatedPositions();
            } else {
                startSupportActionModeIfNeeded();
            }
        }
    }

    private void startSupportActionModeIfNeeded() {
        if (choiceActionMode == null) {
            if (multiChoiceModeCallback == null) {
                throw new IllegalStateException("No callback set");
            }
            choiceActionMode = activity.startSupportActionMode(multiChoiceModeCallback);
        }
    }

    public static class SavedState implements Parcelable {

        int activatedItemCount;
        SparseBooleanArray isPositionActivated;
        LongSparseArray<Integer> activatedIdStates;

        SavedState() {
        }

        SavedState(Parcel in) {
            activatedItemCount = in.readInt();
            isPositionActivated = in.readSparseBooleanArray();
            final int n = in.readInt();
            if (n >= 0) {
                activatedIdStates = new LongSparseArray<>(n);
                for (int i = 0; i < n; i++) {
                    final long key = in.readLong();
                    final int value = in.readInt();
                    activatedIdStates.append(key, value);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(activatedItemCount);
            out.writeSparseBooleanArray(isPositionActivated);
            final int n = activatedIdStates != null ? activatedIdStates.size() : -1;
            out.writeInt(n);
            for (int i = 0; i < n; i++) {
                out.writeLong(activatedIdStates.keyAt(i));
                out.writeInt(activatedIdStates.valueAt(i));
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    void confirmActivatedPositions() {
        if (activatedItemCount == 0) {
            return;
        }

        final int itemCount = adapter.getItemCount();
        boolean activatedCountChanged = false;

        if (itemCount == 0) {
            // Optimized path for empty adapter: remove all items.
            activatedPositions.clear();
            if (activatedIdStates != null) {
                activatedIdStates.clear();
            }
            activatedItemCount = 0;
            activatedCountChanged = true;
        } else if (activatedIdStates != null) {
            // Clear out the positional activated states, we'll rebuild it below from IDs.
            activatedPositions.clear();

            for (int index = 0; index < activatedIdStates.size(); index++) {
                final long id = activatedIdStates.keyAt(index);
                final int lastPos = activatedIdStates.valueAt(index);

                if ((lastPos >= itemCount) || (id != adapter.getItemId(lastPos))) {
                    // Look around to see if the ID is nearby. If not, deactivate it.
                    final int start = Math.max(0, lastPos - ACTIVATED_POSITION_SEARCH_DISTANCE);
                    final int end = Math.min(lastPos + ACTIVATED_POSITION_SEARCH_DISTANCE,
                            itemCount);
                    boolean found = false;
                    for (int searchPos = start; searchPos < end; searchPos++) {
                        final long searchId = adapter.getItemId(searchPos);
                        if (id == searchId) {
                            found = true;
                            activatedPositions.put(searchPos, true);
                            activatedIdStates.setValueAt(index, searchPos);
                            break;
                        }
                    }

                    if (!found) {
                        activatedIdStates.remove(id);
                        index--;
                        activatedItemCount--;
                        activatedCountChanged = true;
                        if (choiceActionMode != null && multiChoiceModeCallback != null) {
                            multiChoiceModeCallback.onItemActivatedStateChanged(choiceActionMode,
                                    lastPos, id, false);
                        }
                    }
                } else {
                    activatedPositions.put(lastPos, true);
                }
            }
        } else {
            // If the total number of items decreased, remove all out-of-range activated indexes.
            for (int i = activatedPositions.size() - 1; (i >= 0) &&
                    (activatedPositions.keyAt(i) >= itemCount); i--) {
                if (activatedPositions.valueAt(i)) {
                    activatedItemCount--;
                    activatedCountChanged = true;
                }
                activatedPositions.delete(activatedPositions.keyAt(i));
            }
        }

        if (activatedCountChanged && choiceActionMode != null) {
            if (activatedItemCount == 0) {
                choiceActionMode.finish();
            } else {
                choiceActionMode.invalidate();
            }
        }
    }

    class AdapterDataSetObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            confirmActivatedPositions();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            confirmActivatedPositions();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            confirmActivatedPositions();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            confirmActivatedPositions();
        }
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {

        private MultiChoiceModeListener wrapped;

        public void setWrapped(@NonNull MultiChoiceModeListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return wrapped.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return wrapped.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return wrapped.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            wrapped.onDestroyActionMode(mode);
            choiceActionMode = null;
            clearChoices();
        }

        @Override
        public void onItemActivatedStateChanged(ActionMode mode, int position, long id,
                                                boolean activated) {
            wrapped.onItemActivatedStateChanged(mode, position, id, activated);
        }
    }
}
