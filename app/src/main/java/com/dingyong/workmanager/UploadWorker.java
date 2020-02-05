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
public class UploadWorker extends Worker {
    public static final String TAG = "UploadWorker";
    public static final String KEY_UPLOAD_WORKER = "key_upload_worker";
    public static final String KEY_UPLOAD_WORKER_INPUT = "key_upload_worker_input";
    public static final String PROGRESS = "PROGRESS";
    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        setProgressAsync(new Data.Builder().putInt(PROGRESS, 0).build());

    }

    @NonNull
    @Override
    public Result doWork() {
        return uploadImages();
    }

    private Result uploadImages() {
        // Worker 类可通过调用 Worker.getInputData() 访问输入参数。
        //  类似地，Data 类可用于输出返回值。要返回 Data 对象，请将它包含到 Result 的 Result.success()
        //  或 Result.failure() 中，如下所示。
        // get the input
        Data inputData = getInputData();
        String uri = inputData.getString(KEY_UPLOAD_WORKER_INPUT);
        // Do the work
        Log.d(TAG, "uploadImages start");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "uploadImages end");
        // Return the output
        Data outData = new Data.Builder()
                .putString(KEY_UPLOAD_WORKER, uri + "  uploade sccess")
                .build();
        setProgressAsync(new Data.Builder().putInt(PROGRESS, 100).build());
        return Result.success(outData);
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d(TAG, "onStopped");
    }
}
