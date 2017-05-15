package com.ampm.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ampm.coolweather.R;
import com.ampm.coolweather.db.CoolWeatherDB;
import com.ampm.coolweather.model.City;
import com.ampm.coolweather.model.Country;
import com.ampm.coolweather.model.Province;
import com.ampm.coolweather.util.HttpCallbackListener;
import com.ampm.coolweather.util.HttpUtil;
import com.ampm.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/14.
 */

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_COUNTY = 2;
    public static final int LEVEL_CITY = 1;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private ListView list_view;
    private TextView title_text;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private Province selectedProvince;
    private int currentLevel;
    private City selectedCity;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chooes_area);
        list_view = (ListView) findViewById(R.id.list_view);
        title_text = (TextView) findViewById(R.id.title_text);
//        queryFromServer(null,"province");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        list_view.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
                queryProvinces();
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvince();
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            title_text.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");
        }
    }

    private void queryFromServer(String code, final String type) {
    String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void finish(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handlerProvincesResponse(coolWeatherDB,response);
                }else if("city".equals(type)){
                    result = Utility.handleCitiesRespones(coolWeatherDB,response,selectedProvince.getId());
                }else if("country".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDiallog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("country".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void error(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDiallog();
                           Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
    }

    private void closeProgressDiallog() {
        if(progressDialog != null){
          progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
    if(progressDialog == null){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(" 正在加载...");
        progressDialog.setCanceledOnTouchOutside(false);
    }
        progressDialog.show();
    }
    /*
* 查询市内的所有县
* */
    private void queryCounties() {
        countryList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countryList.size()>0){
            dataList.clear();
            for(Country country : countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            title_text.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
            queryFromServer(selectedCity.getCityName(),"country");
        }
    }
    /*
* 查询市内的所有市
* */
    private void queryCities() {
    cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            list_view.setSelection(0);
            title_text.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
}
