package com.voidarchives.util;

import android.app.Activity;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.voidarchives.R;

public class WindowInsetsHelper {

    /**
     * Aplica márgenes seguros (Safe Area) en todas las pantallas para evitar que el contenido
     * o los botones superiores queden debajo de la barra de estado, cámara frontal (notch)
     * o barra de navegación inferior en Android 15+ (Edge-to-Edge).
     */
    public static void aplicarMargenesSeguros(Activity activity) {
        if (activity == null || activity.getWindow() == null) return;

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        if (controller != null) {
            // Iconos blancos en la barra de notificaciones y de navegación para fondo oscuro
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        View contentView = activity.findViewById(android.R.id.content);
        if (contentView == null) return;

        contentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.fondo_lateral));

        ViewCompat.setOnApplyWindowInsetsListener(contentView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            int bottomInset = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset);

            return WindowInsetsCompat.CONSUMED;
        });
    }
}
