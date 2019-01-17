package com.imagecroper.bgremover;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Pair;

import com.imagecroper.ResultActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.annotation.RequiresApi;

import static android.view.View.VISIBLE;

public class SaveDrawingTask extends AsyncTask<Bitmap, Void, Pair<File, Exception>> {

    private static final String SAVED_IMAGE_FORMAT = ".png";
    private static final String SAVED_IMAGE_NAME = "cutout_tmp";

    private final WeakReference<ResultActivity> activityWeakReference;

    public SaveDrawingTask(ResultActivity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        activityWeakReference.get().loadingModal.setVisibility(VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Pair<File, Exception> doInBackground(Bitmap... bitmaps) {

        try {
           // File file=new File(ResultActivity.getSaveDir());
            File file = File.createTempFile(SAVED_IMAGE_NAME, SAVED_IMAGE_FORMAT,new File(ResultActivity.getSaveDir()));

            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmaps[0].compress(Bitmap.CompressFormat.PNG, 95, out);
                return new Pair<>(file, null);
            }
        } catch (IOException e) {
            return new Pair<File, Exception>(null, e);
        }
    }

    protected void onPostExecute(Pair<File, Exception> result) {
        super.onPostExecute(result);

        Intent resultIntent = new Intent();

        if (result.first != null) {
            Uri uri = Uri.fromFile(result.first);

            resultIntent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, uri);
            activityWeakReference.get().setResult(Activity.RESULT_OK, resultIntent);
            activityWeakReference.get().finish();

        } else {
            activityWeakReference.get().exitWithError(result.second);
        }
    }
}