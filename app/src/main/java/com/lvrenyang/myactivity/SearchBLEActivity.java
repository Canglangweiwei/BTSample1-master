package com.lvrenyang.myactivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lvrenyang.io.BLEPrinting;
import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Pos;
import com.lvrenyang.sample1.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 蓝牙4.0
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SearchBLEActivity extends Activity implements OnClickListener, IOCallBack, LeScanCallback {

  private Button btnSearch, btnDisconnect, btnPrint;
  private SearchBLEActivity mActivity;
  private LinearLayout linearlayoutdevices;
  private ProgressBar progressBarSearchStatus;
  private Pos mPos = new Pos();
  private BLEPrinting mBt = new BLEPrinting();
  private ExecutorService es = Executors.newScheduledThreadPool(30);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_searchbt);

    mActivity = this;

    progressBarSearchStatus = findViewById(R.id.progressBarSearchStatus);
    linearlayoutdevices = findViewById(R.id.linearlayoutdevices);

    btnSearch = findViewById(R.id.buttonSearch);
    btnDisconnect = findViewById(R.id.buttonDisconnect);
    btnPrint = findViewById(R.id.buttonPrint);
    btnSearch.setOnClickListener(this);
    btnDisconnect.setOnClickListener(this);
    btnPrint.setOnClickListener(this);
    btnSearch.setEnabled(true);
    btnDisconnect.setEnabled(false);
    btnPrint.setEnabled(false);

    mPos.Set(mBt);
    mBt.SetCallBack(this);
  }

  @Override
  protected void onDestroy() {
    StopScan();
    btnDisconnect.performClick();
    super.onDestroy();
  }

  private void StopScan() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (null != adapter) {
      if (adapter.isEnabled()) {
        progressBarSearchStatus.setIndeterminate(false);
        adapter.stopLeScan(this);
      }
    }
  }

  private void StartScan() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (null != adapter) {
      if (adapter.isEnabled()) {
        linearlayoutdevices.removeAllViews();
        progressBarSearchStatus.setIndeterminate(true);
        adapter.startLeScan(this);
      }
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.buttonSearch: {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
          finish();
          break;
        }
        if (!adapter.isEnabled()) {
          if (adapter.enable()) {
            while (!adapter.isEnabled())
              ;
            Log.v("TAG", "Enable BluetoothAdapter");
          } else {
            finish();
            break;
          }
        }
        StartScan();
        break;
      }
      case R.id.buttonDisconnect:
        es.submit(new TaskClose(mBt));
        break;
      case R.id.buttonPrint:
        btnPrint.setEnabled(false);
        es.submit(new TaskPrint(mPos));
        break;
    }
  }

  @Override
  public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        if (device == null)
          return;
        final String address = device.getAddress();
        String name = device.getName();
        if (name == null)
          name = "BT";
        else if (name.equals(address))
          name = "BT";
        Button button = new Button(mActivity);
        button.setText(String.format("%s: %s", name, address));

        for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
          Button btn = (Button) linearlayoutdevices.getChildAt(i);
          if (btn.getText().equals(button.getText())) {
            return;
          }
        }

        button.setGravity(android.view.Gravity.CENTER_VERTICAL);
        button.setOnClickListener(new OnClickListener() {

          public void onClick(View arg0) {
            Toast.makeText(mActivity, "Connecting...", Toast.LENGTH_SHORT).show();
            btnSearch.setEnabled(false);
            linearlayoutdevices.setEnabled(false);
            for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
              Button btn = (Button) linearlayoutdevices.getChildAt(i);
              btn.setEnabled(false);
            }
            btnDisconnect.setEnabled(false);
            btnPrint.setEnabled(false);
            mActivity.StopScan();
            es.submit(new TaskOpen(mBt, address, mActivity));
            //es.submit(new TaskTest(mPos, mBt, address, mActivity));
          }
        });
        button.getBackground().setAlpha(100);
        linearlayoutdevices.addView(button);
      }
    });
  }

  @Override
  public void OnOpen() {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        btnDisconnect.setEnabled(true);
        btnPrint.setEnabled(true);
        btnSearch.setEnabled(false);
        linearlayoutdevices.setEnabled(false);
        for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
          Button btn = (Button) linearlayoutdevices.getChildAt(i);
          btn.setEnabled(false);
        }
        Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void OnOpenFailed() {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        btnDisconnect.setEnabled(false);
        btnPrint.setEnabled(false);
        btnSearch.setEnabled(true);
        linearlayoutdevices.setEnabled(true);
        for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
          Button btn = (Button) linearlayoutdevices.getChildAt(i);
          btn.setEnabled(true);
        }
        Toast.makeText(mActivity, "Failed", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void OnClose() {
    this.runOnUiThread(new Runnable() {

      @Override
      public void run() {
        btnDisconnect.setEnabled(false);
        btnPrint.setEnabled(false);
        btnSearch.setEnabled(true);
        linearlayoutdevices.setEnabled(true);
        for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
          Button btn = (Button) linearlayoutdevices.getChildAt(i);
          btn.setEnabled(true);
        }
      }
    });
  }

  public static class TaskOpen implements Runnable {
    BLEPrinting bt;
    String address;
    Context context;

    TaskOpen(BLEPrinting bt, String address, Context context) {
      this.bt = bt;
      this.address = address;
      this.context = context;
    }

    @Override
    public void run() {
      bt.Open(address, context);
    }
  }

  public static class TaskClose implements Runnable {

    private BLEPrinting bt;

    TaskClose(BLEPrinting bt) {
      this.bt = bt;
    }

    @Override
    public void run() {
      bt.Close();
    }
  }

  public class TaskTest implements Runnable {
    Pos pos;
    BLEPrinting bt;
    String address;
    Context context;

    public TaskTest(Pos pos, BLEPrinting bt, String address, Context context) {
      this.pos = pos;
      this.bt = bt;
      this.address = address;
      this.context = context;
      pos.Set(bt);
    }

    @Override
    public void run() {
      for (int i = 0; i < 1000; ++i) {
        long beginTime = System.currentTimeMillis();
        if (bt.Open(address, context)) {
          long endTime = System.currentTimeMillis();
          pos.POS_S_Align(0);
          pos.POS_S_TextOut(i + " " + "Open   UsedTime:" + (endTime - beginTime) + "\r\n", 0, 0, 0, 0, 0);
          beginTime = System.currentTimeMillis();
          boolean ticketResult = pos.POS_TicketSucceed(i, 30000);
          endTime = System.currentTimeMillis();
          pos.POS_S_TextOut(i + " " + "Ticket UsedTime:" + (endTime - beginTime) + " " + (ticketResult ? "Succeed" : "Failed") + "\r\n", 0, 0, 0, 0, 0);
          pos.POS_Beep(1, 500);
          byte[] status = new byte[1];
          pos.POS_QueryStatus(status, 3000, 2);
          bt.Close();
        }
      }
    }
  }

  public class TaskPrint implements Runnable {

    private Pos pos;

    TaskPrint(Pos pos) {
      this.pos = pos;
    }

    @Override
    public void run() {
      final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), pos, AppStart.nPrintWidth, AppStart.bCutter, AppStart.bDrawer, AppStart.bBeeper, AppStart.nPrintCount, AppStart.nPrintContent, AppStart.nCompressMethod, AppStart.bCheckReturn);
      final boolean bIsOpened = pos.GetIO().IsOpened();

      mActivity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(mActivity.getApplicationContext(), bPrintResult ? getResources().getString(R.string.printsuccess) : getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
          mActivity.btnPrint.setEnabled(bIsOpened);
        }
      });
    }
  }
}
