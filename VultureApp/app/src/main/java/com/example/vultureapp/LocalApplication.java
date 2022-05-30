package com.example.vultureapp;

import android.app.Application;

import com.example.vultureapp.Threads.APIThread;

/**
 * Clase que hereda de la plase Application y controla los métodos del ciclo de vida de la aplicación.
 */
public class LocalApplication extends Application {

    //cuando se crea la apalicación
    @Override
    public void onCreate() {
        super.onCreate();

        //se inicia el hilo de conexiones de la API
        APIThread.getInstance().start();
    }

    //este método solo es para entornos de emulación, hay que  encontrar una alternativa
    @Override
    public void onTerminate() {
        super.onTerminate();

        //cuando termine tien que cerrar la conexión con el servidor
        APIThread.getInstance().closeConnection();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
