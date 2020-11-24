package com.example.practica_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;

public class DescargaImagenActivity extends AppCompatActivity {

    private Button btn_descarga_img;
    private ProgressDialog myProgressDialog;
    private ImageView iv_descarga;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private final static String CHANNEL_ID = "NOTIFICACION";
    private final static int NOTIFICACION_ID = 0;
    private final String URL = "https://upload.wikimedia.org/wikipedia/en/b/bd/Doraemon_character.png";
    private Bitmap bitmap;
    private HandlerThread ht = new HandlerThread("MyHandlerThread");
    private Handler handler;
    boolean valido = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga_imagen);

        btn_descarga_img = findViewById(R.id.btn_descarga_img);
        iv_descarga = findViewById(R.id.iv_descarga);
        iv_descarga.setImageDrawable(null);
        myProgressDialog = new ProgressDialog(DescargaImagenActivity.this);
        bitmap = null;

        ht.start();
        handler = new Handler(ht.getLooper());

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        solicitarPermisos();

        btn_descarga_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(DescargaImagenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    doSomeTaskAsync();

                } else {
                    ActivityCompat.requestPermissions(DescargaImagenActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_CODE);
                }

            }
        });
    }

    public void solicitarPermisos() {

        int permiso = ActivityCompat.checkSelfPermission(DescargaImagenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permiso != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    Toast.makeText(getApplicationContext(), "Debe activar permiso de escritura", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void mostrarNotificacion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificacion";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_baseline_notifications_active_24);
        builder.setContentTitle("Notificación");
        builder.setContentText("Imagen guardada en dispositivo");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setDefaults(Notification.DEFAULT_SOUND);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(NOTIFICACION_ID, builder.build());
    }


    public static Bitmap getBitmapFromURL(String url_image) {
        try {
            URL url = new URL(url_image);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }


    public void guardarImagen(Context context, String URL) {
        try {
            // externalStorage
            String ExternalStorageDirectory = Environment.getExternalStorageDirectory() + File.separator;

            //carpeta "imagenesguardadas"
            String rutacarpeta = "imagenesguardadas/";

            // nombre del nuevo png
            String nombre = "doraemon.png";

            // Compruebas si existe la carpeta "imagenesguardadas", sino, la crea
            File directorioImagenes = new File(ExternalStorageDirectory + rutacarpeta);

            directorioImagenes.mkdir();

            // le pasas al bitmap la imagen de la URL
            Bitmap bitmap = getBitmapFromURL(URL);


            // pones las medidas que quieras del nuevo .png
            int bitmapWidth = bitmap.getWidth(); // para utilizar width de la imagen original: bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight(); // para utilizar height de la imagen original: bitmap.getHeight();
            Bitmap bitmapout = Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false);


            //creas el nuevo png en la nueva ruta
            bitmapout.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(ExternalStorageDirectory + rutacarpeta + nombre));

            // le pones parametros necesarios a la imagen para que se muestre en cualquier galería
            File filefinal = new File(ExternalStorageDirectory + rutacarpeta + nombre);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Doraemon");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Imagen de Doraemon");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.ImageColumns.BUCKET_ID, filefinal.toString().toLowerCase(Locale.getDefault()).hashCode());
                values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, filefinal.getName().toLowerCase(Locale.getDefault()));
            }
            values.put("_data", filefinal.getAbsolutePath());
            ContentResolver cr = getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR:", "Se ha producido un error");
        }
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
                myProgressDialog.setTitle("Descarga de imagen");
                myProgressDialog.setMessage("Descargando....");
                myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                myProgressDialog.show();

                try {
                    InputStream in = new java.net.URL(URL).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.d("ERROR:", Objects.requireNonNull(e.getMessage()));
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

                runOnUiThread(new Thread(new Runnable() {
                    public void run() {
                        if (bitmap != null) {
                            iv_descarga.setImageBitmap(bitmap);
                            guardarImagen(getApplicationContext(), URL);
                            mostrarNotificacion();
                        } else {
                            Toast.makeText(getApplicationContext(), "Se ha producido un error", Toast.LENGTH_SHORT).show();
                        }

                    }
                }));

            }
        });
    }


    /*static class DescargarImagen extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageView;
        private final WeakReference<Context> context;
        private final ProgressDialog myProgressDialog;

        public DescargarImagen(Context context, ImageView imagen, ProgressDialog myProgressDialog) {

            imageView = new WeakReference<>(imagen);
            this.context = new WeakReference<>(context);
            this.myProgressDialog = myProgressDialog;
        }

        @Override
        protected void onPreExecute() {
            myProgressDialog.setTitle("Descarga de imagen");
            myProgressDialog.setMessage("Descargando....");
            myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            myProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String URL = urls[0];
            Bitmap bitmap = null;

            try {
                InputStream in = new java.net.URL(URL).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d("ERROR:", Objects.requireNonNull(e.getMessage()));
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap resultado) {


            myProgressDialog.dismiss();
            if (resultado != null) {
                imageView.get().setImageBitmap(resultado);
            } else {
                Toast.makeText(context.get(), "Error en descarga de la imagen", Toast.LENGTH_SHORT).show();
            }

        }
    }*/

}