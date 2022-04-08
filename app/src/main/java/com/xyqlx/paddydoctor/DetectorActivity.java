/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xyqlx.paddydoctor;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.xyqlx.paddydoctor.customview.OverlayView;
import com.xyqlx.paddydoctor.customview.OverlayView.DrawCallback;
import com.xyqlx.paddydoctor.env.BorderedText;
import com.xyqlx.paddydoctor.env.ImageUtils;
import com.xyqlx.paddydoctor.env.Logger;
import com.xyqlx.paddydoctor.tflite.Classifier;
import com.xyqlx.paddydoctor.tflite.DetectorFactory;
import com.xyqlx.paddydoctor.tflite.YoloV5Classifier;
import com.xyqlx.paddydoctor.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.3f;
    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 640);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    // 自己定义的
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    // 自己定义的
    private YoloV5Classifier detector;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    // 自己定义的
    private MultiBoxTracker tracker;
    // 自己定义的，这个由我自己注释，原因是borderedText并没有被使用
    private BorderedText borderedText;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        Log.i("-","------------------------");
        Log.i("onPreviewSizeChosen","onPreviewSizeChosen");
//        System.out.println(MODE);

        // 这个由我自己注释，原因是borderedText并没有被使用
        final float textSizePx =
                // TypedValue.applyDimension把Android系统中的非标准度量尺寸转变为标准度量尺寸
                // (Android系统中的标准尺寸是px, 即像素)(px是安卓系统内部使用的单位, dp是与设备无关的尺寸单位 )
                // 非标准单位: dp, in, mm, pt, sp
                // 如下的写法就是dp->px，返回TEXT_SIZE_DIP * metrics.density
                // getResources().getDisplayMetrics()获取屏幕信息
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE); // Typeface.MONOSPACE字体

        // 定义了画板画笔颜色和画图的函数
        tracker = new MultiBoxTracker(this);

        // 获取选中项
        final int modelIndex = modelView.getCheckedItemPosition();
        final String modelString = modelStrings.get(modelIndex);

        try {
            detector = DetectorFactory.getDetector(getAssets(), modelString);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        int cropSize = detector.getInputSize();

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        // 将照相机获取的原始图片，转换为cropSize*cropSize的图片，用来作为模型预测的输入。
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);
                        }
//                        borderedText.drawText(canvas, "" + java.lang.String(num));
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
        Log.i("-","------------------------");
        Log.i("onPreviewSizeChosenEND","onPreviewSizeChosenEND");
    }

    protected void updateActiveModel() {
        Log.i("-","------------------------");
        Log.i("updateActiveModel","updateActiveModel");
        // Get UI information before delegating to background
        final int modelIndex = modelView.getCheckedItemPosition();
        final int deviceIndex = deviceView.getCheckedItemPosition();
        String threads = threadsTextView.getText().toString().trim();
        final int numThreads = Integer.parseInt(threads);

        handler.post(() -> {
            if (modelIndex == currentModel && deviceIndex == currentDevice
                    && numThreads == currentNumThreads) {
                return;
            }
            currentModel = modelIndex;
            currentDevice = deviceIndex;
            currentNumThreads = numThreads;

            // Disable classifier while updating
            if (detector != null) {
                detector.close();
                detector = null;
            }

            // Lookup names of parameters.
            String modelString = modelStrings.get(modelIndex);
            String device = deviceStrings.get(deviceIndex);

            LOGGER.i("Changing model to " + modelString + " device " + device);

            // Try to load model.

            try {
                detector = DetectorFactory.getDetector(getAssets(), modelString);
                // Customize the interpreter to the type of device we want to use.
                if (detector == null) {
                    return;
                }
            }
            catch(IOException e) {
                e.printStackTrace();
                LOGGER.e(e, "Exception in updateActiveModel()");
                Toast toast =
                        Toast.makeText(
                                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }


            if (device.equals("CPU")) {
                detector.useCPU();
            } else if (device.equals("GPU")) {
                detector.useGpu();
            } else if (device.equals("NNAPI")) {
                detector.useNNAPI();
            }
            detector.setNumThreads(numThreads);

            int cropSize = detector.getInputSize();
            croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

            frameToCropTransform =
                    ImageUtils.getTransformationMatrix(
                            previewWidth, previewHeight,
                            cropSize, cropSize,
                            sensorOrientation, MAINTAIN_ASPECT);

            cropToFrameTransform = new Matrix();
            frameToCropTransform.invert(cropToFrameTransform);
        });
        Log.i("-","------------------------");
        Log.i("updateActiveModelEND","updateActiveModelEND");
    }

    @Override
    protected void processImage() {
//        Log.i("-","------------------------");
//        Log.i("processImage","processImage");
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        Log.i("-","------------------------");
        Log.i("processImage","processImage");
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        // 图片的绘制等，不是模型预测的重点，不分析了
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        // 利用分类器classifier对图片进行预测分析，得到图片为每个分类的概率. 比较耗时，放在子线程中
        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.i("Running detection on image " + currTimestamp);

                        final long startTime = SystemClock.uptimeMillis();
                        // classifier对图片进行识别，得到输入图片为每个分类的概率
                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                        Log.e("CHECK", "run: " + results.size());

                        DetectorActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                DetectorActivity.super.setThisTitle(results.size());
                            }
                        });

                        // 将得到的前三个最大概率的分类的名字及概率，反馈到app上。也就是results区域
                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        final Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Style.STROKE);
                        paint.setStrokeWidth(2.0f);

                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        switch (MODE) {
                            case TF_OD_API:
                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                break;
                        }

                        final List<Classifier.Recognition> mappedRecognitions =
                                new LinkedList<Classifier.Recognition>();

                        for (final Classifier.Recognition result : results) {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                canvas.drawRect(location, paint);

                                cropToFrameTransform.mapRect(location);

                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }

                        tracker.trackResults(mappedRecognitions, currTimestamp);
                        trackingOverlay.postInvalidate();

                        computingDetection = false;

                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        showFrameInfo(previewWidth + "x" + previewHeight);
                                        showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                                        showInference(lastProcessingTimeMs + "ms");
                                    }
                                });
                    }
                });
        Log.i("-","------------------------");
        Log.i("processImageEND","processImageEND");
    }

    @Override
    protected int getLayoutId() {
        Log.i("-","------------------------");
        Log.i("getLayoutId","getLayoutId");
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        Log.i("-","------------------------");
        Log.i("getDesiredPreviewF...","getDesiredPreviewFrameSize");
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        Log.i("-","------------------------");
        Log.i("setUseNNAPI","setUseNNAPI");
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        Log.i("-","------------------------");
        Log.i("setNumThreads","setNumThreads");
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
}
