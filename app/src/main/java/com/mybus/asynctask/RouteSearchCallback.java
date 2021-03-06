package com.mybus.asynctask;

import com.mybus.model.BusRouteResult;

import java.util.List;

/**
 * Created by Julian Gonzalez <jgonzalez@devspark.com>
 */
public interface RouteSearchCallback {
    void onRouteFound(List<BusRouteResult> results);
}
