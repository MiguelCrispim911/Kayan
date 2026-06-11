#!/usr/bin/env python3
"""Crear iconos de la aplicación Kayab en diferentes tamaños"""

from PIL import Image, ImageDraw
import os

# Dimensiones necesarias para Android
SIZES = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

def create_icon(size, filename):
    """Crear un icono dorado con geometría interesante"""
    # Crear imagen con fondo
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Colores dorados (como el botón de screenshot)
    dark_gold = (128, 102, 0, 255)
    gold = (255, 214, 0, 255)
    light_gold = (255, 239, 102, 255)

    # Dibujar un triángulo (representa una montaña o pirámide - tema Kayab)
    margin = size // 10
    points_mountain = [
        (size // 2, margin),  # arriba
        (size - margin, size - margin),  # abajo derecha
        (margin, size - margin)  # abajo izquierda
    ]
    draw.polygon(points_mountain, fill=gold, outline=dark_gold, width=max(1, size // 48))

    # Dibujar segunda capa (decorativa)
    inner_margin = size // 4
    inner_size = size - (inner_margin * 2)

    # Círculo dorado claro en el centro (sol/joya)
    circle_center = size // 2
    circle_radius = inner_size // 5
    draw.ellipse(
        [circle_center - circle_radius, circle_center - circle_radius,
         circle_center + circle_radius, circle_center + circle_radius],
        fill=light_gold,
        outline=dark_gold,
        width=max(1, size // 64)
    )

    # Guardar
    img.save(filename)
    print(f"✓ Creado: {filename} ({size}x{size})")

# Crear directorio de trabajo
work_dir = os.path.dirname(os.path.abspath(__file__))

# Crear iconos para cada tamaño
for dpi, size in SIZES.items():
    drawable_dir = os.path.join(work_dir, 'android', 'res', f'drawable-{dpi}')
    os.makedirs(drawable_dir, exist_ok=True)

    filename = os.path.join(drawable_dir, 'ic_launcher.png')
    create_icon(size, filename)

print("\n✨ ¡Iconos creados exitosamente en todas las resoluciones!")
print("Los iconos están listos en:")
print("  - android/res/drawable-mdpi/ic_launcher.png")
print("  - android/res/drawable-hdpi/ic_launcher.png")
print("  - android/res/drawable-xhdpi/ic_launcher.png")
print("  - android/res/drawable-xxhdpi/ic_launcher.png")
print("  - android/res/drawable-xxxhdpi/ic_launcher.png")

