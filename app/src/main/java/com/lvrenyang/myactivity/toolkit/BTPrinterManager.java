package com.lvrenyang.myactivity.toolkit;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.lvrenyang.io.BTPrinting;
import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Pos;
import com.lvrenyang.myactivity.AppHolder;
import com.lvrenyang.myactivity.AppStart;
import com.lvrenyang.myactivity.Prints;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2020/3/19
 */
public class BTPrinterManager implements IOCallBack {

  // 打印机连接成功
  private final static int CONNECT_SUCCESS = 1;
  // 打印机连接失败
  private final static int CONNECT_FAILED = 0;
  // 打印机断开连接
  private final static int CONNECT_CLOSED = -1;
  // 打印成功
  private final static int PRINT_SUCCESS = 2;
  // 打印失败
  private final static int PRINT_FAILED = 3;

  // TAG
  private final static String TAG = BTPrinterManager.class.getSimpleName();
  // 蓝牙适配器
  private final static BluetoothAdapter ADAPTER = BluetoothAdapter.getDefaultAdapter();
  // 类目实例
  private volatile static BTPrinterManager INSTANCE = null;
  // 打印机各种指令工具类
  private final Pos mPos = new Pos();
  // 打印机工具类
  private final BTPrinting btPrinting = new BTPrinting();
  // handler
  @SuppressLint("HandlerLeak")
  private Handler handler = new Handler() {

    @Override
    public void handleMessage(@NonNull Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case CONNECT_SUCCESS:// 打印机连接成功
          toast("打印机连接成功");
          break;
        case CONNECT_FAILED:// 打印机连接失败
          toast("打印机连接失败");
          break;
        case CONNECT_CLOSED:// 打印机断开连接
          toast("打印机断开连接");
          break;
        case PRINT_SUCCESS:// 打印成功

          break;
        case PRINT_FAILED:// 打印失败

          break;
      }
    }
  };
  // 蓝牙监听广播
  private BroadcastReceiver broadcastReceiver;
  // 蓝牙设备列表
  private List<BluetoothDevice> devices = new ArrayList<>();
  // 暂存蓝牙地址
  private StringBuilder sb = new StringBuilder();
  // 线程连接池
  private ExecutorService executor = Executors.newScheduledThreadPool(30);

  /**
   * 私有构造器
   */
  private BTPrinterManager() {
    //no instance
    initBroadcast();
    mPos.Set(btPrinting);
    btPrinting.SetCallBack(this);
  }

  public static BTPrinterManager newInstance() {
    if (INSTANCE == null) {
      synchronized (BTPrinterManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new BTPrinterManager();
        }
      }
    }
    return INSTANCE;
  }

  private void toast(String msg) {
    Toast.makeText(AppHolder.newInstance(), msg, Toast.LENGTH_SHORT).show();
  }

  /**
   * 开始
   */
  public void start() {
    if (!ADAPTER.isEnabled()) {
      if (ADAPTER.enable()) {
        while (!ADAPTER.isEnabled())
          ;
      } else {
        toast("蓝牙打开失败");
        return;
      }
      // 判断蓝牙是否正在搜索,如果是 停止当前搜索
      if (ADAPTER.isDiscovering()) {
        ADAPTER.cancelDiscovery();
      }
      // 开始新的搜索
      ADAPTER.startDiscovery();
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
            if (device == null) {
              return;
            }
            onSearchResult(device);
            break;
          case BluetoothAdapter.ACTION_DISCOVERY_STARTED:// 开始搜索
            Log.d(TAG, "开始搜索");
            // 清空原设备列表
            devices.clear();
            break;
          case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:// 搜索结束
            Log.d(TAG, "搜索结束");
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
    AppHolder.newInstance().registerReceiver(broadcastReceiver, intentFilter);
  }

  /**
   * 连接蓝牙设备
   */
  public void choiceOnePrinter() {
    if (devices.isEmpty()) {
      toast("没有搜索到打印机设备");
      return;
    }
    // 判断是否已连接了打印机
    if (btPrinting.IsOpened()) {
      return;
    }
    // 弹出设备列表
    showDevicesWindow();
  }

  /**
   * showDevicesWindow
   */
  private void showDevicesWindow() {
    List<String> data = new ArrayList<>();
    for (BluetoothDevice d : devices) {
      data.add(d.getAddress());
    }
    if (data.isEmpty()) {
      return;
    }
    int size = data.size();
    final String[] items = data.toArray(new String[size]);
    if (items == null || items.length == 0) {
      return;
    }
    new AlertDialog.Builder(AppHolder.newInstance())
      .setTitle("【打印机列表】")
      .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

          // 连接打印机
          executor.submit(new TaskOpen(btPrinting, items[which], AppHolder.newInstance()));
        }
      }).show();
  }

  /**
   * 本地打印
   */
  public void printLocal() {
    executor.execute(new TaskPrint(mPos, handler));
  }

  /**
   * 断开打印机
   */
  public void close() {
    executor.execute(new TaskClose(btPrinting));
  }

  /**
   * 搜索到蓝牙设备
   *
   * @param device 蓝牙设备
   */
  private void onSearchResult(@NonNull BluetoothDevice device) {
    final String address = device.getAddress();
    String name = device.getName();
    // 过滤打印机设备
    if (name == null || !(name.toLowerCase()).contains("mpt")) {
      return;
    }
    String s = sb.toString();
    if (s.contains(address)) {
      return;
    }
    sb.append(address).append(",");
    devices.add(device);
  }

  /**
   * 取消广播
   */
  public void onDestroy() {
    if (broadcastReceiver != null) {
      AppHolder.newInstance().unregisterReceiver(broadcastReceiver);
    }
    if (handler != null) {
      handler.removeCallbacksAndMessages(null);
      handler = null;
    }
  }

  /**
   * 打印机连接成功
   */
  @Override
  public void OnOpen() {
    handler.sendEmptyMessage(CONNECT_SUCCESS);
  }

  /**
   * 打印机连接失败
   */
  @Override
  public void OnOpenFailed() {
    handler.sendEmptyMessage(CONNECT_FAILED);
  }

  /**
   * 打印机断开连接
   */
  @Override
  public void OnClose() {
    handler.sendEmptyMessage(CONNECT_CLOSED);
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
  public static class TaskPrint implements Runnable {

    private Pos pos;
    private Handler handler;

    TaskPrint(Pos pos, Handler handler) {
      this.pos = pos;
      this.handler = handler;
    }

    @Override
    public void run() {
      final boolean bPrintResult = Prints.PrintTicket(AppHolder.newInstance(), pos, AppStart.nPrintWidth, AppStart.bCutter, AppStart.bDrawer, AppStart.bBeeper, AppStart.nPrintCount, AppStart.nPrintContent, AppStart.nCompressMethod, AppStart.bCheckReturn);
      handler.sendEmptyMessage(bPrintResult ? PRINT_SUCCESS : PRINT_FAILED);
    }
  }
}
