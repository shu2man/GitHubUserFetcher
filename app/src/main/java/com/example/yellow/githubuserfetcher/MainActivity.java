package com.example.yellow.githubuserfetcher;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Observable;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.internal.http.HttpConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.R.attr.path;

public class MainActivity extends AppCompatActivity {
    private EditText searchEt;
    private String searchKey;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyPermission(this);

        initRecyclerView();

        searchEt=(EditText)findViewById(R.id.search_text_edit);
        progressBar=(ProgressBar)findViewById(R.id.loading_progressbar);

        //addDefaultData();
    }

    public void fetchByKeyWord(View view){
        searchKey=searchEt.getText().toString();
        hideKeyBoard();
        if(searchKey.equals("")){
            Toast.makeText(this,"Empty Key!",Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String urlStr="https://api.github.com/users/"+searchKey;
        String baseurl="https://api.github.com";

        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(baseurl)//要访问的网站，baseUrl: https://api.github.com
                .addConverterFactory(GsonConverterFactory.create())//使用的转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//适配器
                .client(new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(20,TimeUnit.SECONDS)
                        .writeTimeout(10,TimeUnit.SECONDS)
                        .build())
                .build();
        //retrofit2.Call<GitHubUser> modelA=service.getUser_Call(searchKey);
        //这种先获取Model再操作的方式无法请求到结果
/*        rx.Observable<GitHubUser> modelB=service.getUser(searchKey);
        modelB.subscribeOn(Schedulers.io())//Schedulers.newThread()新线程请求,Schedulers.io()io线程*/
        GithubInterface service=retrofit.create(GithubInterface.class);
        service.getUser(searchKey)
                .subscribeOn(Schedulers.io())//Schedulers.newThread()新线程请求,Schedulers.io()io线程
                .observeOn(AndroidSchedulers.mainThread())//回调在主线程
                .subscribe(new Subscriber<GitHubUser>() {
                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this,"Fail, Check your input or the internet",Toast.LENGTH_SHORT).show();//
                        progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onNext(GitHubUser gitHubUser) {
                        String l=gitHubUser.getLogin();
                        String i=gitHubUser.getId().toString();
                        String b=gitHubUser.getBlog();
                        myAdapter.add(l,i,b);
                    }
                });
/*        modelA.enqueue(new Callback<GitHubUser>() {
            @Override
            public void onResponse(retrofit2.Call<GitHubUser> call, Response<GitHubUser> response) {
                String login=response.body().getLogin();
                String id=response.body().getId().toString();
                String blog=response.body().getBlog();
                addUserToList(login,id,blog);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(retrofit2.Call<GitHubUser> call, Throwable t) {
                Toast.makeText(MainActivity.this,"Fetch fail,check your input or the internet",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });*/
    }


    public interface GithubInterface{
        @GET("/users/{user}")
            //使用RxJava返回类型为observable
        rx.Observable<GitHubUser> getUser(@Path("user") String user);
    }


    public void initRecyclerView(){
        recyclerView=(RecyclerView)findViewById(R.id.user_recyclerview);
        myAdapter=new MyAdapter();
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,myAdapter.getName(position),Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MainActivity.this,RepositoriesActivity.class);
                intent.putExtra("name",myAdapter.getName(position));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                myAdapter.remove(position);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
    public void addUserToList(String login,String id,String blog){
        myAdapter.add(login,id,blog);

    }
    public void clearList(View view){
        myAdapter.clear();
        searchEt.setText("");
        Toast.makeText(MainActivity.this,"CLEARED",Toast.LENGTH_SHORT).show();
    }

    public void addDefaultData(){
        myAdapter.add("ABCDEFG","1234567","www.myblog.com");
        myAdapter.add("HIJKLMN","8901213","www.github.com");
    }
    public void hideKeyBoard(){
        View view=getCurrentFocus();
        if(view!=null){
            ((InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private static String[] PERMISSION_INTERNET={Manifest.permission.INTERNET};
    public void verifyPermission(Activity activity){
        try{
            int permission= ActivityCompat.checkSelfPermission(activity,"android.permission.INTERNET");
            if(permission!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,PERMISSION_INTERNET,1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permission[],int[] grantResults){
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            //permission granted
        }
        else{
            //permission not set
        }
    }

}
