package com.lvrenyang.myactivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lvrenyang.io.BTPrinting;
import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Pos;
import com.lvrenyang.sample1.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 蓝牙2.0测试
 */
public class SearchBTActivity1 extends AppCompatActivity implements OnClickListener, IOCallBack {

  private LinearLayout linearlayoutdevices;
  private ProgressBar progressBarSearchStatus;

  private BroadcastReceiver broadcastReceiver;

  private Button btnSearch, btnDisconnect, btnPrint;
  private SearchBTActivity1 mActivity;

  private Pos mPos = new Pos();
  private BTPrinting btPrinting = new BTPrinting();
  private ExecutorService es = Executors.newScheduledThreadPool(30);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_searchbt1);

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

    mPos.Set(btPrinting);
    btPrinting.SetCallBack(this);

    initBroadcast();
  }

  @Override
  public void onClick(View arg0) {
    switch (arg0.getId()) {
      case R.id.buttonSearch:// 开始搜索蓝牙设备
      {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
          onBackPressed();
          break;
        }

        if (!adapter.isEnabled()) {
          if (adapter.enable()) {
            while (!adapter.isEnabled())
              ;
          } else {
            onBackPressed();
            return;
          }
        }

        adapter.cancelDiscovery();
        linearlayoutdevices.removeAllViews();
        adapter.startDiscovery();
      }
      break;
      case R.id.buttonDisconnect:// 断开蓝牙设备
        es.submit(new TaskClose(btPrinting));
        break;
      case R.id.buttonPrint:// 开始打印
        btnPrint.setEnabled(false);
        es.submit(new TaskPrint(mPos));
        break;
    }
  }

  private void initBroadcast() {
    broadcastReceiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (action == null) {
          return;
        }
        switch (action) {
          case BluetoothDevice.ACTION_FOUND:// 搜索到蓝牙设备
            Log.d("SearchBTActivity1", "搜索到了设备+++++++++++++++++++++++");
            if (device == null) {
              return;
            }
            onSearch(device);
            break;
          case BluetoothAdapter.ACTION_DISCOVERY_STARTED:// 开始搜索
            progressBarSearchStatus.setIndeterminate(true);
            Log.d("SearchBTActivity1", "开始搜索了");
            break;
          case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:// 搜索结束
            progressBarSearchStatus.setIndeterminate(false);
            Log.d("SearchBTActivity1", "搜索结束了");
            break;
          default:
            break;
        }
      }
    };
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    registerReceiver(broadcastReceiver, intentFilter);
  }

  /**
   * 搜索到蓝牙设备
   *
   * @param device 蓝牙设备
   */
  private void onSearch(@NonNull BluetoothDevice device) {
    final String address = device.getAddress();
    String name = device.getName();
    // 过滤打印机设备
    if (name == null || !(name.toLowerCase()).contains("mpt")) {
      return;
    }

    Button button = new Button(this);

//          BluetoothClass btClass = device.getBluetoothClass();
//          int nClass = 0;
//          if (btClass.hasService(Service.AUDIO))
//            nClass |= Service.AUDIO;
//          else if (btClass.hasService(Service.CAPTURE))
//            nClass |= Service.CAPTURE;
//          else if (btClass.hasService(Service.INFORMATION))
//            nClass |= Service.INFORMATION;
//          else if (btClass.hasService(Service.LIMITED_DISCOVERABILITY))
//            nClass |= Service.LIMITED_DISCOVERABILITY;
//          else if (btClass.hasService(Service.NETWORKING))
//            nClass |= Service.NETWORKING;
//          else if (btClass.hasService(Service.OBJECT_TRANSFER))
//            nClass |= Service.OBJECT_TRANSFER;
//          else if (btClass.hasService(Service.POSITIONING))
//            nClass |= Service.POSITIONING;
//          else if (btClass.hasService(Service.RENDER))
//            nClass |= Service.RENDER;
//          else if (btClass.hasService(Service.TELEPHONY))
//            nClass |= Service.TELEPHONY;
//
//          nClass |= btClass.getDeviceClass();
//
//          String strClass = String.format("%06X", nClass);

//          button.setText(name + ": " + address + "(" + strClass + ")");
    button.setText(String.format("%s: %s", name, address));

    for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
      Button btn = (Button) linearlayoutdevices.getChildAt(i);
      if (btn.getText().equals(button.getText())) {
        return;
      }
    }

    button.setGravity(Gravity.CENTER_VERTICAL);
    button.setOnClickListener(new OnClickListener() {

      public void onClick(View arg0) {
        Toast.makeText(mActivity, "正在连接...", Toast.LENGTH_SHORT).show();
        btnSearch.setEnabled(false);
        linearlayoutdevices.setEnabled(false);
        for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
          Button btn = (Button) linearlayoutdevices.getChildAt(i);
          btn.setEnabled(false);
        }
        btnDisconnect.setEnabled(false);
        btnPrint.setEnabled(false);
        es.submit(new TaskOpen(btPrinting, address, mActivity));
        //es.submit(new TaskTest(mPos, btPrinting, address, mActivity));
      }
    });
    button.getBackground().setAlpha(100);
    linearlayoutdevices.addView(button);
  }

  /**
   * 取消广播
   */
  private void unBindBroadcast() {
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
    }
  }

  /**
   * 打印机连接成功
   */
  @Override
  public void OnOpen() {
    runOnUiThread(new Runnable() {

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
        if (AppStart.bAutoPrint) {
          btnPrint.performClick();
        }
      }
    });
  }

  /**
   * 打印机连接失败
   */
  @Override
  public void OnOpenFailed() {
    runOnUiThread(new Runnable() {

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

  /**
   * 打印机断开连接
   */
  @Override
  public void OnClose() {
    runOnUiThread(new Runnable() {

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

  @Override
  protected void onDestroy() {
    // 解除蓝牙监听
    unBindBroadcast();
    // 断开蓝牙连接
    es.submit(new TaskClose(btPrinting));
    // 调用父类的页面销毁方法
    super.onDestroy();
    // 垃圾回收
    System.gc();
    // 运行任何未完成的对象的终结方法。
    System.runFinalization();
  }

  /**
   * 连接蓝牙打印机
   */
  public static class TaskOpen implements Runnable {

    private BTPrinting bt;
    private String address;
    private Context context;

    TaskOpen(BTPrinting bt, String address, Context context) {
      this.bt = bt;
      this.address = address;
      this.context = context;
    }

    @Override
    public void run() {
      bt.Open(address, context);
    }
  }

  /**
   * 断开蓝牙连接
   */
  public static class TaskClose implements Runnable {

    private BTPrinting bt;

    TaskClose(BTPrinting bt) {
      this.bt = bt;
    }

    @Override
    public void run() {
      bt.Close();
    }
  }

  /**
   * 开始打印
   */
  public class TaskPrint implements Runnable {

    private Pos pos;

    TaskPrint(Pos pos) {
      this.pos = pos;
    }

    @Override
    public void run() {
      final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), pos, AppStart.nPrintWidth, AppStart.bCutter, AppStart.bDrawer, AppStart.bBeeper, AppStart.nPrintCount, AppStart.nPrintContent, AppStart.nCompressMethod, AppStart.bCheckReturn);
      final boolean bIsOpened = pos.GetIO().IsOpened();

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(mActivity.getApplicationContext(), bPrintResult ? getResources().getString(R.string.printsuccess) : getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
          mActivity.btnPrint.setEnabled(bIsOpened);
        }
      });
    }
  }

//  public class TaskTest implements Runnable {
//    Pos pos = null;
//    BTPrinting bt = null;
//    String address = null;
//    Context context = null;
//
//    public TaskTest(Pos pos, BTPrinting bt, String address, Context context) {
//      this.pos = pos;
//      this.bt = bt;
//      this.address = address;
//      this.context = context;
//      pos.Set(bt);
//    }
//
//    @Override
//    public void run() {
//      for (int i = 0; i < 1000; ++i) {
//        for (int retry = 0; retry < 10; ++retry) {
//          if (Ticket(i))
//            break;
//          else
//            try {
//              Thread.sleep(1000);
//            } catch (InterruptedException e) {
//              e.printStackTrace();
//            }
//        }
//      }
//    }
//
//    private boolean Ticket(int i) {
//      boolean result = false;
//
//      long beginTime = System.currentTimeMillis();
//      if (bt.Open(address, context)) {
//        long endTime = System.currentTimeMillis();
//        if (pos.POS_RTQueryStatus(new byte[1], 1, 2000, 3)) {
//          pos.POS_S_Align(0);
//          pos.POS_S_TextOut(i + " " + "Open   UsedTime:" + (endTime - beginTime) + "\r\n", 0, 0, 0, 0, 0);
//          beginTime = System.currentTimeMillis();
//          result = pos.POS_RTQueryStatus(new byte[1], 1, 1000, 3);
//          endTime = System.currentTimeMillis();
//          pos.POS_S_TextOut(i + " " + "Ticket UsedTime:" + (endTime - beginTime) + " " + (result ? "Succeed" : "Failed") + "\r\n", 0, 0, 0, 0, 0);
//          result &= bt.IsOpened();
//        }
//        bt.Close();
//
//        try {
//          Thread.sleep(200);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
//
//      return result;
//    }
//  }
}
