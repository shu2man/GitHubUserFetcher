package com.example.yellow.githubuserfetcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yellow on 2017-12-22.
 */

public class RepositoriesActivity extends Activity {
    private String name;
    private ProgressBar progressBar;

    private List<Map<String,Object>> datas;
    private ArrayList<GitHubRepository> DataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respositories);
        name=getIntent().getStringExtra("name");
        //Toast.makeText(this,name,Toast.LENGTH_SHORT).show();

        datas=new ArrayList<>();
        getRepositories();//先获取数据
        //initListView();//再显示数据
    }

    public void initListView(){
        ListView lv;
        lv=findViewById(R.id.respositories_list);
        if(datas!=null) Toast.makeText(this,datas.size()+"",Toast.LENGTH_SHORT).show();
        SimpleAdapter adapter=new SimpleAdapter(this,datas,R.layout.list_item,
                new String[]{"name","language","description"},
                new int[]{R.id.repository_name,R.id.language_type,R.id.brief_information});
        lv.setAdapter(adapter);
    }

    public void backToMain(View view){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    public interface GithubRepos{
        @GET("repos")
        rx.Observable<ArrayList<GitHubRepository>> getRepos();//使用RxJava返回类型为observable
    }
    public void getRepositories(){
        progressBar=findViewById(R.id.respositories_progressbar);
        String rpsurl="https://api.github.com/users/shu2man/repos";
        String baseurl="https://api.github.com/users/"+name+"/";

        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(baseurl)//要访问的网站
                .addConverterFactory(GsonConverterFactory.create())//使用的转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//适配器
                .client(new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(20,TimeUnit.SECONDS)
                        .writeTimeout(10,TimeUnit.SECONDS)
                        .build())
                .build();
        GithubRepos service=retrofit.create(GithubRepos.class);
        service.getRepos()
                .subscribeOn(Schedulers.io())//Schedulers.newThread()新线程请求,Schedulers.io()io线程
                .observeOn(AndroidSchedulers.mainThread())//回调在主线程
                .subscribe(new Subscriber<ArrayList<GitHubRepository>>() {
                    @Override
                    public void onCompleted() {
                        //Toast.makeText(RepositoriesActivity.this,"Done",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(RepositoriesActivity.this,"Fail, Check your input or the internet",Toast.LENGTH_SHORT).show();//
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(ArrayList<GitHubRepository> reposList) {
                        //if(reposList.size()==0) Toast.makeText(RepositoriesActivity.this,"The User Has No Repository!",Toast.LENGTH_SHORT).show();
                        setListData(reposList);
                    }
                });
    }
    public void setListData(ArrayList<GitHubRepository> reposList){
        if(reposList.size()==0){
            Map<String,Object> item=new HashMap<>();
            item.put("name","        No Repository To Show");
            item.put("language","");
            item.put("description","");
            datas.add(item);
            return;
        }
        String n="";
        for(int i=0;i<reposList.size();i++){
            Map<String,Object> item=new HashMap<>();
            item.put("name",reposList.get(i).getName());
            item.put("language",reposList.get(i).getLanguage());
            if(reposList.get(i).getDescription().length()>31){
                item.put("description",reposList.get(i).getDescription().substring(0,30)+"...");
            }
            else item.put("description",reposList.get(i).getDescription());
            datas.add(item);
            n+=reposList.get(i).getName();
        }
        initListView();//再显示数据
    }


}
