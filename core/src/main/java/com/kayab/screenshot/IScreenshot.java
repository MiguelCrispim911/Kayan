package com.kayab.screenshot;

/** Interfaz para capturar screenshot (implementada en Android). */
public interface IScreenshot {
    /**
     * Capturar la pantalla actual y guardar en galería.
     * Abre automáticamente con Intent ACTION_VIEW.
     */
    void captureAndSave();
}

