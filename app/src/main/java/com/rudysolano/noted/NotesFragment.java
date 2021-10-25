package com.rudysolano.noted;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class NotesFragment extends Fragment  {

    private static final String STATE_ADAPTER = "adapter";

    private RecyclerView mRecyclerView;
    private View mEmptyStateView;
    private OnEditNoteRequestListener onEditNoteRequestListener;
    private com.rudysolano.noted.NotesAdapter notesAdapter;
    private com.rudysolano.noted.NotesViewModel notesViewModel;

    public NotesFragment() {
        // Required empty public constructor
    }

    /**Create new instance of the fragment **/
    public static NotesFragment newInstance() {
        return new NotesFragment();
    }

    /**
     * Ensures that the host Activity implements the fragment's listener interface.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnEditNoteRequestListener) {
            onEditNoteRequestListener = (OnEditNoteRequestListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + getResources().getString(R.string.error_listener_implementation_missing));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retrieve shared instance of the view model
        notesViewModel = new ViewModelProvider(requireActivity()).get(NotesViewModel.class);
        //Instantiate adapter
        notesAdapter = new com.rudysolano.noted.NotesAdapter((AppCompatActivity) getActivity(),
                notesViewModel);
        if (savedInstanceState != null) {
            notesAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_ADAPTER));
        }
        //Implement the adapter's listener, which will handle navigating to the next fragment when
        // the user wants to edit a note.
        notesAdapter.setOnNoteClickListener((note) -> {
            //Call listener callback to handle navigating to next fragment
            onEditNoteRequestListener.onEditNoteRequest(
                    note.getId(), note.getNoteText());
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_notes, container,
                false);

        //Instantiate and configure RecyclerView
        mRecyclerView = rootView.findViewById(R.id.recyler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        //Instantiate empty state view
        mEmptyStateView = rootView.findViewById(R.id.empty_state);

        //Make all view invisible for now
        setShowViews(false, false);

        //Set the adapter
        mRecyclerView.setAdapter(notesAdapter);

        //Set up observer on notes data
        notesViewModel.getAllNotes().observe(getActivity(), new Observer<List<Note>>() {
            /**
             * Update the RecyclerView upon data change. Show appropriate views.
             */
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                //Upon data change, if notes list contains at least one note, then show the
                // RecyclerView, hide the empty state view and update ui. Else, hide the
                // RecyclerView and show the empty state view.
                if (!notes.isEmpty()) {
                    setShowViews(true, false);
                    notesAdapter.submitList(notes);
                } else {
                    setShowViews(false, true);
                }
            }
        });

        //return fragment layout
        return rootView;
    }

    private void setShowViews(boolean showRecyclerView, boolean showEmptyState) {
        if (showRecyclerView) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyStateView.setVisibility(View.GONE);
        } else if (showEmptyState) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyStateView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * An interface to listen for when the user wants to edit a note. To be implemented by host.
     */
    public interface OnEditNoteRequestListener {
        void onEditNoteRequest(int id, String noteText);
    }

    @Override
    public void onDestroyView() {
        notesAdapter.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_ADAPTER, notesAdapter.onSaveInstanceState());
    }
}