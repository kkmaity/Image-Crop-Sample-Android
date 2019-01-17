// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.imagecroper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alexvasilkov.gestures.views.interfaces.GestureView;
import com.imagecroper.bgremover.BitmapUtility;
import com.imagecroper.bgremover.CutOut;
import com.imagecroper.bgremover.DrawView;
import com.imagecroper.bgremover.SaveDrawingTask;
import com.imagecroper.cropImage.CropImage;
import com.imagecroper.cropImage.CropImageView;

import java.io.IOException;
import java.util.ArrayList;

import top.defaults.checkerboarddrawable.CheckerboardDrawable;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ResultActivity extends AppCompatActivity {
  private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
  private static final int IMAGE_CHOOSER_REQUEST_CODE = 2;
  private static final int CAMERA_REQUEST_CODE = 3;

  private static final String INTRO_SHOWN = "INTRO_SHOWN";
  public FrameLayout loadingModal;
  private GestureView gestureView;
  private DrawView drawView;
  private LinearLayout manualClearSettingsLayout;

  private static final short MAX_ERASER_SIZE = 150;
  private static final short BORDER_SIZE = 45;
  private static final float MAX_ZOOM = 4F;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_results);
    /*Toolbar toolbar = findViewById(R.id.photo_edit_toolbar);
    toolbar.setBackgroundColor(Color.BLACK);
    toolbar.setTitleTextColor(Color.WHITE);
    setSupportActionBar(toolbar);*/

    FrameLayout drawViewLayout = findViewById(R.id.drawViewLayout);

    int sdk = android.os.Build.VERSION.SDK_INT;

    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      drawViewLayout.setBackgroundDrawable(CheckerboardDrawable.create());
    } else {
      drawViewLayout.setBackground(CheckerboardDrawable.create());
    }

    SeekBar strokeBar = findViewById(R.id.strokeBar);
    strokeBar.setMax(MAX_ERASER_SIZE);
    strokeBar.setProgress(50);

    gestureView = findViewById(R.id.gestureView);

    drawView = findViewById(R.id.drawView);
    drawView.setDrawingCacheEnabled(true);
    drawView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    //drawView.setDrawingCacheEnabled(true);
    drawView.setStrokeWidth(strokeBar.getProgress());

    strokeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        drawView.setStrokeWidth(seekBar.getProgress());
      }
    });

    loadingModal = findViewById(R.id.loadingModal);
    loadingModal.setVisibility(INVISIBLE);

    drawView.setLoadingModal(loadingModal);

    manualClearSettingsLayout = findViewById(R.id.manual_clear_settings_layout);

    setUndoRedo();
    initializeActionButtons();

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

     /* if (toolbar.getNavigationIcon() != null) {
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
      }*/

    }

    Button doneButton = findViewById(R.id.done);

    doneButton.setOnClickListener(v -> startSaveDrawingTask());



  }

  private void startSaveDrawingTask() {
    SaveDrawingTask task = new SaveDrawingTask(ResultActivity.this);

    int borderColor;
    if ((borderColor = getIntent().getIntExtra(CutOut.CUTOUT_EXTRA_BORDER_COLOR, -1)) != -1) {
      Bitmap image = BitmapUtility.getBorderedBitmap(this.drawView.getDrawingCache(), borderColor, BORDER_SIZE);
      task.execute(image);
    } else {
      task.execute(this.drawView.getDrawingCache());
    }
  }

  /** Start pick image activity with chooser. */
  public void onSelectImageClick(View view) {
    CropImage.activity()
        .setGuidelines(CropImageView.Guidelines.ON)
        .setActivityTitle("My Crop")
            .setAspectRatio(3,4)
        .setCropShape(CropImageView.CropShape.RECTANGLE)
        .setCropMenuCropButtonTitle("Done")
        .setRequestedSize(400, 400)
        .setCropMenuCropButtonIcon(R.mipmap.ic_launcher)
        .start(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    // handle result of CropImageActivity
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);
      if (resultCode == RESULT_OK) {
        //((ImageView) findViewById(R.id.quick_start_cropped_image)).setImageURI(result.getUri());
       // lists.add(new ImageUriList(result.getUri()));
      //  adapter.notifyDataSetChanged();


          setDrawViewBitmap(result.getUri());



        Toast.makeText(
                this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG)
            .show();
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
      }
    }
  }
  private void activateGestureView() {
    gestureView.getController().getSettings()
            .setMaxZoom(MAX_ZOOM)
            .setDoubleTapZoom(-1f) // Falls back to max zoom level
            .setPanEnabled(true)
            .setZoomEnabled(true)
            .setDoubleTapEnabled(true)
            .setOverscrollDistance(0f, 0f)
            .setOverzoomFactor(2f);
  }

  private void deactivateGestureView() {
    gestureView.getController().getSettings()
            .setPanEnabled(false)
            .setZoomEnabled(false)
            .setDoubleTapEnabled(false);
  }

  private void initializeActionButtons() {
    Button autoClearButton = findViewById(R.id.auto_clear_button);
    Button manualClearButton = findViewById(R.id.manual_clear_button);
    Button zoomButton = findViewById(R.id.zoom_button);

    autoClearButton.setActivated(false);
    autoClearButton.setOnClickListener((buttonView) -> {
      if (!autoClearButton.isActivated()) {
        drawView.setAction(DrawView.DrawViewAction.AUTO_CLEAR);
        manualClearSettingsLayout.setVisibility(INVISIBLE);
        autoClearButton.setActivated(true);
        manualClearButton.setActivated(false);
        zoomButton.setActivated(false);
        deactivateGestureView();
      }
    });

    manualClearButton.setActivated(true);
    drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);
    manualClearButton.setOnClickListener((buttonView) -> {
      if (!manualClearButton.isActivated()) {
        drawView.setAction(DrawView.DrawViewAction.MANUAL_CLEAR);
        manualClearSettingsLayout.setVisibility(VISIBLE);
        manualClearButton.setActivated(true);
        autoClearButton.setActivated(false);
        zoomButton.setActivated(false);
        deactivateGestureView();
      }

    });

    zoomButton.setActivated(false);
    deactivateGestureView();
    zoomButton.setOnClickListener((buttonView) -> {
      if (!zoomButton.isActivated()) {
        drawView.setAction(DrawView.DrawViewAction.ZOOM);
        manualClearSettingsLayout.setVisibility(INVISIBLE);
        zoomButton.setActivated(true);
        manualClearButton.setActivated(false);
        autoClearButton.setActivated(false);
        activateGestureView();
      }

    });
  }

  private void setUndoRedo() {
    Button undoButton = findViewById(R.id.undo);
    undoButton.setEnabled(false);
    undoButton.setOnClickListener(v -> undo());
    Button redoButton = findViewById(R.id.redo);
    redoButton.setEnabled(false);
    redoButton.setOnClickListener(v -> redo());

    drawView.setButtons(undoButton, redoButton);
  }

  public void exitWithError(Exception e) {
    Intent intent = new Intent();
    intent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, e);
    setResult(CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE, intent);
    finish();
  }

  private void setDrawViewBitmap(Uri uri) {
    try {
      Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
      drawView.setBitmap(bitmap);
    } catch (IOException e) {
      exitWithError(e);
    }
  }
  private void undo() {
    drawView.undo();
  }

  private void redo() {
    drawView.redo();
  }


  @NonNull
  public static String getSaveDir() {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/fetch";
  }

}
