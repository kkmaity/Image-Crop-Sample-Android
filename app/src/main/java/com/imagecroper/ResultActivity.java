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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.imagecroper.cropImage.CropImage;
import com.imagecroper.cropImage.CropImageView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

  private RecyclerView mRecyclerView;
  private ArrayList<ImageUriList> lists;
private ImageCropedAdapter adapter;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_results);
    mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
    lists=new ArrayList<>();
    // use this setting to improve performance if you know that changes
    // in content do not change the layout size of the RecyclerView
    mRecyclerView.setHasFixedSize(true);
    // use a linear layout manager
    GridLayoutManager manager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
    mRecyclerView.setLayoutManager(manager);
    adapter=new ImageCropedAdapter(lists);
    mRecyclerView.setAdapter(adapter);

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
        lists.add(new ImageUriList(result.getUri()));
        adapter.notifyDataSetChanged();

        Toast.makeText(
                this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG)
            .show();
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
      }
    }
  }
}
