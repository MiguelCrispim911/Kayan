#!/usr/bin/env python3
"""Crear iconos adaptativos para Android API 26+"""

from PIL import Image, ImageDraw
import os

def create_adaptive_icon_layers(size, filename_foreground, filename_background):
    """Crear los dos archivos de capas para icono adaptativo"""
    # Crear imagen con fondo
    img_bg = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    img_fg = Image.new('RGBA', (size, size), (0, 0, 0, 0))

    draw_bg = ImageDraw.Draw(img_bg)
    draw_fg = ImageDraw.Draw(img_fg)

    # Colores dorados
    dark_gold = (128, 102, 0, 255)
    gold = (255, 214, 0, 255)
    light_gold = (255, 239, 102, 255)

    # FONDO (Background) - círculo dorado completo
    radius = size // 2
    margin = int(size * 0.05)
    draw_bg.ellipse(
        [margin, margin, size - margin, size - margin],
        fill=gold,
        outline=dark_gold,
        width=max(1, size // 48)
    )

    # FOREGROUND (Foreground) - triángulo/montaña en el centro
    # El foreground ocupa solo el 66% del tamaño del fondo para que no se corte
    fg_size = int(size * 0.66)
    offset = (size - fg_size) // 2

    points_mountain = [
        (size // 2, offset),  # arriba
        (size - offset, offset + fg_size),  # abajo derecha
        (offset, offset + fg_size)  # abajo izquierda
    ]
    draw_fg.polygon(points_mountain, fill=light_gold, outline=dark_gold, width=max(1, size // 48))

    # Círculo decorativo en el foreground (sol)
    circle_center = size // 2
    circle_radius = fg_size // 6
    draw_fg.ellipse(
        [circle_center - circle_radius, offset + fg_size // 3 - circle_radius,
         circle_center + circle_radius, offset + fg_size // 3 + circle_radius],
        fill=dark_gold,
        outline=gold,
        width=max(1, size // 64)
    )

    # Guardar
    img_bg.save(filename_background)
    img_fg.save(filename_foreground)
    print(f"✓ Capas adaptativas creadas ({size}x{size})")

# Tamaños para iconos adaptativos
ADAPTIVE_SIZE = 108  # 108x108 es el tamaño estándar para capas adaptativas

# Crear directorio
adaptive_dir = os.path.join(os.getcwd(), 'android', 'res', 'drawable-anydpi-v26')
os.makedirs(adaptive_dir, exist_ok=True)

# Crear capas adaptativas
create_adaptive_icon_layers(
    ADAPTIVE_SIZE,
    os.path.join(adaptive_dir, 'ic_launcher_foreground.png'),
    os.path.join(adaptive_dir, 'ic_launcher_background.png')
)

print("\n✨ ¡Iconos adaptativos (API 26+) creados!")

