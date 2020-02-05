package com.dingyong.workmanager;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Context mContext;
    private UUID mUploadUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
    }

    public void onWorkManager(View view) {
        // 工作约束   如果指定了多个约束，您的任务将仅在满足所有约束时才会运行。
        //如果在任务运行期间某个约束不再得到满足，则 WorkManager 将停止工作器。
        // 当约束继续得到满足时，系统将重新尝试执行该任务。
        Constraints constraints = new Constraints.Builder()
                /*
                 * 指定网络状态执行任务
                 * NetworkType.NOT_REQUIRED：对网络没有要求
                 * NetworkType.CONNECTED：网络连接的时候执行
                 * NetworkType.UNMETERED：不计费的网络比如WIFI下执行
                 * NetworkType.NOT_ROAMING：非漫游网络状态
                 * NetworkType.METERED：计费网络比如3G，4G下执行。
                 */
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true) //执行任务时电池电量不能偏低。
                .setRequiresCharging(true) //在设备充电时才能执行任务。
                //.setRequiresDeviceIdle(true) // 设备空闲时才能执行
                .setRequiresStorageNotLow(true) //设备储存空间足够时才能执行。
                .build();

        //您的任务可能需要数据以输入参数的形式传入，或者将数据返回为结果。例如，某个任务负责处理图像上传，
        // 它要求以要上传的图像的 URI 为输入，并且可能要求用已上传图像的网址作为输出。
        // 输入和输出值以键值对的形式存储在 Data 对象中。下面的代码展示了如何在 WorkRequest 中设置输入数据
        Data inputData = new Data.Builder()
                .putString(UploadWorker.KEY_UPLOAD_WORKER_INPUT, "www.baidu.com")
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(UploadWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .addTag(UploadWorker.TAG) // 标记工作  您可以通过为任意 WorkRequest 对象分配标记字符串，按逻辑对任务进行分组。这样您就可以对使用特定标记的所有任务执行操作。
                .build();

        //获取uuid
        mUploadUUID = oneTimeWorkRequest.getId();
        WorkManager.getInstance(mContext).enqueue(oneTimeWorkRequest);
        // 观察您的工作状态
        getWorkInfoByIdLiveData();
        //getWorkInfosByTagLiveData();


    }

    /**
     * 根据 id
     */
    private void getWorkInfoByIdLiveData() {
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(mUploadUUID)
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        getWorkInfo(workInfo);
                    }
                });
    }

    /**
     * 根据tag
     */
    private void getWorkInfosByTagLiveData() {
        //根据 tag
        WorkManager.getInstance(mContext).getWorkInfosByTagLiveData(UploadWorker.TAG)
                .observe(this, new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(List<WorkInfo> workInfos) {
                        for (WorkInfo workInfo : workInfos) {
                            getWorkInfo(workInfo);
                        }
                    }
                });
    }

    /**
     * 取消和停止工作
     * 如果您不再需要运行先前加入队列的作业，则可以申请取消。
     * 最简单的方法是使用其 id 并调用 WorkManager.cancelWorkById(UUID) 来取消单个 WorkRequest：
     *
     * @param view
     */
    public void onCancleWorkManager(View view) {
        //在后台，WorkManager 会检查工作的 State。如果工作已经完成，则不会发生任何变化。
        //否则，其状态将更改为 CANCELLED，之后就不会运行这个工作。
        //任何依赖于这项工作的 WorkRequests 的状态也将变为 CANCELLED。
        //此外，如果工作当前的状态为 RUNNING，则工作器也会收到对 ListenableWorker.onStopped() 的调用。
        //替换此方法以处理任何可能的清理操作。
        //您也可以使用 WorkManager.cancelAllWorkByTag(String) 按标记取消 WorkRequest。请注意，
        //此方法会取消所有具有此标记的工作。此外，您还可以使用 WorkManager.cancelUniqueWork(String) 取消具有唯一名称的所有工作。
        //通过 tag 取消使用特定标记的所有任务
        WorkManager.getInstance(mContext).cancelAllWorkByTag(UploadWorker.TAG);
        // 通过id 取消任务
        if (mUploadUUID != null) {
            WorkManager.getInstance(mContext).cancelWorkById(mUploadUUID);
        }

    }

    /**
     * 在工作的整个生命周期内，它会经历多个不同的 State。
     * <p>
     * 如果有尚未完成的前提性工作，则工作处于 BLOCKED State。
     * 如果工作能够在满足 Constraints 和时机条件后立即运行，则被视为处于 ENQUEUED 状态。
     * 当工作器在活跃地执行时，其处于 RUNNING State。
     * 如果工作器返回 Result.success()，则被视为处于 SUCCEEDED 状态。这是一种终止 State；只有 OneTimeWorkRequest 可以进入这种 State。
     * 相反，如果工作器返回 Result.failure()，则被视为处于 FAILED 状态。这也是一个终止 State；只有 OneTimeWorkRequest 可以进入这种 State。所有依赖工作也会被标记为 FAILED，并且不会运行。
     * 当您明确取消尚未终止的 WorkRequest 时，它会进入 CANCELLED State。所有依赖工作也会被标记为 CANCELLED，并且不会运行。
     *
     * @param workInfo WorkInfo
     */
    private void getWorkInfo(WorkInfo workInfo) {
        if (workInfo == null) {
            return;
        }

        Data data = workInfo.getOutputData();
        Log.d(TAG, "getWorkInfo data = " + data.toString());
        WorkInfo.State state = workInfo.getState();
        switch (state) {
            case ENQUEUED:
                // 进入队列
                // 如果工作能够在满足 Constraints 和时机条件后立即运行，则被视为处于 ENQUEUED 状态。
                Log.d(TAG, "getWorkInfo state = enqueued ");
                break;
            case RUNNING:
                //当工作器在活跃地执行时，其处于 RUNNING State。
                //运行中
                Log.d(TAG, "getWorkInfo state = running ");
                break;
            case SUCCEEDED:
                // 如果工作器返回 Result.success()，则被视为处于 SUCCEEDED 状态。这是一种终止 State；
                // 只有 OneTimeWorkRequest 可以进入这种 State。
                // 运行成功
                Log.d(TAG, "getWorkInfo state = succeeded ");
                break;
            case FAILED:
                //如果工作器返回 Result.failure()，则被视为处于 FAILED 状态。这也是一个终止 State；
                // 只有 OneTimeWorkRequest 可以进入这种 State。所有依赖工作也会被标记为 FAILED，并且不会运行。
                //运行失败
                Log.d(TAG, "getWorkInfo state = failed ");
                break;
            case BLOCKED:
                // 如果有尚未完成的前提性工作，则工作处于 BLOCKED State。
                // 阻塞中
                Log.d(TAG, "getWorkInfo state = blocked ");
                break;
            case CANCELLED:
                // 当您明确取消尚未终止的 WorkRequest 时，它会进入 CANCELLED State。
                // 所有依赖工作也会被标记为 CANCELLED，并且不会运行
                //运行取消中
                Log.d(TAG, "getWorkInfo state = cancelled ");
                break;
            default:
                break;
        }
        Data progress = workInfo.getProgress();
        int value = progress.getInt(UploadWorker.PROGRESS, 0);
        Log.d(TAG, "getProgress data = " + value);
    }


    public void onSerial(View view) {
        OneTimeWorkRequest work1 = new OneTimeWorkRequest
                .Builder(Work1.class)
                .build();
        OneTimeWorkRequest work2 = new OneTimeWorkRequest
                .Builder(Work2.class)
                .build();
        OneTimeWorkRequest work3 = new OneTimeWorkRequest
                .Builder(Work3.class)
                .build();
        //执行顺序  work1 -> work2 -> work3
        WorkManager.getInstance(mContext)
                .beginWith(work1)
                .then(work2)
                .then(work3)
                .enqueue();

        getWorkInfoByIdLiveData(work1);
        getWorkInfoByIdLiveData(work2);
        getWorkInfoByIdLiveData(work3);

    }

    public void onParallel(View view) {
        OneTimeWorkRequest work1 = new OneTimeWorkRequest
                .Builder(Work1.class)
                .build();
        OneTimeWorkRequest work2 = new OneTimeWorkRequest
                .Builder(Work2.class)
                .build();
        OneTimeWorkRequest work3 = new OneTimeWorkRequest
                .Builder(Work3.class)
                .build();

        OneTimeWorkRequest work4 = new OneTimeWorkRequest
                .Builder(Work4.class)
                .build();
        OneTimeWorkRequest work5 = new OneTimeWorkRequest
                .Builder(Work5.class)
                .build();
        //执行顺序  work1 work2  work3  一起执行 然后执行 work4 和work5
        // (work1 , work2 , work3) -> work4 -> work5

        WorkManager.getInstance(mContext)
                .beginWith(Arrays.asList(work1, work2, work3))
                .then(work4)
                .then(work5)
                .enqueue();
        getWorkInfoByIdLiveData(work1);
        getWorkInfoByIdLiveData(work2);
        getWorkInfoByIdLiveData(work3);
    }

    public void onWorkContinuation(View view) {
        OneTimeWorkRequest work1 = new OneTimeWorkRequest
                .Builder(Work1.class)
                .build();
        OneTimeWorkRequest work2 = new OneTimeWorkRequest
                .Builder(Work2.class)
                .build();
        OneTimeWorkRequest work3 = new OneTimeWorkRequest
                .Builder(Work3.class)
                .build();

        OneTimeWorkRequest work4 = new OneTimeWorkRequest
                .Builder(Work4.class)
                .build();
        OneTimeWorkRequest work5 = new OneTimeWorkRequest
                .Builder(Work5.class)
                .build();
        //执行顺序  (work1 - work3) -> (work2 - work4) -> work5
        WorkContinuation continuation1 = WorkManager.getInstance(mContext)
                .beginWith(work1).then(work2);
        WorkContinuation continuation2 = WorkManager.getInstance(mContext)
                .beginWith(work3).then(work4);
        WorkContinuation.combine(Arrays.asList(continuation1, continuation2)).then(work5).enqueue();
    }

    private void getWorkInfoByIdLiveData(WorkRequest workRequest) {
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(workRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Data data = workInfo.getOutputData();
                String out = data.getString("Work");
                if (!TextUtils.isEmpty(out)) {
                    Log.d(TAG, "getWorkInfoByIdLiveData  workInfo data = " + out);
                }
            }
        });
    }


    public void onPeriodicWorkRequest(View view) {
        PeriodicWorkRequest saveRequest = new PeriodicWorkRequest
                .Builder(SaveImageWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(mContext).enqueue(saveRequest);
        WorkManager.getInstance(mContext).getWorkInfoByIdLiveData(saveRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        Data data = workInfo.getOutputData();
                        Log.d(TAG, "data=" + data.toString());
                    }
                });
    }
}
