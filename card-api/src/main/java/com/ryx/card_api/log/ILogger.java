package com.ryx.card_api.log;


/**
 * Created by hezhiqiang on 2018/11/20.
 */

public interface ILogger {

    boolean isShowLog = false;
    boolean isShowStackTrace = false;
    String defaultTag = "card";

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void debug(String tag, String message);

    void info(String tag, String message);

    void warning(String tag, String message);

    void error(String tag, String message);

    void monitor(String message);

    boolean isMonitorMode();

    String getDefaultTag();
}
