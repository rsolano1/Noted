package com.rudysolano.noted;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;

/**
 * The MainActivity has limited responsibilities. Outside of showing the {@link NotesFragment}, it
 * handles the creation of the toolbar menu, handles the menu item selections, and handles requests
 * to edit notes by implementing the
 * {@link NotesFragment.OnEditNoteRequestListener} interface and
 * defining its callback method, which in turn shows the {@link AddEditNoteDialogFragment}.
 *
 */
public class MainActivity extends AppCompatActivity
        implements NotesFragment.OnEditNoteRequestListener {

    private com.rudysolano.noted.NotesViewModel mNotesViewModel;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Use the support library's Toolbar instead of the native ActionBar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNotesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        fragmentManager = getSupportFragmentManager();

        //If savedInstanceState is null, add NoteFragment to the Activity.
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, NotesFragment.newInstance())
                    .commit();
        }

        //Set up the FAB to show the AddEditNoteDialogFragment
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((view) -> {
            showDialogFragment(AddEditNoteDialogFragment.newInstance());
        });
    }

    /**
     * Implementation of callback method for
     * {@link NotesFragment.OnEditNoteRequestListener}. Handles the
     * request to edit a note by showing {@link AddEditNoteDialogFragment}.
     * @param id the id for the note being edited
     * @param noteText the note text for the note being edited
     */
    @Override
    public void onEditNoteRequest(int id, String noteText) {
        showDialogFragment(com.rudysolano.noted.AddEditNoteDialogFragment.newInstance(id, noteText));
    }

    /**
     * Show the dialog fragment being requested.
     * @param dialogFragment the dialog fragment to show
     */
    private void showDialogFragment(DialogFragment dialogFragment) {
        dialogFragment.show(fragmentManager, null);
    }

    /**
     * Initialize the contents of the Activity's standard options menu, which will be shown in the
     * toolbar.
     * @param menu the menu being created
     * @return true if you want the menu displayed, false otherwise
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    /**
     * Handle menu item clicks.
     * @param item the selected menu item
     * @return false to allow normal menu processing to proceed, true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Get the id of the menu item that was pressed.
        int id = item.getItemId();

        if (id == R.id.delete_all) {
            //Show dialog to delete all notes.
            showDialogFragment(DeleteAllNotesDialogFragment.newInstance());
        } else if (id == R.id.sort_a_to_z) {
            //Sort notes by tag, A to Z.
            mNotesViewModel.setSortOption(NotesViewModel.SORT_TAG_ASC);
        } else if (id == R.id.sort_z_to_a) {
            //Sort notes by tag, Z to A.
            mNotesViewModel.setSortOption(NotesViewModel.SORT_TAG_DESC);
        } else if (id == R.id.sort_entry_order_ascending) {
            //Sort notes by id, ascending.
            mNotesViewModel.setSortOption(NotesViewModel.SORT_ID_ASC);
        } else if (id == R.id.sort_entry_order_descending) {
            //Sort notes by id, descending.
            mNotesViewModel.setSortOption(NotesViewModel.SORT_ID_DESC);
        }

        return super.onOptionsItemSelected(item);
    }
}