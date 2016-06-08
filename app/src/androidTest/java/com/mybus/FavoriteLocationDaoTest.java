package com.mybus;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.mybus.dao.FavoriteLocationDao;
import com.mybus.model.FavoriteLocation;

import io.realm.RealmResults;

/**
 * Created by Julian Gonzalez <jgonzalez@devspark.com>
 */
public class FavoriteLocationDaoTest extends InstrumentationTestCase {
    private Context mContext;
    private final String NAME_FIELD = "name";
    private final String NAME_VALUE = "Test";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }

    public void test1SaveFavorite() {
        FavoriteLocation favLocation = new FavoriteLocation(NAME_VALUE, "4 de Abril", 1100, -37.3291053, -59.1336692);
        FavoriteLocation saved = FavoriteLocationDao.getInstance(mContext).saveOrUpdate(favLocation);
        assertNotNull(saved);
        Log.i("TestSaveFavorite", "Favorite saved: " + saved.toString());
    }

    public void test2GetFavoriteByName() {
        FavoriteLocation favLocation = FavoriteLocationDao.getInstance(mContext).getByField(NAME_FIELD, NAME_VALUE);
        assertNotNull(favLocation);
        Log.i("TestGetFavoriteByName", "Favorite: " + favLocation.toString());
    }

    public void test3UpdateUsageCount() {
        FavoriteLocation favLocation = FavoriteLocationDao.getInstance(mContext).getByField(NAME_FIELD, NAME_VALUE);
        assertNotNull(favLocation);
        Long id = favLocation.getId();
        FavoriteLocationDao.getInstance(mContext).updateItemUsageCount(id);
        FavoriteLocation favUpdated = FavoriteLocationDao.getInstance(mContext).getById(id);
        Log.i("TestUpdateUsageCount", "UsageCount: " + favUpdated.getUsageCount());
        assertTrue(favUpdated.getUsageCount() > 0);
    }

    public void test4UpdateFavorite() {
        FavoriteLocation favLocation = FavoriteLocationDao.getInstance(mContext).getByField(NAME_FIELD, NAME_VALUE);
        favLocation.setName("Test_Updated");
        favLocation.setStreetName("9 de Julio");
        favLocation.setStreetNumber(340);
        FavoriteLocation favUpdated = FavoriteLocationDao.getInstance(mContext).saveOrUpdate(favLocation);
        assertNotNull(favUpdated);
        assertTrue(favUpdated.getName().equalsIgnoreCase("Test_Updated"));
    }

    public void test5ListFavorites() {
        RealmResults<FavoriteLocation> results = FavoriteLocationDao.getInstance(mContext).getAll();
        assertTrue(results.size() > 0);
        Log.i("TestListFavorites", "All Favorites");
        for (FavoriteLocation fav : results) {
            Log.i("TestListFavorites", "Favorite: " + fav.toString());
        }
    }

    public void test6RemoveFavoriteUsedForTests() {
        FavoriteLocation fav = FavoriteLocationDao.getInstance(mContext).getByField(NAME_FIELD, "Test_Updated");
        assertNotNull(fav);
        assertTrue(FavoriteLocationDao.getInstance(mContext).remove(fav.getId()));
    }
}
