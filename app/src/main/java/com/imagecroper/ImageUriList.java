package com.imagecroper;

import android.net.Uri;

public class ImageUriList {
    private Uri imageUri;

    public ImageUriList(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
