package com.lvrenyang.myactivity;

import android.app.Application;

/**
 * Created by Administrator on 2020/3/19
 */
public class AppHolder extends Application {

  private static AppHolder INSTANCE;

  public static AppHolder newInstance() {
    if (INSTANCE == null) {
      INSTANCE = new AppHolder();
    }
    return INSTANCE;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    INSTANCE = this;
  }
}
