package com.kayab;

import com.badlogic.gdx.graphics.Color;
import com.kayab.CinematicScreen.Scene;

/**
 * US16 — Guion de las cinemáticas (sin JSON, solo arreglos de Scene en Java),
 * basado en la historia del GDD: Kayab, los Chimalpa, la alianza y la traición.
 */
public final class Cinematics {

    private static final Color KAYAB    = new Color(0.50f, 0.65f, 1.00f, 1f);
    private static final Color CITLALI  = new Color(0.95f, 0.80f, 0.50f, 1f);
    private static final Color VARGAS   = new Color(0.72f, 0.77f, 0.84f, 1f);
    private static final Color MALINALLI= new Color(0.82f, 0.62f, 0.92f, 1f);
    private static final Color XOCOTL   = new Color(0.95f, 0.45f, 0.40f, 1f);
    private static final Color NARR     = new Color(0.85f, 0.85f, 0.85f, 1f);

    private Cinematics() {}

    public static Scene[] intro() {
        return new Scene[]{
            new Scene(1, "", NARR, "Pueblo Chimalpa. Tierra de trueno y de maíz."),
            new Scene(1, "Citlali", CITLALI, "Ten cuidado con lo que invitas, nieto."),
            new Scene(1, "Kayab", KAYAB, "Los aztecas vuelven cada luna por nuestro grano. Esta vez los detendré yo mismo.")
        };
    }

    public static Scene[] opening(int world) {
        switch (world) {
            case 1: return new Scene[]{
                new Scene(1, "", NARR, "Mundo 1 — La Tierra del Trueno"),
                new Scene(1, "Kayab", KAYAB, "Solo con mi arco. Como siempre.")
            };
            case 2: return new Scene[]{
                new Scene(2, "", NARR, "Mundo 2 — El Pacto de Hierro"),
                new Scene(2, "Vargas", VARGAS, "Pelea a nuestro lado, Kayab. España premia a sus amigos."),
                new Scene(2, "Malinalli", MALINALLI, "Yo también le creí a alguien así, una vez.")
            };
            case 3: return new Scene[]{
                new Scene(3, "", NARR, "Mundo 3 — El Corazón del Imperio"),
                new Scene(3, "Kayab", KAYAB, "Tenochtitlan arde. Hoy cae Xócotl, el gran capitán azteca.")
            };
            case 4: return new Scene[]{
                new Scene(4, "", NARR, "Mundo 4 — La Traición"),
                new Scene(4, "Kayab", KAYAB, "¿Cruces donde estaban nuestros templos? ¿Qué han hecho?"),
                new Scene(4, "Vargas", VARGAS, "Tu gente trabajará para la Corona. Así funciona la encomienda.")
            };
            default: return new Scene[]{ new Scene(world, "", NARR, "Mundo " + world) };
        }
    }

    public static Scene[] closing(int world) {
        switch (world) {
            case 1: return new Scene[]{
                new Scene(1, "Kayab", KAYAB, "Los rechacé... pero seguirán viniendo. Solo no basta."),
                new Scene(1, "Citlali", CITLALI, "Un guerrero sabio busca aliados. Reza por que sean los correctos.")
            };
            case 2: return new Scene[]{
                new Scene(2, "Kayab", KAYAB, "Su acero es fuerte. Pero algo en sus ojos no me deja dormir.")
            };
            case 3: return new Scene[]{
                new Scene(3, "Kayab", KAYAB, "Xócotl ha caído. Ganamos la guerra... ¿verdad?"),
                new Scene(3, "Xócotl", XOCOTL, "Mira bien a tus aliados, joven. Pronto lo entenderás.")
            };
            default: return new Scene[]{ new Scene(world, "", NARR, "...") };
        }
    }

    /** Final + epílogo (tras derrotar a Vargas). */
    public static Scene[] ending() {
        return new Scene[]{
            new Scene(4, "Vargas", VARGAS, "(Vencido) Hazlo. Mátame."),
            new Scene(4, "Kayab", KAYAB, "Gané todas las batallas pero mi pueblo salió perdiendo."),
            new Scene(0, "", NARR, "Kayab y su pueblo resistieron cuarenta años más. No aparecen en los libros españoles. Pero los Chimalpa los recuerdan."),
            new Scene(0, "", NARR, "Esta historia está basada en hechos reales. Los Tlaxcaltecas y miles de pueblos indígenas vivieron exactamente esto.")
        };
    }
}
