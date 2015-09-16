package com.stockboo.model.db;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import com.stockboo.model.StockList;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by narpr05 on 6/20/2015.
 */

public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    private static final Class<?>[] classes = new Class[]{StockList.class};

    public static void main(String[] args) throws SQLException, IOException {
        //writeConfigFile("C:/narpr05/AndroidStudioProjects/StockBoo/app/src/main/res/raw/ormlite_config.txt", classes);
        writeConfigFile("ormlite_config.txt", classes);
    }
}