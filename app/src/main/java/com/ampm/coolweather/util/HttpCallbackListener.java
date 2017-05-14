package com.ampm.coolweather.util;

/**
 * Created by Administrator on 2017/5/14.
 */

public interface HttpCallbackListener {
    void finish(String response);
    void error(Exception e);
}
