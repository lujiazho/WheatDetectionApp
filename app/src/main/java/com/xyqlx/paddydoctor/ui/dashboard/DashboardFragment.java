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
    // 一下变量都是自己加的
    private ImageView imageView;
    private TextView resultTextView;
    private TextView resultTitleTextView;
//    private ImageButton feedbackButton;
    private Spinner switchModelSpinner;
    // 服务端返回的文件id
    private String fileId;
    // TODO 模型名从服务器下载
    private String[] models = {"best", "detect"};
    private String[] modelNames = {"yolov5s", "实时检测"};
    private int modelIndex = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 获取view控件并设置listener
        imageView = root.findViewById(R.id.imageView);
        ImageButton shotButton = root.findViewById(R.id.shotButton);
        resultTextView = (TextView) root.findViewById(R.id.resultTextView);
        resultTitleTextView = root.findViewById(R.id.resultTitleTextView);
//        feedbackButton = root.findViewById(R.id.feedbackButton);
        shotButton.setOnClickListener(this::shotPhoto);
        ImageButton albumButton = root.findViewById(R.id.albumButton);
        albumButton.setOnClickListener(this::selectFromAlbum);
//        feedbackButton.setOnClickListener(v -> startActivityForResult(new Intent(this.getContext(), DetectorActivity.class), 1));
        // 初始化切换模型控件
        switchModelSpinner = root.findViewById(R.id.switchModelSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_model_spinner, modelNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switchModelSpinner.setAdapter(adapter);
        switchModelSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());

        return root;
    }

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int IMAGE_REQUEST_CODE = 2;

    // 从相册选取图片
    private void selectFromAlbum(View v) {
        // 没有权限时请求
        if(ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.getActivity(),new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }
        // 启动选取界面
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    // 照相获取图片
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

    // 拍照
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
            Log.i("到哪儿了", "到哪儿了");
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i("启动activity前", "启动activity前");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // 此方法可以读取拍照的缩略图
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
            // 经过截胡后，data里不再包含数据，需要从文件系统读取
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

    // 测试连通
    private void postImage1(File file){
        String url = "http://139.217.228.30:13579/classifier/test";
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
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

    // 把照片post到服务器
    private void postImage(File file){
        // 等待中
        this.resultTextView.setVisibility(View.VISIBLE);
        this.resultTitleTextView.setMaxEms(2);
        this.resultTextView.setText("识别中……");
        // 请求
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        FlaskServerService serverService = retrofit.create(FlaskServerService.class);
        // 用图像file创建MultipartBody.Part，以表单进行传输
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        // 获取token，一起传给服务器，以便保持session
        String token = LoggedInUser.getLoggedInUser(this.getContext()).getUserToken();

        // 创建分类请求
        Call<ResponseBody> call = serverService.classify(models[modelIndex], token, filePart);
        // 异步发送
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // 数据请求成功，得到返回数据
                Log.d("", "success");
                String body = "{}";

                try {
                    // 得到主题转化成string
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

                // 手动解析
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();

                // TODO 异常处理
                String imgpath = obj.get("imgpath").getAsString();

                System.out.println(imgpath);

                // 获得分类类型
                String number = obj.get("number").getAsString();
//                Log.d("位置", "type后");
                // String text = obj.get("text").getAsString();
                // 获得置信度
//                Double confidence = obj.get("confidence").getAsDouble();
                // 获得文件id
//                fileId = obj.get("fileid").getAsString();
                // String info = "病虫害类型为" + type + "，" + text;
//                String info = "所示农作物的病害类型有"
//                        + String.format(Locale.CHINA, "%.0f%%", confidence)
//                        + "的可能为" + type;
                // 不开新线程会报错
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 显示图片
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

                String info = "所示图片中麦穗数为" + number;
                resultTextView.setText(info);
                // feedbackButton.setVisibility(View.VISIBLE);
                // Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //数据请求失败
                Log.d("", "failure");
            }
        });
    }

    // 反馈
    private void sendFeedback(View v){
        if(fileId == null){
            Toast.makeText(getContext(), "识别后点击此按钮反馈", Toast.LENGTH_SHORT).show();
        }
        final EditText editText = new EditText(this.getActivity());
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(this.getContext());
        inputDialog.setTitle("您对本次识别结果有什么意见吗？").setView(editText);
        inputDialog.setPositiveButton(Html.fromHtml("<font color='#000000'>确定</font>", Html.FROM_HTML_MODE_COMPACT),
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
                                //数据请求成功
                                Toast.makeText(DashboardFragment.this.getContext(), "发送成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                //数据请求失败
                            }
                        });
                    }
                }).show();
    }

    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            Toast.makeText(getContext(), "当前模型为" + modelNames[arg2], Toast.LENGTH_SHORT).show();
            modelIndex = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}