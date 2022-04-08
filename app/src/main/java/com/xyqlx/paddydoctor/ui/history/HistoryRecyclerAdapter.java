package com.xyqlx.paddydoctor.ui.history;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xyqlx.paddydoctor.FlaskServerService;
import com.xyqlx.paddydoctor.R;
import com.xyqlx.paddydoctor.data.model.HistoryItem;
import com.xyqlx.paddydoctor.data.model.LoggedInUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<HistoryItem> mDatas;

    public HistoryRecyclerAdapter(Context context, ArrayList<HistoryItem> datas) {
        mContext = context;
        mDatas = datas;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_history, parent, false);
        return new HistoryItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryItemHolder historyItemHolder = (HistoryItemHolder) holder;
        historyItemHolder.itemType.setText(mDatas.get(position).getDetectType());
        historyItemHolder.itemConfidence.setText(String.format(Locale.CHINA, "%.2f%%", mDatas.get(position).getConfidence()));
        loadImage(mDatas.get(position).getFileId(), historyItemHolder.itemImage);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private void loadImage(final String fileId, ImageView imageView) {
        // TODO 加载本地缓存
        // 请求
        Retrofit retrofit = new Retrofit.Builder().baseUrl(FlaskServerService.HttpConfig.BASE_URL).build();
        FlaskServerService serverService = retrofit.create(FlaskServerService.class);
        String token = LoggedInUser.getLoggedInUser(mContext).getUserToken();
        Call<ResponseBody> call = serverService.getImage(fileId, token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //数据请求成功
                Log.d("", "success");
                ResponseBody body = response.body();
                try {
                    String path = mContext.getExternalFilesDir(null) + fileId + "." + body.contentType().subtype();
                    File futureStudioIconFile = new File(path);
                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = body.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = body.byteStream();
                        outputStream = new FileOutputStream(futureStudioIconFile);

                        while (true) {
                            int read = inputStream.read(fileReader);

                            if (read == -1) {
                                break;
                            }

                            outputStream.write(fileReader, 0, read);

                            fileSizeDownloaded += read;

                            Log.d("", "file download: " + fileSizeDownloaded + " of " + fileSize);
                        }
                        outputStream.flush();
                        imageView.setImageBitmap(BitmapFactory.decodeFile(path));
                    } catch (IOException e) {
                        Log.d("", "download failed");
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } catch (IOException e) {
                    Log.d("", "parse failed");
                    e.printStackTrace();
                }

                // Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //数据请求失败
                Log.d("", "failure");
            }
        });
    }

    public class HistoryItemHolder extends RecyclerView.ViewHolder {
        public ImageView itemImage;
        public TextView itemType;
        public TextView itemConfidence;

        public HistoryItemHolder(View itemView) {
            super(itemView);
            itemImage = (ImageView) itemView.findViewById(R.id.item_image);
            itemType = (TextView) itemView.findViewById(R.id.item_type);
            itemConfidence = (TextView) itemView.findViewById(R.id.item_confidence);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, itemType.getText(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
