package com.dingyong.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Created by：dingyong on 2020/2/4 14:16
 * email：dingyongjxnu@163.com
 */
public class SaveImageWorker extends Worker {
    public static final String TAG = "SaveImageWorker";

    public SaveImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        return saveImageWorker();
    }

    private Result saveImageWorker() {
        Log.d(TAG, "saveImageWorker start");
        // Return the output
        Data outData = new Data.Builder()
                .putString("saveImageWorker", "SaveImageWorker")
                .build();
        Log.d(TAG, "saveImageWorker end");
        return Result.success(outData);
    }
}
