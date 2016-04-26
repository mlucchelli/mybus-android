package com.mybus;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mybus.adapter.StreetAutoCompleteAdapter;
import com.mybus.adapter.ViewPagerAdapter;
import com.mybus.asynctask.RouteSearchCallback;
import com.mybus.asynctask.RouteSearchTask;
import com.mybus.fragment.BusRouteFragment;
import com.mybus.helper.SearchFormStatus;
import com.mybus.listener.AppBarStateChangeListener;
import com.mybus.listener.CustomAutoCompleteClickListener;
import com.mybus.location.LocationGeocoding;
import com.mybus.location.LocationUpdater;
import com.mybus.location.OnAddressGeocodingCompleteCallback;
import com.mybus.location.OnLocationChangedCallback;
import com.mybus.location.OnLocationGeocodingCompleteCallback;
import com.mybus.model.BusRouteResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnLocationChangedCallback,
        OnAddressGeocodingCompleteCallback, OnLocationGeocodingCompleteCallback, RouteSearchCallback {

    public static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private LocationUpdater mLocationUpdater;
    private Float DEFAULT_MAP_ZOOM;
    @Bind(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.floating_action_button)
    FloatingActionButton mFloatingSearchButton;
    @Bind(R.id.center_location_action_button)
    FloatingActionButton mCenterLocationButton;
    @Bind(R.id.perform_search_action_button)
    FloatingActionButton mPerformSearchButton;
    @Bind(R.id.from_field)
    AppCompatAutoCompleteTextView mFromInput;
    @Bind(R.id.to_field)
    AppCompatAutoCompleteTextView mToInput;
    MarkerOptions mUserLocationMarkerOptions;
    //Marker used to update the location on the map
    Marker mUserLocationMarker;
    //Marker used to show the Start Location
    Marker mStartLocationMarker;
    //Marker used to show the End Location
    Marker mEndLocationMarker;
    //Temporary Marker
    MarkerOptions mStartLocationMarkerOptions;
    MarkerOptions lastAddressGeocodingType;
    MarkerOptions lastLocationGeocodingType;

    MarkerOptions mEndLocationMarkerOptions;
    //Keeps the state of the app bar
    private AppBarStateChangeListener.State mAppBarState;
    private BottomSheetBehavior<LinearLayout> mBottomSheetBehavior;
    private ViewPagerAdapter mViewPagerAdapter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    OnAddressGeocodingCompleteCallback mOnAddressGeocodingCompleteCallback;
    OnLocationGeocodingCompleteCallback mOnLocationGeocodingCompleteCallback;

    LocationGeocoding locationGeocoding;

    /**
     * Checks the state of the AppBarLayout
     */
    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener = new AppBarStateChangeListener() {
        @Override
        public void onStateChanged(AppBarLayout appBarLayout, State state) {
            mAppBarState = state;
            if (state.equals(State.COLLAPSED)) {
                showSoftKeyBoard(false);
            }
        }
    };

    /**
     * Listener for To_TextView, makes the search when the user hits the magnifying glass
     */
    private TextView.OnEditorActionListener mOnEditorAndroidListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
            String address = tv.getText().toString();
            if ((tv.getId() == mFromInput.getId()) && actionId == EditorInfo.IME_ACTION_NEXT) {
                setMarkerTitle(mStartLocationMarker, mStartLocationMarkerOptions, address);
                lastAddressGeocodingType = mStartLocationMarkerOptions;
                locationGeocoding.performGeocodeByAddress(address, mOnAddressGeocodingCompleteCallback);
                mToInput.requestFocus();
                return true;
            }
            if ((tv.getId() == mToInput.getId()) && actionId == EditorInfo.IME_ACTION_SEARCH) {
                setMarkerTitle(mEndLocationMarker, mEndLocationMarkerOptions, address);
                lastAddressGeocodingType = mEndLocationMarkerOptions;
                locationGeocoding.performGeocodeByAddress(address, mOnAddressGeocodingCompleteCallback);
                showSoftKeyBoard(false);
                return true;
            }
            return false;
        }
    };

    /**
     * Listener for Map Long Click Listener for setting start or end locations.
     */
    private GoogleMap.OnMapLongClickListener mMapOnLongClickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            if (!SearchFormStatus.getInstance().isStartFilled()) {
                lastLocationGeocodingType = mStartLocationMarkerOptions;
                mStartLocationMarker = positionMarker(mStartLocationMarker, mStartLocationMarkerOptions, latLng, true);
            } else if (!SearchFormStatus.getInstance().isDestinationFilled()) {
                lastLocationGeocodingType = mEndLocationMarkerOptions;
                mEndLocationMarker = positionMarker(mEndLocationMarker, mEndLocationMarkerOptions, latLng, true);
            }
        }
    };

    public Marker positionMarker(Marker marker, MarkerOptions markerOptions, LatLng latLng, boolean performGeocoding) {
        if (marker == null) {
            markerOptions.position(latLng);
            marker = mMap.addMarker(markerOptions);
        } else {
            marker.setPosition(latLng);
        }
        if (performGeocoding) {
            locationGeocoding.performGeocodeByLocation(latLng, mOnLocationGeocodingCompleteCallback);
        }
        //Update searchButton status
        if (mStartLocationMarker != null && markerOptions == mEndLocationMarkerOptions
                || mEndLocationMarker != null && markerOptions == mStartLocationMarkerOptions) {
            mPerformSearchButton.setAlpha(255);
            mPerformSearchButton.setEnabled(true);
        }
        return marker;
    }

    /**
     * Listener for the marker drag
     */
    private GoogleMap.OnMarkerDragListener mOnMarkerDragListener = new GoogleMap.OnMarkerDragListener() {

        @Override
        public void onMarkerDragStart(Marker marker) {
            marker.hideInfoWindow();
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            marker.hideInfoWindow();
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            marker.hideInfoWindow();
            if (marker.getId().equals(mStartLocationMarker.getId())) {
                lastLocationGeocodingType = mStartLocationMarkerOptions;
            } else if (marker.getId().equals(mEndLocationMarker.getId())) {
                lastLocationGeocodingType = mEndLocationMarkerOptions;
            }
            locationGeocoding.performGeocodeByLocation(marker.getPosition(), mOnLocationGeocodingCompleteCallback);
        }
    };

    /**
     * Bottom Sheet Tab selected listener
     * <p/>
     * Expands the bottom sheet when the user re-selects any tab
     */
    private TabLayout.OnTabSelectedListener mOnTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            //TODO: At least display both start and destination markers
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            //TODO: Remove markers
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    };

    @OnClick(R.id.floating_action_button)
    public void onFloatingSearchButton(View view) {
        mAppBarLayout.setExpanded(true, true);
        mFromInput.requestFocus();
        showSoftKeyBoard(true);
    }

    @OnClick(R.id.center_location_action_button)
    public void onCenterLocationButtonClick(View view) {
        centerToLastKnownLocation();
    }

    @OnClick(R.id.perform_search_action_button)
    public void onPerformSearchButtonClick(View view) {
        performSearch();
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

        mFromInput.setAdapter(autoCompleteAdapter);
        mFromInput.setOnEditorActionListener(mOnEditorAndroidListener);
        mFromInput.setOnItemClickListener(new CustomAutoCompleteClickListener(mFromInput));

        mToInput.setAdapter(autoCompleteAdapter);
        mToInput.setOnItemClickListener(new CustomAutoCompleteClickListener(mToInput));
        mToInput.setOnEditorActionListener(mOnEditorAndroidListener);

        mLocationUpdater = new LocationUpdater(this, this);
        DEFAULT_MAP_ZOOM = new Float(getResources().getInteger(R.integer.default_map_zoom));
        mUserLocationMarkerOptions = new MarkerOptions()
                .title(getString(R.string.current_location_marker))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_dot));

        mOnAddressGeocodingCompleteCallback = this;
        mOnLocationGeocodingCompleteCallback = this;
        locationGeocoding = new LocationGeocoding(this);
        //Disable the mPerformSearchButton action
        mPerformSearchButton.setAlpha(50);
        mPerformSearchButton.setEnabled(false);
        resetLocalVariables();
        setupBottomSheet();
    }

    private void setupBottomSheet() {
        LinearLayout bottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
    }

    /**
     * This method restart the local variables to avoid old apps's states
     */
    private void resetLocalVariables() {
        SearchFormStatus.getInstance().clearFormStatus();
        mUserLocationMarkerOptions = null;
        mUserLocationMarker = null;
        mStartLocationMarker = null;
        mEndLocationMarker = null;
        lastAddressGeocodingType = null;
        lastLocationGeocodingType = null;
        mStartLocationMarkerOptions = new MarkerOptions()
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_origen))
                .title("origen");
        mEndLocationMarkerOptions = new MarkerOptions()
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_destino))
                .title("destino");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mUserLocationMarkerOptions = new MarkerOptions()
                .title(getString(R.string.current_location_marker))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_dot));
        mMap = googleMap;
        mLocationUpdater.startListening();
        centerToLastKnownLocation();

        mMap.setOnMapLongClickListener(mMapOnLongClickListener);
        //mMap.setOnInfoWindowClickListener(mOnInfoWindowClickListener);
        mMap.setOnMarkerDragListener(mOnMarkerDragListener);
    }

    public void centerToLastKnownLocation() {
        //get the last gps location
        LatLng lastLocation = mLocationUpdater.getLastKnownLocation();
        if (lastLocation != null) {
            mUserLocationMarkerOptions.position(lastLocation);
            //if the marker is not on the map, add it
            if (mUserLocationMarker == null) {
                mUserLocationMarker = mMap.addMarker(mUserLocationMarkerOptions);
            }
            zoomTo(mLocationUpdater.getLastKnownLocation());
        }
    }

    private void zoomTo(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM));
    }

    @Override
    public void onLocationChanged(LatLng latLng) {
        if (mUserLocationMarker != null) {
            mUserLocationMarker.setPosition(latLng);
        }
    }

    /**
     * Show or hide the soft keyboard
     *
     * @param show
     */
    private void showSoftKeyBoard(boolean show) {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            if (show) {
                imm.showSoftInput(mFromInput, InputMethodManager.SHOW_IMPLICIT);
            } else {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mAppBarState != null && mAppBarState.equals(AppBarStateChangeListener.State.EXPANDED)) {
            mAppBarLayout.setExpanded(false, true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onAddressGeocodingComplete(LatLng location) {
        if (location != null) {
            if (lastAddressGeocodingType == mStartLocationMarkerOptions) {
                mStartLocationMarker = positionMarker(mStartLocationMarker, mStartLocationMarkerOptions, location, false);
            }
            if (lastAddressGeocodingType == mEndLocationMarkerOptions) {
                mEndLocationMarker = positionMarker(mEndLocationMarker, mEndLocationMarkerOptions, location, false);
            }
            zoomTo(location);
        }
    }

    private void setMarkerTitle(Marker marker, MarkerOptions markerOptions, String title) {
        if (marker != null) {
            marker.setTitle(title);
            marker.showInfoWindow();
        }
        markerOptions.title(title);
    }

    @Override
    public void onLocationGeocodingComplete(String address) {
        if (address != null) {
            if (lastLocationGeocodingType == mStartLocationMarkerOptions) {
                setMarkerTitle(mStartLocationMarker, mStartLocationMarkerOptions, address);
                mFromInput.setText(address);
                SearchFormStatus.getInstance().setStartFilled(true);
                SearchFormStatus.getInstance().setStartMarkerId(mStartLocationMarker.getId());
            }
            if (lastLocationGeocodingType == mEndLocationMarkerOptions) {
                setMarkerTitle(mEndLocationMarker, mEndLocationMarkerOptions, address);
                mToInput.setText(address);
                SearchFormStatus.getInstance().setDestinationFilled(true);
            }
        }
    }

    private void performSearch() {
        if (mStartLocationMarker == null || mEndLocationMarker == null) {
            return;
        }
        RouteSearchTask routeSearchTask = new RouteSearchTask(this);
        routeSearchTask.execute(mStartLocationMarker.getPosition(), mEndLocationMarker.getPosition());
        Toast.makeText(this, "performing search", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteFound(List<BusRouteResult> results) {
        if (results != null && !results.isEmpty()) {
            Toast.makeText(this, "results found", Toast.LENGTH_LONG).show();
            populateBottomSheet(results);
        } else {
            Toast.makeText(this, "Did not get any result", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Populates the bottom sheet with the Routes found
     *
     * @param results
     */
    private void populateBottomSheet(List<BusRouteResult> results) {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), MainActivity.this.getLayoutInflater());
        for (BusRouteResult route : results) {
            BusRouteFragment fragment = new BusRouteFragment();
            fragment.setBusRouteResult(route);
            mViewPagerAdapter.addFragment(fragment);
        }
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setCustomView(mViewPagerAdapter.getTabView(mTabLayout, results.get(i)));
        }
        showBottomSheetResults(true);
    }

    /**
     * Show or hide the bottom sheet Layout
     *
     * @param show
     */
    private void showBottomSheetResults(boolean show) {
        if (mBottomSheetBehavior != null) {
            if (show) {
                mBottomSheetBehavior.setPeekHeight(100);
            } else {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mBottomSheetBehavior.setPeekHeight(0);
            }
        }
    }
}