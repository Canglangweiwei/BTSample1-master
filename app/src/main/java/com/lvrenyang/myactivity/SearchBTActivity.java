package com.lvrenyang.myactivity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.lvrenyang.myactivity.toolkit.BTPrinterManager;
import com.lvrenyang.sample1.R;

/**
 * 蓝牙2.0测试
 */
public class SearchBTActivity extends AppCompatActivity implements OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_searchbt);

    Button btnSearch = findViewById(R.id.btn_search);
    btnSearch.setOnClickListener(this);

    Button btnChoice = findViewById(R.id.btn_choice);
    btnChoice.setOnClickListener(this);

    Button btnDisconnect = findViewById(R.id.btn_disconnect);
    btnDisconnect.setOnClickListener(this);

    Button btnPrint = findViewById(R.id.btn_print);
    btnPrint.setOnClickListener(this);

    // 默认一开始就搜索蓝牙设备
    BTPrinterManager.newInstance().start();
  }

  @Override
  public void onClick(View arg0) {
    switch (arg0.getId()) {
      case R.id.btn_search:// 开始搜索蓝牙设备
      {
        BTPrinterManager.newInstance().start();
      }
      break;
      case R.id.btn_choice://选择打印机
      {
        BTPrinterManager.newInstance().choiceOnePrinter();
      }
      break;
      case R.id.btn_disconnect:// 断开蓝牙设备
      {
        BTPrinterManager.newInstance().close();
      }
      break;
      case R.id.btn_print:// 开始打印
      {
        BTPrinterManager.newInstance().printLocal();
      }
      break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    BTPrinterManager.newInstance().onDestroy();
  }
}
