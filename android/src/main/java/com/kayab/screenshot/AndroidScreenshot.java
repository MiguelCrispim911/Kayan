package com.kayab.screenshot;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import java.io.File;

/** Implementación de screenshot para Android usando LibGDX. */
public class AndroidScreenshot implements IScreenshot {
    private final android.app.Activity activity;

    public AndroidScreenshot(android.app.Activity activity) {
        this.activity = activity;
    }

    @Override
    public void captureAndSave() {
        // Capturar el framebuffer en el siguiente frame
        Gdx.app.postRunnable(this::captureFramebuffer);
    }

    private void captureFramebuffer() {
        try {
            // Capturar los píxeles de la pantalla
            Pixmap pixmap = Pixmap.createFromFrameBuffer(
                0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );

            // Crear directorio Kayab en Pictures
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File kayabDir = new File(picturesDir, "Kayab");
            if (!kayabDir.exists()) {
                kayabDir.mkdirs();
            }

            // Generar nombre de archivo con timestamp
            long timestamp = System.currentTimeMillis();
            String filename = "kayab_" + timestamp + ".png";
            File screenshotFile = new File(kayabDir, filename);

            // Guardar Pixmap a PNG usando FileHandle de LibGDX
            FileHandle fh = new FileHandle(screenshotFile);
            PixmapIO.writePNG(fh, pixmap);
            pixmap.dispose();

            // Insertar la imagen en MediaStore para que la galería la reconozca y obtener un content Uri
            String imgUrl = null;
            try {
                imgUrl = MediaStore.Images.Media.insertImage(activity.getContentResolver(), screenshotFile.getAbsolutePath(), filename, "");
            } catch (Exception ex) {
                // ignore
            }

            Uri uriToOpen = null;
            if (imgUrl != null) {
                uriToOpen = Uri.parse(imgUrl);
            } else {
                // Fallback: usar Uri.fromFile (puede fallar en Android N+ si no hay FileProvider)
                uriToOpen = Uri.fromFile(screenshotFile);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriToOpen, "image/png");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(intent);

            Gdx.app.log("Screenshot", "Guardado en: " + screenshotFile.getAbsolutePath());
        } catch (Exception e) {
            Gdx.app.error("Screenshot", "Error al capturar: " + e.getMessage(), e);
        }
    }
}

