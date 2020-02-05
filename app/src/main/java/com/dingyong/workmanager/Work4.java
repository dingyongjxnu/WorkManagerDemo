package com.dingyong.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Created by：dingyong on 2020/2/4 15:55
 * email：dingyongjxnu@163.com
 */
public class Work4 extends Worker {
    private static final String TAG = "Work4";

    public Work4(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Work4 start");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Work4 end");
        return Result.success(new Data.Builder().putString("Work", "Work4").build());
    }
}
