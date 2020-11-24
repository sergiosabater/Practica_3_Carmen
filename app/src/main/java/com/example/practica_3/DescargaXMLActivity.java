package com.example.practica_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DescargaXMLActivity extends AppCompatActivity {

    private Button btn_descarga_img;
    private ListView listViewPosts;
    private static final String URL_POST = "https://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";
    private ListViewAdapter adaptador;
    private List<Entrada> listaEntradas;
    private ProgressDialog myProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga_x_m_l);

        btn_descarga_img = findViewById(R.id.btn_descarga_img);
        listViewPosts = findViewById(R.id.listViewPosts);
        myProgressDialog = new ProgressDialog(DescargaXMLActivity.this);
        listaEntradas = new ArrayList<>();

        btn_descarga_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                doSomeTaskAsync();
            }
        });
    }


    // Uploads XML from stackoverflow.com, parses it, and returns ArrayList of Entrada.

    private static List<Entrada> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();
        List<Entrada> entradas = null;

        try {
            stream = downloadUrl(urlString);
            entradas = stackOverflowXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return entradas;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private static InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    public void doSomeTaskAsync() {
        HandlerThread ht = new HandlerThread("MyHandlerThread");
        ht.start();
        Handler asyncHandler = new Handler(ht.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Object response = msg.obj;
                doSomethingOnUi(response);
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                myProgressDialog.setTitle("Descarga de posts");
                myProgressDialog.setMessage("Procesando XML....");
                myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                myProgressDialog.show();

                try {
                    listaEntradas = loadXmlFromNetwork(URL_POST);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // create message and pass any object here doesn't matter
                // for a simple example I have used a simple string
                Message message = new Message();
                message.obj = "My Message!";
                asyncHandler.sendMessage(message);

            }
        };
        asyncHandler.post(runnable);
    }


    private void doSomethingOnUi(Object response) {
        Handler uiThread = new Handler(Looper.getMainLooper());
        uiThread.post(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(3000);
                        myProgressDialog.dismiss();
                        if (listaEntradas != null) {

                            adaptador = new ListViewAdapter(getApplicationContext(), listaEntradas);

                            listViewPosts.setAdapter(adaptador);

                            listViewPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String url = listaEntradas.get(position).getLink();
                                    Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();

                                    Uri webpage = Uri.parse(url);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    if (intent.resolveActivity(getApplication().getPackageManager()) != null) {
                                        getApplication().startActivity(intent);
                                    }

                                }
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "Se ha producido un error", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
    }

/*
    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    static class DownloadXmlTask extends AsyncTask<String, Void, List<Entrada>> {

        private List<Entrada> listaEntradas;
        private ListViewAdapter adaptador;
        private ListView listViewPosts;
        private final WeakReference<Context> context;
        private final ProgressDialog myProgressDialog;

        public DownloadXmlTask(Context context, List<Entrada> listaEntradas, ProgressDialog myProgressDialog, ListViewAdapter adaptador, ListView listViewPosts) {

            this.context = new WeakReference<>(context);
            this.listaEntradas = listaEntradas;
            this.myProgressDialog = myProgressDialog;
            this.adaptador = adaptador;
            this.listViewPosts = listViewPosts;
        }

        @Override
        protected void onPreExecute() {
            myProgressDialog.setTitle("Descarga de posts");
            myProgressDialog.setMessage("Procesando XML....");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.show();
        }

        @Override
        protected List<Entrada> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                Log.d("ERROR:", Objects.requireNonNull(e.getMessage()));
            } catch (XmlPullParserException e) {
                Log.d("ERROR:", Objects.requireNonNull(e.getMessage()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Entrada> resultado) {
            myProgressDialog.dismiss();
            if (resultado != null) {
                listaEntradas = resultado;

                adaptador = new ListViewAdapter(context.get(), listaEntradas);

                listViewPosts.setAdapter(adaptador);

                listViewPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String url = listaEntradas.get(position).getLink();
                        Toast.makeText(context.get(), url, Toast.LENGTH_SHORT).show();

                        Uri webpage = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if (intent.resolveActivity(context.get().getPackageManager()) != null) {
                            context.get().startActivity(intent);
                        }

                    }
                });

            } else {
                Toast.makeText(context.get(), "Se ha producido un error en onPostExecute", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/
}

