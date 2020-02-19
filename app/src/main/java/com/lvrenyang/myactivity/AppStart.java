package com.lvrenyang.myactivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.lvrenyang.sample1.R;

public class AppStart extends AppCompatActivity implements OnClickListener {

  public static int nPrintWidth = 384;
  public static boolean bCutter = false;
  public static boolean bDrawer = false;
  public static boolean bBeeper = true;
  public static int nPrintCount = 1;
  public static int nCompressMethod = 0;
  public static boolean bAutoPrint = false;
  public static int nPrintContent = 0;
  public static boolean bCheckReturn = false;

  private RadioButton
    radio58, radio80,
    radioPrintCount1, radioPrintCount10, radioPrintCount100, radioPrintCount1000,
    radioPrintContentS, radioPrintContentM, radioPrintContentL;
  private CheckBox chkCutter, chkDrawer, chkBeeper, chkPictureCompress, chkAutoPrint, chkCheckReturn;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_private);

    /* 启动WIFI */
    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
      wifiManager.setWifiEnabled(true);
    }

    /* 启动蓝牙 */
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (null != adapter) {
      if (!adapter.isEnabled()) {
        if (!adapter.enable()) {
          finish();
          return;
        }
      }
    }

    radio58 = findViewById(R.id.radioButtonTicket58);
    radio80 = findViewById(R.id.radioButtonTicket80);
    radioPrintCount1 = findViewById(R.id.radioButtonPrintCount1);
    radioPrintCount10 = findViewById(R.id.radioButtonPrintCount10);
    radioPrintCount100 = findViewById(R.id.radioButtonPrintCount100);
    radioPrintCount1000 = findViewById(R.id.radioButtonPrintCount1000);
    radioPrintContentS = findViewById(R.id.radioButtonPrintContentS);
    radioPrintContentM = findViewById(R.id.radioButtonPrintContentM);
    radioPrintContentL = findViewById(R.id.radioButtonPrintContentL);
    chkCutter = findViewById(R.id.checkBoxCutter);
    chkDrawer = findViewById(R.id.checkBoxDrawer);
    chkBeeper = findViewById(R.id.checkBoxBeeper);
    chkPictureCompress = findViewById(R.id.checkBoxPictureCompress);
    chkAutoPrint = findViewById(R.id.checkBoxAutoPrint);
    chkCheckReturn = findViewById(R.id.checkBoxCheckReturn);

    findViewById(R.id.btnTestBT).setOnClickListener(this);
    findViewById(R.id.btnTestBLE).setOnClickListener(this);
    findViewById(R.id.btnTestUSB).setOnClickListener(this);
    findViewById(R.id.btnTestNET).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (radio58.isChecked()) {
      nPrintWidth = 384;
    } else if (radio80.isChecked()) {
      nPrintWidth = 576;
    }

    if (radioPrintCount1.isChecked()) {
      nPrintCount = 1;
    } else if (radioPrintCount10.isChecked()) {
      nPrintCount = 10;
    } else if (radioPrintCount100.isChecked()) {
      nPrintCount = 100;
    } else if (radioPrintCount1000.isChecked()) {
      nPrintCount = 1000;
    }

    if (radioPrintContentS.isChecked()) {
      nPrintContent = 1;
    } else if (radioPrintContentM.isChecked()) {
      nPrintContent = 2;
    } else if (radioPrintContentL.isChecked()) {
      nPrintContent = 3;
    }

    bCutter = chkCutter.isChecked();
    bDrawer = chkDrawer.isChecked();
    bBeeper = chkBeeper.isChecked();

    nCompressMethod = chkPictureCompress.isChecked() ? 1 : 0;
    bAutoPrint = chkAutoPrint.isChecked();
    bCheckReturn = chkCheckReturn.isChecked();

    switch (v.getId()) {
      case R.id.btnTestBT:// 蓝牙2.0测试
      {
        Intent intent = new Intent(AppStart.this, SearchBTActivity.class);
        startActivity(intent);
      }
      break;
      case R.id.btnTestBLE:// 蓝牙4.0测试
      {
        Intent intent = new Intent(AppStart.this, SearchBLEActivity.class);
        startActivity(intent);
      }
      break;
      case R.id.btnTestUSB:// USB测试
      {
        Intent intent = new Intent(AppStart.this, ConnectUSBActivity.class);
        //Intent intent = new Intent(AppStart.this, ConnectCP2102Activity.class);
        startActivity(intent);
      }
      break;
      case R.id.btnTestNET:// 网络测试
      {
        Intent intent = new Intent(AppStart.this, ConnectIPActivity.class);
        startActivity(intent);
      }
      break;
    }
  }
}
