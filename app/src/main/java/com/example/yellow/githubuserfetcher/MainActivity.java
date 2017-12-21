package com.example.yellow.githubuserfetcher;

import android.database.Observable;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        initRecyclerView();

        searchEt=(EditText)findViewById(R.id.search_text_edit);
        progressBar=(ProgressBar)findViewById(R.id.loading_progressbar);

        addDefaultData();
    }

    public void fetchByKeyWord(View view){
        searchKey=searchEt.getText().toString();
        if(searchKey.equals("")){
            Toast.makeText(this,"Empty Key!",Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String urlStr="https://api.github.com/users/"+searchKey;
        String baseurl="http://api.github.com";

        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(baseurl)//要访问的网站
                .addConverterFactory(GsonConverterFactory.create())//使用的转换器
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//适配器
                .client(new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15,TimeUnit.SECONDS)
                    .writeTimeout(10,TimeUnit.SECONDS)
                    .build())
                .build();
        GithubInterface service=retrofit.create(GithubInterface.class);
        //retrofit2.Call<GitHubUser> modelA=service.getUser_Call(searchKey);
        rx.Observable<GitHubUser> modelB=service.getUser(searchKey);
        modelB.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GitHubUser>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this,"Fetch fail, Check your input or the internet",Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(GitHubUser gitHubUser) {
                        myAdapter.add(gitHubUser.getLogin(),gitHubUser.getId().toString(),gitHubUser.getBlog());
                        //progressBar.setVisibility(View.GONE);
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
        rx.Observable<GitHubUser> getUser(@Path("user") String user);
        retrofit2.Call<GitHubUser> getUser_Call(@Path("user") String user);
        rx.Subscriber<GitHubUser> getUser_sub(@Path("user") String user);
    }

    class GetThread extends Thread{

        public void run(){
            searchKey=searchEt.getText().toString();
            String urlStr="https://api.github.com/users/"+searchKey;

            String baseurl="http://api.github.com";
            Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl(baseurl)//要访问的网站
                    .addConverterFactory(GsonConverterFactory.create())//使用的转换器
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//适配器
                    .build();
            GithubInterface service=retrofit.create(GithubInterface.class);
            retrofit2.Call<GitHubUser> modelA=service.getUser_Call(searchKey);
            rx.Observable<GitHubUser> modelB=service.getUser(searchKey);
            modelA.enqueue(new Callback<GitHubUser>() {
                @Override
                public void onResponse(retrofit2.Call<GitHubUser> call, Response<GitHubUser> response) {
                    String login=response.body().getLogin();
                    String id=response.body().getId().toString();
                    String blog=response.body().getBlog();
                    addUserToList(login,id,blog);
                }

                @Override
                public void onFailure(retrofit2.Call<GitHubUser> call, Throwable t) {
                    Toast.makeText(MainActivity.this,"Fetch fail, Please check your keyword or the internet",Toast.LENGTH_SHORT).show();
                }
            });

        }



    }

    public void initRecyclerView(){
        recyclerView=(RecyclerView)findViewById(R.id.user_recyclerview);
        myAdapter=new MyAdapter();
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this,myAdapter.getName(position),Toast.LENGTH_SHORT).show();
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
        Toast.makeText(MainActivity.this,"CLEARED",Toast.LENGTH_SHORT).show();
    }

    public void addDefaultData(){
        myAdapter.add("ABCDEFG","1234567","www.myblog.com");
        myAdapter.add("HIJKLMN","8901213","www.github.com");
    }

}
