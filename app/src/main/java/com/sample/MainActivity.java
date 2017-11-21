/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;
import com.sample.utils.Utils;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class MainActivity extends AppCompatActivity {

    private static String dirPath;
    Button buttonOne,buttonCancelOne;

    TextView textViewProgressOne;

    ProgressBar progressBarOne;

    int downloadIdOne;
    String URL="";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dirPath = Utils.getRootDirPath(getApplicationContext());

        init();
       progressDialog= new ProgressDialog(MainActivity.this);
        Intent intent=getIntent();

        if (intent!=null)
        {
            progressDialog.setTitle("Fetching Details");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Parsing download link");
            progressDialog.show();
            Bundle extras=intent.getExtras();
            if (extras!=null) {
                String link = extras.getString(Intent.EXTRA_TEXT);
                Log.d("Link",link);
                new YouTubeExtractor(MainActivity.this)
                {
                    @Override
                    protected void onExtractionComplete(SparseArray<YtFile> sparseArray, VideoMeta videoMeta) {
                        if (sparseArray != null) {
                            int itag = 22;
                            URL = sparseArray.get(itag).getUrl();
                            Log.d("Download URL",URL);
                            textViewProgressOne.setText(videoMeta.getTitle()+"; 720p");
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            Toast.makeText(MainActivity.this,"Click on start",Toast.LENGTH_SHORT).show();
                        }
                    }
                }.extract(link,true,true);
            }
        }
        onClickListenerOne();

    }

    private void init() {
        buttonOne = findViewById(R.id.buttonOne);


        buttonCancelOne = findViewById(R.id.buttonCancelOne);


        textViewProgressOne = findViewById(R.id.textViewProgressOne);


        progressBarOne = findViewById(R.id.progressBarOne);

    }

    public void onClickListenerOne() {
        buttonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (!URL.equals("")){
                if (Status.RUNNING == PRDownloader.getStatus(downloadIdOne)) {
                    PRDownloader.pause(downloadIdOne);
                    return;
                }

                buttonOne.setEnabled(false);
                progressBarOne.setIndeterminate(true);
                progressBarOne.getIndeterminateDrawable().setColorFilter(
                        Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

                if (Status.PAUSED == PRDownloader.getStatus(downloadIdOne)) {
                    PRDownloader.resume(downloadIdOne);
                    return;
                }

                downloadIdOne = PRDownloader.download(URL, dirPath, "video.mp4")
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                progressBarOne.setIndeterminate(false);
                                buttonOne.setEnabled(true);
                                buttonOne.setText(R.string.pause);
                                buttonCancelOne.setEnabled(true);
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                                buttonOne.setText(R.string.resume);
                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                buttonOne.setText(R.string.start);
                                buttonCancelOne.setEnabled(false);
                                progressBarOne.setProgress(0);
                                textViewProgressOne.setText("");
                                downloadIdOne = 0;
                                progressBarOne.setIndeterminate(false);
                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                                progressBarOne.setProgress((int) progressPercent);
                                textViewProgressOne.setText(Utils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                                progressBarOne.setIndeterminate(false);
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                buttonOne.setEnabled(false);
                                buttonCancelOne.setEnabled(false);
                                buttonOne.setText(R.string.completed);
                            }

                            @Override
                            public void onError(Error error) {
                                buttonOne.setText(R.string.start);
                                Toast.makeText(getApplicationContext(), getString(R.string.some_error_occurred) + " " + "1", Toast.LENGTH_SHORT).show();
                                textViewProgressOne.setText("");
                                progressBarOne.setProgress(0);
                                downloadIdOne = 0;
                                buttonCancelOne.setEnabled(false);
                                progressBarOne.setIndeterminate(false);
                                buttonOne.setEnabled(true);
                            }
                        });
            }
            else
            {
              Toast.makeText(MainActivity.this,"Enter Link",Toast.LENGTH_SHORT).show();
            }}
        });

        buttonCancelOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(downloadIdOne);
            }
        });
    }

}

