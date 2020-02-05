package com.dingyong.workmanager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

/**
 * Created by：dingyong on 2020/2/5 11:08
 * email：dingyongjxnu@163.com
 */
public class App extends Application implements Configuration.Provider {
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        Configuration configuration = new Configuration.Builder().build();
        WorkManager.initialize(this, configuration);
        return configuration;


    }
}
