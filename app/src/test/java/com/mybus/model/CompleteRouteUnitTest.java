package com.mybus.model;

import com.mybus.helper.FileLoaderHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Julian Gonzalez <jgonzalez@devspark.com>
 */
public class CompleteRouteUnitTest {
    /**
     * Tests parsing for a complete route (idline=2)
     */
    @Test
    public void parseCompleteRouteTest() throws JSONException {
        JSONObject jsonMockedResponse = FileLoaderHelper.loadJSONObjectFromResource(this, "complete_routes/complete_route_id_line_2.json");
        CompleteBusRoute parsedCompleteRoute = CompleteBusRoute.parseCompleteBusRoute(jsonMockedResponse);
        // Checks fields
        assertEquals(parsedCompleteRoute.getType(), 1);
        assertEquals(parsedCompleteRoute.getColor(), "#00338e");

        // Checks PointList
        assertNotNull(parsedCompleteRoute.getPointList());
        assertEquals(parsedCompleteRoute.getPointList().size(), 126);
    }
}
