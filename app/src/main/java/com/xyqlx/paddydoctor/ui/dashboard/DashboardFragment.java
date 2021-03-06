package com.xyqlx.paddydoctor.ui.dashboard;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xyqlx.paddydoctor.BottomNavigationActivity;
import com.xyqlx.paddydoctor.DetectorActivity;
import com.xyqlx.paddydoctor.FlaskServerService;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;
import com.xyqlx.paddydoctor.ui.ActionBarUtils;
import com.xyqlx.paddydoctor.ui.login.LoginActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    // ??????????????????????????????
    private ImageView imageView;
    private TextView resultTextView;
    private TextView resultTitleTextView;
//    private ImageButton feedbackButton;
    private Spinner switchModelSpinner;
    // ????????????????????????id
    private String fileId;
    // TODO ???????????????????????????
    private String[] models = {"best", "detect"};
    private String[] modelNames = {"yolov5s", "????????????"};
    private int modelIndex = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // ??????view???????????????listener
        imageView = root.findViewById(R.id.imageView);
        ImageButton shotButton = root.findViewById(R.id.shotButton);
        resultTextView = (TextView) root.findViewById(R.id.resultTextView);
        resultTitleTextView = root.findViewById(R.id.resultTitleTextView);
//        feedbackButton = root.findViewById(R.id.feedbackButton);
        shotButton.setOnClickListener(this::shotPhoto);
        ImageButton albumButton = root.findViewById(R.id.albumButton);
        albumButton.setOnClickListener(this::selectFromAlbum);
//        feedbackButton.setOnClickListener(v -> startActivityForResult(new Intent(this.getContext(), DetectorActivity.class), 1));
        // ???????????????????????????
        switchModelSpinner = root.findViewById(R.id.switchModelSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_model_spinner, modelNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switchModelSpinner.setAdapter(adapter);
        switchModelSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());

        return root;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int IMAGE_REQUEST_CODE = 2;

    // ?????????????????????
    private void selectFromAlbum(View v) {
        // ?????????????????????
        if(ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.getActivity(),new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }
        // ??????????????????
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    // ??????????????????
    public void shotPhoto(View view){
        dispatchTakePictureIntent();
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
        File image = new File(storageDir, imageFileName +".jpg");
        if(image.exists()) {
            image.delete();
        }
        image.createNewFile();
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // ??????
    private void dispatchTakePictureIntent() {
        if (modelIndex == 1) {
            Intent myIntent = new Intent(getContext(), DetectorActivity.class);
            getContext().startActivity(myIntent);
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getContext(), "Error occurred while creating the File", Toast.LENGTH_SHORT).show();
            }
            Log.i("????????????", "????????????");
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i("??????activity???", "??????activity???");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // ???????????????????????????????????????
    private void dispatchTakePictureIntent1() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "onActivityResult: ");
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            // ??????????????????data???????????????????????????????????????????????????
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //imageView.setImageBitmap(imageBitmap);
            imageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
            File file = new File(currentPhotoPath);
            postImage(file);
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            Uri uri = data.getData();
            String path = getRealPathFromURI(uri);
            imageView.setImageBitmap(BitmapFactory.decodeFile(path));
            File file = new File(path);
            postImage(file);
        }
    }

    // ????????????
    private void postImage1(File file){
        String url = "http://139.217.228.30:13579/classifier/test";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//????????????GET?????????????????????
                .build();
        okhttp3.Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("", "onFailure: ");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.d("", "onResponse: " + response.body().string());
            }
        });
    }

    private String getRealPathFromURI(Uri contentURI) {
        Log.i("getRealPathFromURI", "getRealPathFromURI: ");
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // ?????????post????????????
    private void postImage(File file){
        // ?????????
        this.resultTextView.setVisibility(View.VISIBLE);
        this.resultTitleTextView.setMaxEms(2);
        this.resultTextView.setText("???????????????");
        // ??????
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        FlaskServerService serverService = retrofit.create(FlaskServerService.class);
        // ?????????file??????MultipartBody.Part????????????????????????
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        // ??????token???????????????????????????????????????session
        String token = LoggedInUser.getLoggedInUser(this.getContext()).getUserToken();

        // ??????????????????
        Call<ResponseBody> call = serverService.classify(models[modelIndex], token, filePart);
        // ????????????
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // ???????????????????????????????????????
                Log.d("", "success");
                String body = "{}";

                try {
                    // ?????????????????????string
                    body = response.body().string();
                } catch (IOException e) {
                    Log.d("", "parse failed");
                    e.printStackTrace();
                }

//                final Paint boxPaint = new Paint();
//                boxPaint.setColor(Color.RED);
//                boxPaint.setAlpha(200);
//                boxPaint.setStyle(Paint.Style.STROKE);
//                Canvas newimg = null;
//                newimg.drawRect(250, 75, 350, 120, boxPaint);
//                imageView.draw(newimg);

                // ????????????
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();

                // TODO ????????????
                String imgpath = obj.get("imgpath").getAsString();

                System.out.println(imgpath);

                // ??????????????????
                String number = obj.get("number").getAsString();
//                Log.d("??????", "type???");
                // String text = obj.get("text").getAsString();
                // ???????????????
//                Double confidence = obj.get("confidence").getAsDouble();
                // ????????????id
//                fileId = obj.get("fileid").getAsString();
                // String info = "??????????????????" + type + "???" + text;
//                String info = "?????????????????????????????????"
//                        + String.format(Locale.CHINA, "%.0f%%", confidence)
//                        + "????????????" + type;
                // ????????????????????????
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // ????????????
                        Bitmap bmp = null;
                        String url = FlaskServerService.HttpConfig.BASE_URL + imgpath;
                        try {
                            InputStream is = (InputStream) new URL(url).getContent();
                            bmp = BitmapFactory.decodeStream(is);
                            is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        imageView.setImageBitmap(bmp);
                    }
                };
                new Thread(runnable).start();

                String info = "???????????????????????????" + number;
                resultTextView.setText(info);
                // feedbackButton.setVisibility(View.VISIBLE);
                // Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //??????????????????
                Log.d("", "failure");
            }
        });
    }

    // ??????
    private void sendFeedback(View v){
        if(fileId == null){
            Toast.makeText(getContext(), "??????????????????????????????", Toast.LENGTH_SHORT).show();
        }
        final EditText editText = new EditText(this.getActivity());
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(this.getContext());
        inputDialog.setTitle("?????????????????????????????????????????????").setView(editText);
        inputDialog.setPositiveButton(Html.fromHtml("<font color='#000000'>??????</font>", Html.FROM_HTML_MODE_COMPACT),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JsonObject object = new JsonObject();
                        object.addProperty("fileid", fileId);
                        object.addProperty("feedback", editText.getText().toString());
                        Retrofit retrofit=new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
                        RequestBody body=RequestBody.create(MediaType.parse("application/json; charset=utf-8"),object.toString());
                        FlaskServerService serverService = retrofit.create(FlaskServerService.class);
                        String token = LoggedInUser.getLoggedInUser(DashboardFragment.this.getContext()).getUserToken();
                        Call<ResponseBody> call = serverService.feedback(token, body);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                //??????????????????
                                Toast.makeText(DashboardFragment.this.getContext(), "????????????", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                //??????????????????
                            }
                        });
                    }
                }).show();
    }

    //????????????????????????
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            Toast.makeText(getContext(), "???????????????" + modelNames[arg2], Toast.LENGTH_SHORT).show();
            modelIndex = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}