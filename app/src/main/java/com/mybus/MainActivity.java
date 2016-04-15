package com.mybus;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mybus.adapter.StreetAutoCompleteAdapter;
import com.mybus.listener.AppBarStateChangeListener;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.mybus.location.LocationUpdater;
import com.mybus.location.OnLocationChangedCallback;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnLocationChangedCallback {

    private GoogleMap mMap;
    private LocationUpdater locationUpdater;
    private String CURRENT_LOCATION_MARKER;
    private Float DEFAULT_MAP_ZOOM;
    @Bind(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.floating_action_button)
    FloatingActionButton mFAB;
    @Bind(R.id.from_field)
    AppCompatAutoCompleteTextView mFromImput;
    @Bind(R.id.to_field)
    AppCompatAutoCompleteTextView mToImput;

    /**
     * Checks the state of the AppBarLayout
     */
    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener = new AppBarStateChangeListener() {
        @Override
        public void onStateChanged(AppBarLayout appBarLayout, State state) {
            if (state.equals(State.COLLAPSED)) {
                mFAB.show();
                showSoftKeyBoard(false);
            }
        }
    };

    /**
     * Listener for To_TextView, makes the search when the user hits the magnifying glass
     */
    private TextView.OnEditorActionListener mOnEditorAndroidListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //ToDo: Perform Search
                return true;
            }
            return false;
        }
    };

    @OnClick(R.id.floating_action_button)
    public void onFABClick(View view) {
        mAppBarLayout.setExpanded(true, true);
        mFAB.hide();
        mFromImput.requestFocus();
        showSoftKeyBoard(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAppBarLayout.addOnOffsetChangedListener(mOnOffsetChangedListener);

        StreetAutoCompleteAdapter autoCompleteAdapter = new StreetAutoCompleteAdapter(MainActivity.this);

        mFromImput.setAdapter(autoCompleteAdapter);
        mToImput.setAdapter(autoCompleteAdapter);

        mToImput.setOnEditorActionListener(mOnEditorAndroidListener);
        locationUpdater = new LocationUpdater(this, this);
        CURRENT_LOCATION_MARKER = getString(R.string.current_location_marker);
        DEFAULT_MAP_ZOOM = new Float(getResources().getInteger(R.integer.default_map_zoom));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationUpdater.startListening();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_MAP_ZOOM));
    }

    @Override
    public void onLocationChanged(double lat, double lon) {
        mMap.clear();
        LatLng latLng = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(latLng).title(CURRENT_LOCATION_MARKER));
    }

    private void showSoftKeyBoard(boolean show) {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            if (show) {
                imm.showSoftInput(mFromImput, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }


}