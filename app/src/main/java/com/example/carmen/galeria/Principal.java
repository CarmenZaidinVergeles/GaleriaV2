package com.example.carmen.galeria;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Principal extends AppCompatActivity {
    private ImageView iv, ivCargar, ivGuardar, ivGirar, ivGris, ivEspejo;
    private Button bt;
    private Bitmap bitmap;
    public static final int REQUEST_IMAGE_GET = 1;
//***************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        iv = (ImageView) findViewById(R.id.imageView);
        ivCargar = (ImageView) findViewById(R.id.ivCargar);
        ivGuardar = (ImageView) findViewById(R.id.ivGuardar);
        ivGirar = (ImageView) findViewById(R.id.ivGirar);
        ivGris = (ImageView) findViewById(R.id.ivGris);
        ivEspejo = (ImageView) findViewById(R.id.ivEspejo);
        bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        init();
    }

    public void init() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        iv.setImageURI(uri);
    }
    //***************************************************************************************
    //PROGRAMAR BOTONES:

    //Boton cargar Imagen
    public void cargarFoto(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    //Boton Guardar foto (Se realiza con diálogo, llamada al método storeImage)
    public void guardar(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.tituloGuardarCambios);
        alert.setPositiveButton(R.string.positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                storeImage(bitmap, "foto");
                Toast.makeText(getApplicationContext(), "La foto ha sido guardada en myAppDir/myImages",
                        Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton(R.string.negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alert.show();
    }

    //Botón convertir a escala de grises
    public void convertirGris(View v) {
        BitmapDrawable bmpDraw = (BitmapDrawable) iv.getDrawable();
        Bitmap bitmap = bmpDraw.getBitmap();
        Bitmap bmpGris = toEscalaDeGris(bitmap);//Se pasa a gris
        iv.setImageBitmap(bmpGris);
    }

    //Botón para rotar la imagen
    public void girar(View v) {
        BitmapDrawable bmpDraw = (BitmapDrawable) iv.getDrawable();
        Bitmap bitmap = bmpDraw.getBitmap();
        iv.setImageBitmap(Principal.rotarBitmap(bitmap, 90));
    }

    //Botón convertir a sepia
    public void sepia(View v){
        bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        Bitmap bmpSepia = toSephia(bitmap);
        iv.setImageBitmap(bmpSepia);
    }

    //Metodo para hacer espejo
    public void espejo(View v) {
        bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        int pixel, red, green, blue, alpha;
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                pixel = bitmap.getPixel(i, j);
                red = Color.red(pixel);
                green = Color.green(pixel);
                blue = Color.blue(pixel);
                alpha = Color.alpha(pixel);
                bmp.setPixel(bitmap.getWidth() - i - 1, j, Color.argb(alpha, red, green, blue));
                //bitmap.getWidth() - i - 1 dar la vuelta img
            }
        }
        iv.setImageBitmap(bmp);
    }

    //Boton atrás
    public void atras(View v) {
        onBackPressed();
    }

    //*********************************************************************************************
    //METODOS
    //Metodo para almacenar la imagen
    private boolean storeImage(Bitmap imageData, String filename) {
        //Obtener la ruta a external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory() + "/myAppDir/myImages/";
        File sdIconStorageDir = new File(iconsStoragePath);

        //crear directorio si no existe
        sdIconStorageDir.mkdirs();

        try {
            String filePath = sdIconStorageDir.toString() + filename;
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //Compresión en formato jpeg
            imageData.compress(Bitmap.CompressFormat.JPEG, 10, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            //Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            //Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }
        return true;
    }

    //Metodo para convertir a escala de grises
    public static Bitmap toEscalaDeGris(Bitmap bmpOriginal) {
        Bitmap bmpGris = Bitmap.createBitmap(bmpOriginal.getWidth(),
                bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas lienzo = new Canvas(bmpGris);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(cmcf);
        lienzo.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGris;
    }
    //Metodo para rotar la imagen
    public static Bitmap rotarBitmap(Bitmap bmpOriginal, float angulo) {
        Matrix matriz = new Matrix();
        matriz.postRotate(angulo);
        return Bitmap.createBitmap(bmpOriginal, 0, 0,
                bmpOriginal.getWidth(), bmpOriginal.getHeight(), matriz, true);
    }

    //Metodo para convertir a Sepia
    public Bitmap toSephia(Bitmap bmpOriginal) {
        int width, height, r, g, b, c, gry;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        int depth = 20;

        Bitmap bmpSephia = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpSephia);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setScale(.3f, .3f, .3f, 1.0f);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        canvas.drawBitmap(bmpOriginal, 0, 0, paint);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = bmpOriginal.getPixel(x, y);

                r = Color.red(c);
                g = Color.green(c);
                b = Color.blue(c);

                gry = (r + g + b) / 3;
                r = g = b = gry;

                r = r + (depth * 2);
                g = g + depth;

                if (r > 255) {
                    r = 255;
                }
                if (g > 255) {
                    g = 255;
                }
                bmpSephia.setPixel(x, y, Color.rgb(r, g, b));
            }

        }
        return bmpSephia;
    }

    //Metodo para salir
    @Override
    public void onBackPressed() {
        super.onBackPressed();   }

}












