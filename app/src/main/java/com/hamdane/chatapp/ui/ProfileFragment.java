package com.hamdane.chatapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.hamdane.chatapp.Api;
import com.hamdane.chatapp.R;
import com.hamdane.chatapp.listenner.Listener;
import com.hamdane.chatapp.model.User;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username;

    private static final int IMAGE_REQUEST = 1;


    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        username.setText(Api.currentUser.getDisplayName());
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });
        Api.getUser(Api.currentUser.getUid(), new Listener<User>() {
            @Override
            public void value(User user) {
                if (user.getAvatar().equals("default")) {
                    image_profile.setImageResource(R.drawable.ic_baseline_account_circle_24);
                } else {
                    byte[] decodedString = Base64.decode(user.getAvatar(), Base64.DEFAULT);
                    Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    image_profile.setImageBitmap(src);
                }
            }
        });
        return view;
    }

    private void openImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Selectioner un photo");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();
            final String encodedImage = prepareImage(imageUri,200);
            Api.updateAvatar(encodedImage, new Listener() {
                @Override
                public void success() {
                    Toast.makeText(getContext(), "L'image de profil a été mise à jour!", Toast.LENGTH_SHORT).show();

                    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    image_profile.setImageBitmap(src);
                }
            });
        }
    }
    public String prepareImage(Uri uri, final int size) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(this.getContext().getContentResolver().openInputStream(uri), null, o);
        }
        catch (Exception e) {
            return null;
        }

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < size || height_tmp / 2 < size)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(this.getContext().getContentResolver().openInputStream(uri), null, o2);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        }
        catch (Exception e) {
            return null;
        }
    }
}