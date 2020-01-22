package com.bhavaniprasad.downloadtask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private String Tag;
    private int recordcount;
    DatabaseManager mDatabase;
    ThreadPoolExecutor executor,executor2;
    private TextView textView;
    Instant start;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = Instant.now();
        progressBar = findViewById(R.id.progress_bar);
        mDatabase = new DatabaseManager(this);
        textView= (TextView) findViewById(R.id.textview);
        Tag = "Mainactivitydownload";
        recordcount=0;
        try{
            progressBar.setVisibility(View.VISIBLE);
            this.runIt(mDatabase,this,start);
        }
        catch(IOException e){
            System.err.println(e);
        }
    }
    class Callback {
        private int i;
        public Callback(int i){
            this.i = i;
        }
        public void callbackMethod(){
//            executor.shutdown();
            System.out.println("Call back:"+i);
        }
    }

    private class Downloader implements Callable<Long> {
        Context context;
        DatabaseManager nDatabase ;
        Long id = 0L;
        Callback callback;
        Instant start;
        Uri uri;
        private final URL url;
        String path,filename,result;
        File theFile,mediaStorageDirp;
        String[] filewithtype;
        byte[] bytearray;

        public Downloader(URL url,Context cnt,DatabaseManager db,Long val,Callback obj,Instant start) {
            this.url = url;
            this.context=cnt;
            this.nDatabase=db;
            this.id = val;
            this.callback = obj;
            this.start=start;
        }

        private String readAll(Reader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            int read = 0;
            while ((read = reader.read()) != -1) {
                builder.append((char) read);
            }
            return builder.toString();
        }
        @Override
        public Long call() throws Exception {
            callback.callbackMethod();
            Reader reader = null;
            InputStream input = null;
            OutputStream output = null;
            try {
                    int count;
                    uri = Uri.parse(url.toString());
                    path = uri.getPath();
                    theFile = new File(path);
                    filewithtype=theFile.getName().split("\\.");
                    filename=filewithtype[0];
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    new File(Environment.getExternalStorageDirectory(), filename);
                    mediaStorageDirp = new File(Environment.getExternalStorageDirectory().toString()+"/"+filename.concat(".html"));
                    output = new FileOutputStream(mediaStorageDirp);
                    result = readAll(reader);
                    input = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
                    byte data[] = new byte[1024*1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }
                    output.flush();
            } catch (IOException e) {
                System.err.println(e);
            }
            catch (Exception e){
                System.err.println(e);
            }
            finally {
                if (reader != null)
                    reader.close();
                if(output != null)
                    output.close();
                if(input != null)
                    input.close();
            }
            return id;
        }
    }


    public void runIt(DatabaseManager mDatabase,Context context,Instant start) throws MalformedURLException {
        StringBuffer aBuffer;
        Uri uri;
        int reccount=0;
        String path,filename,result;
        File theFile,mediaStorageDirp;
        String[] filewithtype;
        String fileurl,file_url;
        char c,replaceWith;
        List<Future<Long>> list = new ArrayList<Future<Long>>();
        BlockingQueue<Runnable> runnables = new ArrayBlockingQueue<Runnable>(1024);
        BlockingQueue<Runnable> runnables2 = new ArrayBlockingQueue<Runnable>(1024);
        executor = new ThreadPoolExecutor(26, 26, 1, TimeUnit.NANOSECONDS, runnables);
        file_url = "http://www.mso.anu.edu.au/~ralph/OPTED/v003/wb1913_a.html";
        try{
            for(c = 'a'; c <= 'z'; ++c) {
                Callback callback = new Callback(c+1);
                replaceWith = c;
                aBuffer = new StringBuffer(file_url.toString());
                aBuffer.setCharAt(aBuffer.length() - 6, replaceWith);
                fileurl = aBuffer.toString();
                Future<Long> future = executor.submit(new Downloader(new URL(fileurl),context,mDatabase,(long)c+1,callback,start));
                list.add(future);
            }
        }
        catch(Exception e){
            Log.e("Exception","Download exception"+e);
        }
        for(Future<Long> fut : list){
            try {
                if(executor.getCompletedTaskCount()==26) {
                    reccount=26;
                    break;
                }
                //print the return value of Future, notice the output delay in console
                // because Future.get() waits for task to get completed
                System.out.println(new Date()+ "RRR::"+fut.get());
                reccount++;
            } catch (InterruptedException | ExecutionException e ) {
                e.printStackTrace();
            }
        }
        if(reccount==26){
            executor.shutdownNow();
            try{
                uri = Uri.parse(file_url.toString());
                path = uri.getPath();
                theFile = new File(path);
                filewithtype=theFile.getName().split("\\.");
                filename=filewithtype[0];
                byte[] bytearray;
                for(c = 'a'; c <= 'z'; ++c) {
                    replaceWith = c;
                    aBuffer = new StringBuffer(filename.toString());
                    aBuffer.setCharAt(aBuffer.length() - 1, replaceWith);
                    filename = aBuffer.toString();
                    bytearray = Files.readAllBytes(Paths.get(Environment.getExternalStorageDirectory().toString()+"/"+filename.concat(".html")));
                    executor2 = new ThreadPoolExecutor(26, 26, 1, TimeUnit.NANOSECONDS, runnables2);
                    executor2.submit(new Storetodb(bytearray,filename,mDatabase,context,start));
                }
            }
            catch (IOException ie){
                System.err.println(ie);
            }
            executor2.shutdown();
        }
         executor.shutdown();
    }


    private class Storetodb implements Callable<Long> {
        byte[] bytearray;
        String filename;
        DatabaseManager db;
        Context cnt;
        Instant start;

        public Storetodb(byte[] bytearray,String filename,DatabaseManager db,Context cnt,Instant start) {
            this.bytearray=bytearray;
            this.filename=filename;
            this.cnt=cnt;
            this.db=db;
            this.start=start;
        }

        @Override
        public Long call() throws Exception {
            if(db.adddata(bytearray,filename)){
                recordcount++;
                Log.e(Tag,"added succesfully"+recordcount);
                if(recordcount==26){
                    executor2.shutdownNow();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            TextView txtView = (TextView) ((Activity)cnt).findViewById(R.id.textview);
                            progressBar = (ProgressBar)((Activity)cnt).findViewById(R.id.progress_bar);
                            Instant finish = Instant.now();
                            long timeElapsed = Duration.between(start, finish).toMillis();
                            progressBar.setVisibility(View.INVISIBLE);
                            txtView.setText(String.valueOf(timeElapsed)+" milliseconds");
                        }
                    });
                }
            }
            return null;
        }
    }
}