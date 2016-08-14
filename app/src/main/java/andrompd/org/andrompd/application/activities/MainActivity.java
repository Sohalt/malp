/*
 * Copyright (C) 2016  Hendrik Borghorst
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package andrompd.org.andrompd.application.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;


import andrompd.org.andrompd.R;
import andrompd.org.andrompd.application.ConnectionManager;
import andrompd.org.andrompd.application.callbacks.FABFragmentCallback;
import andrompd.org.andrompd.application.callbacks.OnSaveDialogListener;
import andrompd.org.andrompd.application.callbacks.ProfileManageCallbacks;
import andrompd.org.andrompd.application.fragments.EditProfileFragment;
import andrompd.org.andrompd.application.fragments.ProfilesFragment;
import andrompd.org.andrompd.application.fragments.SaveDialog;
import andrompd.org.andrompd.application.fragments.SettingsFragment;
import andrompd.org.andrompd.application.fragments.database.AlbumTracksFragment;
import andrompd.org.andrompd.application.fragments.database.AlbumsFragment;
import andrompd.org.andrompd.application.fragments.database.ArtistsFragment;
import andrompd.org.andrompd.application.fragments.database.FilesFragment;
import andrompd.org.andrompd.application.fragments.database.MyMusicTabsFragment;
import andrompd.org.andrompd.application.fragments.database.OutputsFragment;
import andrompd.org.andrompd.application.fragments.database.PlaylistTracksFragment;
import andrompd.org.andrompd.application.fragments.database.SavedPlaylistsFragment;
import andrompd.org.andrompd.application.utils.ThemeUtils;
import andrompd.org.andrompd.application.views.CurrentPlaylistView;
import andrompd.org.andrompd.application.views.NowPlayingView;
import andrompd.org.andrompd.mpdservice.handlers.serverhandler.MPDQueryHandler;
import andrompd.org.andrompd.mpdservice.mpdprotocol.mpdobjects.MPDFile;
import andrompd.org.andrompd.mpdservice.profilemanagement.MPDProfileManager;
import andrompd.org.andrompd.mpdservice.profilemanagement.MPDServerProfile;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AlbumsFragment.AlbumSelectedCallback, ArtistsFragment.ArtistSelectedCallback,
        ProfileManageCallbacks, SavedPlaylistsFragment.SavedPlaylistsCallback,
        NowPlayingView.NowPlayingDragStatusReceiver, OnSaveDialogListener, FilesFragment.FilesCallback,
        FABFragmentCallback {


    private static final String TAG = "MainActivity";

    private final static String MAINACTIVITY_INTENT_EXTRA_REQUESTEDVIEW = "org.malp.requestedview";
    private final static String MAINACTIVITY_INTENT_EXTRA_REQUESTEDVIEW_NOWPLAYINGVIEW = "org.malp.requestedview.nowplaying";

    private final static String MAINACTIVITY_SAVED_INSTANCE_NOW_PLAYING_DRAG_STATUS = "MainActivity.NowPlayingDragStatus";
    private final static String MAINACTIVITY_SAVED_INSTANCE_NOW_PLAYING_VIEW_SWITCHER_CURRENT_VIEW = "MainActivity.NowPlayingViewSwitcherCurrentView";

    private DRAG_STATUS mNowPlayingDragStatus;
    private DRAG_STATUS mSavedNowPlayingDragStatus = null;

    private ActionBarDrawerToggle mDrawerToggle;

    private VIEW_SWITCHER_STATUS mNowPlayingViewSwitcherStatus;
    private VIEW_SWITCHER_STATUS mSavedNowPlayingViewSwitcherStatus = null;

    private MPDProfileManager mProfileManager;


    private FloatingActionButton mFAB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore drag state
        if (savedInstanceState != null) {
            mSavedNowPlayingDragStatus = DRAG_STATUS.values()[savedInstanceState.getInt(MAINACTIVITY_SAVED_INSTANCE_NOW_PLAYING_DRAG_STATUS)];
            mSavedNowPlayingViewSwitcherStatus = VIEW_SWITCHER_STATUS.values()[savedInstanceState.getInt(MAINACTIVITY_SAVED_INSTANCE_NOW_PLAYING_VIEW_SWITCHER_CURRENT_VIEW)];
        }

        // Read theme preference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = sharedPref.getString("pref_theme", "indigo");

        switch (themePref) {
            case "indigo":
                setTheme(R.style.AppTheme_indigo);
                break;
            case "orange":
                setTheme(R.style.AppTheme_orange);
                break;
            case "deeporange":
                setTheme(R.style.AppTheme_deepOrange);
                break;
            case "blue":
                setTheme(R.style.AppTheme_blue);
                break;
            case "darkgrey":
                setTheme(R.style.AppTheme_darkGrey);
                break;
            case "brown":
                setTheme(R.style.AppTheme_brown);
                break;
            case "lightgreen":
                setTheme(R.style.AppTheme_lightGreen);
                break;
        }


        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // enable back navigation
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setCheckedItem(R.id.nav_library);
        }


        if (findViewById(R.id.fragment_container) != null) {
            Fragment fragment = new MyMusicTabsFragment();

            Bundle args = new Bundle();
            args.putInt(MyMusicTabsFragment.MY_MUSIC_REQUESTED_TAB, 0);

            fragment.setArguments(args);


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }


        mFAB = (FloatingActionButton) findViewById(R.id.andrompd_play_button);


        mProfileManager = new MPDProfileManager(getApplicationContext());
        MPDServerProfile autoProfile = mProfileManager.getAutoconnectProfile();


        if (null != autoProfile) {
            Log.v(TAG, "Auto connect profile with statemonitoring: " + autoProfile);
            ConnectionManager.setParameters(autoProfile.getHostname(), autoProfile.getPassword(), autoProfile.getPort());
        }

        registerForContextMenu(findViewById(R.id.main_listview));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mNowPlayingDragStatus == DRAG_STATUS.DRAGGED_UP) {
            NowPlayingView nowPlayingView = (NowPlayingView) findViewById(R.id.now_playing_layout);
            if (nowPlayingView != null) {
                View coordinatorLayout = findViewById(R.id.main_coordinator_layout);
                coordinatorLayout.setVisibility(View.VISIBLE);
                nowPlayingView.minimize();
            }
        } else {
            super.onBackPressed();

            // enable navigation bar when backstack empty
            if (fragmentManager.getBackStackEntryCount() == 0) {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (item.getItemId()) {
            case android.R.id.home:
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    onBackPressed();
                } else {
                    // back stack empty so enable navigation drawer

                    mDrawerToggle.setDrawerIndicatorEnabled(true);

                    if (mDrawerToggle.onOptionsItemSelected(item)) {
                        return true;
                    }
                }
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.main_listview && mNowPlayingDragStatus == DRAG_STATUS.DRAGGED_UP) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu_current_playlist_track, menu);
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (info == null) {
            return super.onContextItemSelected(item);
        }

        CurrentPlaylistView currentPlaylistView = (CurrentPlaylistView) findViewById(R.id.now_playing_playlist);

        if (currentPlaylistView != null && mNowPlayingDragStatus == DRAG_STATUS.DRAGGED_UP) {
            switch (item.getItemId()) {
                case R.id.action_song_play_next:
                    MPDQueryHandler.playIndexAsNext(info.position);
                    return true;
                case R.id.action_remove_song:
                    MPDQueryHandler.removeSongFromCurrentPlaylist(info.position);
                    return true;
                case R.id.action_show_artist:
                    onArtistSelected(((MPDFile)currentPlaylistView.getItem(info.position)).getTrackArtist());
                    return true;
                case R.id.action_show_album:
                    onAlbumSelected(((MPDFile)currentPlaylistView.getItem(info.position)).getTrackAlbum(),"");
                    return true;
            }
        }
        return false;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.v(TAG, "Navdrawer item selected");
        View coordinatorLayout = findViewById(R.id.main_coordinator_layout);
        coordinatorLayout.setVisibility(View.VISIBLE);

        NowPlayingView nowPlayingView = (NowPlayingView) findViewById(R.id.now_playing_layout);
        if (nowPlayingView != null) {
            nowPlayingView.minimize();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        // clear backstack
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        Fragment fragment = null;
        String fragmentTag = "";

        if (id == R.id.nav_library) {
            // Handle the camera action
            fragment = new MyMusicTabsFragment();
            fragmentTag = MyMusicTabsFragment.TAG;
        } else if (id == R.id.nav_saved_playlists) {
            fragment = new SavedPlaylistsFragment();
            fragmentTag = SavedPlaylistsFragment.TAG;
        } else if (id == R.id.nav_files) {
            fragment = new FilesFragment();
            fragmentTag = FilesFragment.TAG;

            Bundle args = new Bundle();
            args.putString(FilesFragment.EXTRA_FILENAME, "");

        } else if (id == R.id.nav_profiles) {
            fragment = new ProfilesFragment();
            fragmentTag = ProfilesFragment.TAG;
        } else if (id == R.id.nav_app_settings) {
            fragment = new SettingsFragment();
            fragmentTag = SettingsFragment.TAG;
        } else if (id == R.id.nav_outputs) {
            fragment = new OutputsFragment();
            fragmentTag = OutputsFragment.TAG;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        // Do the actual fragment transaction
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        transaction.commit();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        NowPlayingView nowPlayingView = (NowPlayingView) findViewById(R.id.now_playing_layout);
        if (nowPlayingView != null) {


            nowPlayingView.registerDragStatusReceiver(this);

            /*
             * Check if the activity got an extra in its intend to show the nowplayingview directly.
             * If yes then pre set the dragoffset of the draggable helper.
             */
            Intent resumeIntent = getIntent();
            if (resumeIntent != null && resumeIntent.getExtras() != null && resumeIntent.getExtras().getString(MAINACTIVITY_INTENT_EXTRA_REQUESTEDVIEW) != null &&
                    resumeIntent.getExtras().getString(MAINACTIVITY_INTENT_EXTRA_REQUESTEDVIEW).equals(MAINACTIVITY_INTENT_EXTRA_REQUESTEDVIEW_NOWPLAYINGVIEW)) {
                nowPlayingView.setDragOffset(0.0f);
                getIntent().removeExtra(MAINACTIVITY_INTENT_EXTRA_REQUESTEDVIEW);
            } else {
                // set drag status
                if (mSavedNowPlayingDragStatus == DRAG_STATUS.DRAGGED_UP) {
                    nowPlayingView.setDragOffset(0.0f);
                } else if (mSavedNowPlayingDragStatus == DRAG_STATUS.DRAGGED_DOWN) {
                    nowPlayingView.setDragOffset(1.0f);
                }
                mSavedNowPlayingDragStatus = null;

                // set view switcher status
                if (mSavedNowPlayingViewSwitcherStatus != null) {
                    nowPlayingView.setViewSwitcherStatus(mSavedNowPlayingViewSwitcherStatus);
                    mNowPlayingViewSwitcherStatus = mSavedNowPlayingViewSwitcherStatus;
                }
                mSavedNowPlayingViewSwitcherStatus = null;
            }
            nowPlayingView.onResume();
        }
        ConnectionManager.reconnectLastServer();


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");

        NowPlayingView nowPlayingView = (NowPlayingView) findViewById(R.id.now_playing_layout);
        if (nowPlayingView != null) {
            nowPlayingView.registerDragStatusReceiver(null);

            nowPlayingView.onPause();
        }

        if (!isChangingConfigurations()) {
            // Disconnect from MPD server
            ConnectionManager.disconnectFromServer();
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // save drag status of the nowplayingview
        savedInstanceState.putInt(MAINACTIVITY_SAVED_INSTANCE_NOW_PLAYING_DRAG_STATUS, mNowPlayingDragStatus.ordinal());

        // save the cover/playlist view status of the nowplayingview
        savedInstanceState.putInt(MAINACTIVITY_SAVED_INSTANCE_NOW_PLAYING_VIEW_SWITCHER_CURRENT_VIEW, mNowPlayingViewSwitcherStatus.ordinal());
    }

    @Override
    public void onAlbumSelected(String albumname, String artistname) {
        Log.v(TAG, "Album selected: " + albumname + ":" + artistname);

        if (mNowPlayingDragStatus == DRAG_STATUS.DRAGGED_UP ) {
            NowPlayingView nowPlayingView =(NowPlayingView)  findViewById(R.id.now_playing_layout);
            if (nowPlayingView != null) {
                View coordinatorLayout = findViewById(R.id.main_coordinator_layout);
                coordinatorLayout.setVisibility(View.VISIBLE);
                nowPlayingView.minimize();
            }
        }

        // Create fragment and give it an argument for the selected article
        AlbumTracksFragment newFragment = new AlbumTracksFragment();
        Bundle args = new Bundle();
        args.putString(AlbumTracksFragment.BUNDLE_STRING_EXTRA_ALBUMNAME, albumname);
        args.putString(AlbumTracksFragment.BUNDLE_STRING_EXTRA_ARTISTNAME, artistname);

        newFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack so the user can navigate
        // back
        transaction.replace(R.id.fragment_container, newFragment, AlbumTracksFragment.TAG);
        transaction.addToBackStack("AlbumTracksFragment");

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onArtistSelected(String artistname) {
        Log.v(TAG, "Artist selected: " + artistname);

        if (mNowPlayingDragStatus == DRAG_STATUS.DRAGGED_UP ) {
            NowPlayingView nowPlayingView =(NowPlayingView)  findViewById(R.id.now_playing_layout);
            if (nowPlayingView != null) {
                View coordinatorLayout = findViewById(R.id.main_coordinator_layout);
                coordinatorLayout.setVisibility(View.VISIBLE);
                nowPlayingView.minimize();
            }
        }
        // Create fragment and give it an argument for the selected article
        AlbumsFragment newFragment = new AlbumsFragment();
        Bundle args = new Bundle();
        args.putString(AlbumsFragment.BUNDLE_STRING_EXTRA_ARTISTNAME, artistname);


        newFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack so the user can navigate
        // back
        transaction.replace(R.id.fragment_container, newFragment, AlbumsFragment.TAG);
        transaction.addToBackStack("ArtistAlbumsFragment");

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onStatusChanged(DRAG_STATUS status) {
        mNowPlayingDragStatus = status;
        if (status == DRAG_STATUS.DRAGGED_UP) {
            View coordinatorLayout = findViewById(R.id.main_coordinator_layout);
            coordinatorLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDragPositionChanged(float pos) {
        // Get the primary color of the active theme from the helper.
        int newColor = ThemeUtils.getThemeColor(this, R.attr.colorPrimary);

        // Calculate the offset depending on the floating point position (0.0-1.0 of the view)
        // Shift by 24 bit to set it as the A from ARGB and set all remaining 24 bits to 1 to
        int alphaOffset = (((255 - (int) (255.0 * pos)) << 24) | 0xFFFFFF);
        // and with this mask to set the new alpha value.
        newColor &= (alphaOffset);
        getWindow().setStatusBarColor(newColor);
    }

    @Override
    public void onSwitchedViews(VIEW_SWITCHER_STATUS view) {
        mNowPlayingViewSwitcherStatus = view;
    }

    @Override
    public void onStartDrag() {
        View coordinatorLayout = findViewById(R.id.main_coordinator_layout);
        coordinatorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void editProfile(MPDServerProfile profile) {
        // Create fragment and give it an argument for the selected article
        EditProfileFragment newFragment = new EditProfileFragment();
        Bundle args = new Bundle();
        if (null != profile) {
            args.putParcelable(EditProfileFragment.EXTRA_PROFILE, profile);
        }


        newFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack so the user can navigate
        // back
        transaction.replace(R.id.fragment_container, newFragment, EditProfileFragment.TAG);
        transaction.addToBackStack("EditProfileFragment");


        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void connectProfile(MPDServerProfile profile) {
        ConnectionManager.disconnectFromServer();
        ConnectionManager.setParameters(profile);
        ConnectionManager.reconnectLastServer();
    }

    @Override
    public void addProfile(MPDServerProfile profile) {
        mProfileManager.addProfile(profile);
    }

    @Override
    public void removeProfile(MPDServerProfile profile) {
        mProfileManager.deleteProfile(profile);
    }

    @Override
    public void onSaveObject(String title, SaveDialog.OBJECTTYPE type) {
        // check type to identify which object should be saved
        switch (type) {
            case PLAYLIST:
                MPDQueryHandler.savePlaylist(title);
                break;

        }
    }

    @Override
    public void openPlaylist(String name) {
        // Create fragment and give it an argument for the selected article
        PlaylistTracksFragment newFragment = new PlaylistTracksFragment();
        Bundle args = new Bundle();
        args.putString(PlaylistTracksFragment.EXTRA_PLAYLIST_NAME, name);


        newFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack so the user can navigate
        // back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack("PlaylistTracksFragment");

        // Commit the transaction
        transaction.commit();

    }


    @Override
    public void setupFAB(boolean active, View.OnClickListener listener) {
        mFAB = (FloatingActionButton) findViewById(R.id.andrompd_play_button);
        if (null == mFAB) {
            return;
        }
        if (active) {
            mFAB.show();
        } else {
            mFAB.hide();
        }
        mFAB.setOnClickListener(listener);
    }

    @Override
    public void setupToolbar(String title, boolean scrollingEnabled, boolean drawerIndicatorEnabled, boolean showImage) {
        // set drawer state
        mDrawerToggle.setDrawerIndicatorEnabled(drawerIndicatorEnabled);

        ImageView collapsingImage = (ImageView) findViewById(R.id.collapsing_image);
        View collapsingImageGradientTop = findViewById(R.id.collapsing_image_gradient_top);
        View collapsingImageGradientBottom = findViewById(R.id.collapsing_image_gradient_bottom);
        if (collapsingImage != null && collapsingImageGradientTop != null && collapsingImageGradientBottom != null) {
            if (showImage) {
                collapsingImage.setVisibility(View.VISIBLE);
                collapsingImageGradientTop.setVisibility(View.VISIBLE);
                collapsingImageGradientBottom.setVisibility(View.VISIBLE);
            } else {
                collapsingImage.setVisibility(View.GONE);
                collapsingImageGradientTop.setVisibility(View.GONE);
                collapsingImageGradientBottom.setVisibility(View.GONE);
            }
        }
        // set scrolling behaviour
        CollapsingToolbarLayout toolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        // set title for both the activity and the collapsingToolbarlayout for both cases
        // where and image is shown and not.
        if (toolbar != null) {
            toolbar.setTitle(title);

            setTitle(title);


            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            AppBarLayout layout = (AppBarLayout) findViewById(R.id.appbar);
            if (layout != null) {
                layout.setExpanded(true, false);
            }

            if (scrollingEnabled) {
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            } else {
                params.setScrollFlags(0);
            }

            if (showImage && collapsingImage != null) {
                // Enable title of collapsingToolbarlayout for smooth transition
                toolbar.setTitleEnabled(true);
                setToolbarImage(getResources().getDrawable(R.drawable.cover_placeholder, null));
                params.setScrollFlags(params.getScrollFlags() | AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);

                // Reset the previously added padding again.
                toolbar.setPadding(0, 0, 0, 0);
            } else {
                // Disable title for collapsingToolbarLayout and show normal title
                toolbar.setTitleEnabled(false);
                // Set the padding to match the statusbar height if a picture is shown.
                toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            }

        }
    }


    /**
     * Method to retrieve the height of the statusbar to compensate in non-transparent cases.
     *
     * @return The Dimension of the statusbar. Used to compensate the padding.
     */
    private int getStatusBarHeight() {
        int resHeight = 0;
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            resHeight = getResources().getDimensionPixelSize(resId);
        }
        return resHeight;
    }


    public void setToolbarImage(Drawable drawable) {
        ImageView collapsingImage = (ImageView) findViewById(R.id.collapsing_image);
        if (collapsingImage != null) {
            collapsingImage.setImageDrawable(drawable);
        }
    }

    @Override
    public void openPath(String path) {
        // Create fragment and give it an argument for the selected directory
        FilesFragment newFragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(FilesFragment.EXTRA_FILENAME, path);

        newFragment.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.addToBackStack("FilesFragment");
        transaction.replace(R.id.fragment_container, newFragment);

        // Commit the transaction
        transaction.commit();

    }
}
