package com.codepath.kathyxing.booknook;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class ImageSelectionUtilities {

    /**
     * Returns the Bitmap of the correctly rotated photo given the photo uri
     *
     * @param photoUri        the photo uri
     * @param contentResolver the content resolver
     * @return the rotated bitmap
     * @throws IOException an I/O exception
     */
    public static Bitmap rotateBitmapOrientationGallery(Uri photoUri, ContentResolver contentResolver) throws IOException {
        InputStream input = contentResolver.openInputStream(photoUri);
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, bounds);
        Bitmap bm = MediaStore.Images.Media.getBitmap(contentResolver, photoUri);
        // Read EXIF Data
        InputStream inputStream = contentResolver.openInputStream(photoUri);
        ExifInterface exif = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            exif = new ExifInterface(inputStream);
        }
        if (exif != null) {
            // exif is not null
            // get the orientation and set the rotation angle accordingly
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            // Rotate Bitmap
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            // Return result
            return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        }
        // return the original bitmap if exif is null
        return bm;
    }

    /**
     * Returns the Bitmap of the correctly rotated photo given the photo file path
     *
     * @param photoFilePath the photo file's path
     * @return the rotated bitmap
     */
    // rotate the image to the correct orientation using the EXIF data stored in the image
    public static Bitmap rotateBitmapOrientationCamera(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif != null) {
            // exif is not null
            // get the orientation and set the rotation angle accordingly
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            // Rotate Bitmap
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            // Return result
            return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        }
        // exif is null, return the original bitmap
        return bm;
    }

    /**
     * Returns the File for a photo stored on disk given the fileName
     *
     * @param fileName the file name
     * @param context  the context
     * @param TAG      the tag
     * @return the file for the photo
     */
    public static File getPhotoFileUri(String fileName, Context context, String TAG) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    /**
     * Crops the bitmap into a circle
     *
     * @param bitmap the bitmap to crop
     * @return the cropped bitmap
     */
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Log.i("HELLO", "cropping bitmap");
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
