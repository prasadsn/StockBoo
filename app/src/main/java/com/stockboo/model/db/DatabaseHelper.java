package com.stockboo.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.stockboo.R;
import com.stockboo.model.NewsItem;
import com.stockboo.model.StockList;
import com.stockboo.model.WatchList;

import java.sql.SQLException;

/**
 * Created by narpr05 on 6/20/2015.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "stocklist.db";
    private static final int DATABASE_VERSION = 1;

    // the DAO object we use to access the StockList table
    private Dao<StockList, Integer> StockListDao = null;
    // The DAO object we use to access the WatchList table
    private Dao<WatchList, Integer> favouriteStockListDao = null;
    // The DAO object we use to access the NewsItem table
    private Dao<NewsItem, Integer> newsItemDao = null;

    private RuntimeExceptionDao<StockList, Integer> StockListRuntimeDao = null;
    private RuntimeExceptionDao<WatchList, Integer> favouriteStockListRuntimeDao = null;
    private RuntimeExceptionDao<NewsItem, Integer> newsListRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, StockList.class);
            TableUtils.createTable(connectionSource, WatchList.class);
            TableUtils.createTable(connectionSource, NewsItem.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

        // here we try inserting data in the on-create as a test
        RuntimeExceptionDao<StockList, Integer> dao = getStockListDao();
        StockList simple = new StockList();
        dao.create(simple);

        RuntimeExceptionDao<WatchList, Integer> favouriteDao = getWatchListRuntimeDao();
        WatchList watch = new WatchList();
        favouriteDao.create(watch);

        RuntimeExceptionDao<NewsItem, Integer> newsItemDao = getNewsListRuntimeDao();
        NewsItem newsItem = new NewsItem();
        newsItemDao.create(newsItem);
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        /*try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, StockList.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }*/
    }

    /**
     * Returns the Database Access Object (DAO) for our StockList class. It will create it or just give the cached
     * value.
     */
    public Dao<StockList, Integer> getDao() throws SQLException {
        if (StockListDao == null) {
            StockListDao = getDao(StockList.class);
        }
        return StockListDao;
    }

    public Dao<WatchList, Integer> getWatchListDao() throws SQLException {
        if (favouriteStockListDao == null) {
            favouriteStockListDao = getDao(WatchList.class);
        }
        return favouriteStockListDao;
    }

    public Dao<NewsItem, Integer> getNewsListDao() throws SQLException {
        if (newsItemDao == null) {
            newsItemDao = getDao(NewsItem.class);
        }
        return newsItemDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our StockList class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<StockList, Integer> getStockListDao() {
        if (StockListRuntimeDao == null) {
            StockListRuntimeDao = getRuntimeExceptionDao(StockList.class);
        }
        return StockListRuntimeDao;
    }

    public RuntimeExceptionDao<WatchList, Integer> getWatchListRuntimeDao() {
        if (favouriteStockListRuntimeDao == null) {
            favouriteStockListRuntimeDao = getRuntimeExceptionDao(WatchList.class);
        }
        return favouriteStockListRuntimeDao;
    }

    public RuntimeExceptionDao<NewsItem, Integer> getNewsListRuntimeDao() {
        if (newsListRuntimeDao == null) {
            newsListRuntimeDao = getRuntimeExceptionDao(NewsItem.class);
        }
        return newsListRuntimeDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        StockListDao = null;
        StockListRuntimeDao = null;

        favouriteStockListDao = null;
        favouriteStockListRuntimeDao = null;

        newsItemDao = null;
        newsListRuntimeDao = null;
    }}