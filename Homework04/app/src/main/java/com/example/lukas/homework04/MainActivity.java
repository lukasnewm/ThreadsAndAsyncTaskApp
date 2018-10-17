package com.example.lukas.homework04;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Lukas Newman, Group 6

public class MainActivity extends AppCompatActivity {

    ExecutorService threadPool;
    SeekBar seekLength;
    SeekBar seekAmount;
    TextView textUpdateLength;
    TextView textUpdateAmount;
    TextView showPass;
    Button btnGenThread;
    Button btnGenAsync;
    int length; int amount;
    Handler myHandle;
    ProgressDialog myProgress;
    CharSequence [] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPool = Executors.newFixedThreadPool(2);
        textUpdateAmount = (TextView)findViewById(R.id.textCountUpdate);
        showPass = (TextView)findViewById(R.id.textPassword) ;
        textUpdateLength = (TextView)findViewById(R.id.textLengthUpdate);
        btnGenAsync = (Button)findViewById(R.id.buttAsync);
        btnGenThread = (Button)findViewById(R.id.buttThread);
        seekAmount = (SeekBar)findViewById(R.id.seekAmount);
        seekLength = (SeekBar)findViewById(R.id.seekLength);
        length = 0; amount = 0;

        length = seekLength.getProgress() + 8;
        textUpdateLength.setText(Integer.toString(length));
        seekLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                length = i + 8;
                textUpdateLength.setText(Integer.toString(length));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        amount = seekAmount.getProgress() + 1;
        textUpdateAmount.setText(Integer.toString(amount));
        seekAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                amount = i + 1;
                textUpdateAmount.setText(Integer.toString(amount));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnGenThread.setOnClickListener(new View.OnClickListener() {
            //ArrayList<String> passwords = new ArrayList<>();
            @Override
            public void onClick(View view) {
                threadPool.execute(new makePasswords());
            }
        });

        btnGenAsync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //threadPool.execute(new makePasswordsAsync());
                new makePasswordsAsync().execute(amount);
            }
        });

        myHandle = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                int currprogress = 0;
                switch (message.what) {
                    case makePasswords.STATUS_START:
                        myProgress = new ProgressDialog(MainActivity.this);
                        myProgress.setCancelable(false);
                        myProgress.setMessage("Generating Passwords");
                        myProgress.setMax(100);
                        myProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        myProgress.setProgress(0);
                        myProgress.show();
                        break;
                    case makePasswords.STATUS_PROGRESS:
                        currprogress = (100 * (Integer)message.obj) / amount;
                        myProgress.setProgress(currprogress);

                        break;
                    case makePasswords.STATUS_STOP:
                        myProgress.setProgress(100);
                        myProgress.dismiss();
                        ArrayList<String> arrString = (ArrayList<String>)message.obj;
                        items = arrString.toArray(new CharSequence[arrString.size()]);

                        AlertDialog.Builder myBuild = new AlertDialog.Builder(MainActivity.this);
                        myBuild.setTitle("Select Password")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        showPass.setText(items[i]);
                                    }
                                });
                        final AlertDialog alert = myBuild.create();
                        alert.show();
                        break;
                }

                return false;
            }
        });


    }

    class makePasswords implements Runnable
    {
        static final int STATUS_START = 01;
        static final int STATUS_PROGRESS = 02;
        static final int STATUS_STOP = 03;
        @Override
        public void run() {
            Util myGenerator = new Util();
            ArrayList<String> passwords = new ArrayList<>();
            Message msgStart = new Message();
            msgStart.what = STATUS_START;
            myHandle.sendMessage(msgStart);

            for (int i = 0; i < amount; i++)
            {
                passwords.add(myGenerator.getPassword(length));
                Message msg = new Message();
                msg.what = STATUS_PROGRESS;
                msg.obj = (Integer)i + 1;
                myHandle.sendMessage(msg);
            }

            Message msgStop = new Message();
            msgStop.what = STATUS_STOP;
            msgStop.obj = (ArrayList<String>)passwords;
            myHandle.sendMessage(msgStop);

        }
    }

    class makePasswordsAsync extends AsyncTask<Integer, Integer, ArrayList<String>>{
        @Override
        protected void onPreExecute() {
            myProgress = new ProgressDialog(MainActivity.this);
            myProgress.setCancelable(false);
            myProgress.setMessage("Generating Passwords");
            myProgress.setMax(100);
            myProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            myProgress.setProgress(0);
            myProgress.show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> myStringList) {
            myProgress.setProgress(100);
            myProgress.dismiss();


            items = myStringList.toArray(new CharSequence[myStringList.size()]);

            AlertDialog.Builder myBuild = new AlertDialog.Builder(MainActivity.this);
            myBuild.setTitle("Select Password")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showPass.setText(items[i]);
                        }
                    });
            final AlertDialog alert = myBuild.create();
            alert.show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int currprogress = 0;// = myProgress.getProgress();
            currprogress = (100 * values[0]) / amount;
            myProgress.setProgress(currprogress);
        }

        @Override
        protected ArrayList<String> doInBackground(Integer... myInt) {
            ArrayList<String> passwords = new ArrayList<>();
            Util myGenerator = new Util();
            for(int i = 0; i < amount; i++)
            {
                passwords.add(myGenerator.getPassword(length));
                publishProgress(i + 1);
            }
            return passwords;
        }
    }
}
