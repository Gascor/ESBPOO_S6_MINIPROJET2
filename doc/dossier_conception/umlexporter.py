#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import math
import os
import re
import sys
import xml.etree.ElementTree as ET

import requests
import cairosvg
from PIL import Image


def parse_length(value):
    """
    Extrait la partie numérique d'une longueur SVG, ex:
    '1200px' -> 1200.0
    '100%'   -> 100.0 (à éviter si possible)
    """
    if value is None:
        return None
    m = re.match(r"^\s*([+-]?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)", str(value))
    return float(m.group(1)) if m else None


def get_svg_viewbox(root):
    """
    Retourne (x, y, width, height) à partir du viewBox,
    ou à défaut de width/height.
    """
    view_box = root.get("viewBox")
    if view_box:
        parts = re.split(r"[,\s]+", view_box.strip())
        parts = [p for p in parts if p]
        if len(parts) == 4:
            x, y, w, h = map(float, parts)
            return x, y, w, h

    width = parse_length(root.get("width"))
    height = parse_length(root.get("height"))
    if width is not None and height is not None:
        return 0.0, 0.0, width, height

    raise ValueError(
        "Impossible de déterminer la taille du SVG "
        "(ni viewBox, ni width/height exploitables)."
    )


def download_svg(url):
    headers = {
        "User-Agent": "Mozilla/5.0",
        "Accept": "image/svg+xml,text/xml,application/xml;q=0.9,*/*;q=0.8",
    }
    r = requests.get(url, headers=headers, timeout=60)
    r.raise_for_status()

    content_type = r.headers.get("Content-Type", "")
    if "svg" not in content_type and b"<svg" not in r.content[:5000]:
        raise ValueError(
            f"L'URL ne semble pas retourner un SVG. Content-Type reçu: {content_type}"
        )
    return r.content


def render_tile(svg_bytes, tile_viewbox, output_width, output_height, out_path):
    """
    Rend une tuile en modifiant le viewBox du SVG.
    """
    root = ET.fromstring(svg_bytes)

    x, y, w, h = tile_viewbox
    root.set("viewBox", f"{x} {y} {w} {h}")
    root.set("width", str(output_width))
    root.set("height", str(output_height))

    tile_svg = ET.tostring(root, encoding="utf-8", xml_declaration=True)

    cairosvg.svg2png(
        bytestring=tile_svg,
        write_to=out_path,
        output_width=output_width,
        output_height=output_height,
    )


def split_and_merge(svg_url, parts, zoom, output_png, tiles_dir):
    svg_bytes = download_svg(svg_url)
    root = ET.fromstring(svg_bytes)

    vb_x, vb_y, vb_w, vb_h = get_svg_viewbox(root)

    grid = int(math.sqrt(parts))
    if grid * grid != parts:
        raise ValueError("Le nombre de parties doit être un carré parfait (4, 9, 16, ...).")

    os.makedirs(tiles_dir, exist_ok=True)

    tile_w = vb_w / grid
    tile_h = vb_h / grid

    # Taille de sortie de chaque tuile.
    # Exemple : si zoom=2, chaque tuile est rendue 2x plus grande.
    out_tile_w = max(1, int(round(tile_w * zoom)))
    out_tile_h = max(1, int(round(tile_h * zoom)))

    tile_paths = []

    print(f"[INFO] SVG viewBox : x={vb_x}, y={vb_y}, w={vb_w}, h={vb_h}")
    print(f"[INFO] Découpage   : {grid} x {grid} = {parts} tuiles")
    print(f"[INFO] Zoom        : {zoom}")
    print(f"[INFO] Tuile PNG   : {out_tile_w} x {out_tile_h}px")

    for row in range(grid):
        for col in range(grid):
            x = vb_x + col * tile_w
            y = vb_y + row * tile_h

            tile_path = os.path.join(tiles_dir, f"tile_r{row+1}_c{col+1}.png")
            render_tile(
                svg_bytes=svg_bytes,
                tile_viewbox=(x, y, tile_w, tile_h),
                output_width=out_tile_w,
                output_height=out_tile_h,
                out_path=tile_path,
            )
            tile_paths.append((row, col, tile_path))
            print(f"[OK] Tuile créée : {tile_path}")

    # Fusion des tuiles
    final_width = out_tile_w * grid
    final_height = out_tile_h * grid
    merged = Image.new("RGBA", (final_width, final_height), (255, 255, 255, 0))

    for row, col, tile_path in tile_paths:
        with Image.open(tile_path) as img:
            merged.paste(img, (col * out_tile_w, row * out_tile_h))

    merged.save(output_png)
    print(f"[OK] Image finale créée : {output_png}")
    print(f"[INFO] Taille finale     : {final_width} x {final_height}px")


def main():
    parser = argparse.ArgumentParser(
        description="Découpe un SVG PlantUML en 4 ou 16 tuiles PNG zoomées puis les fusionne en une seule grande image PNG."
    )
    parser.add_argument(
        "url",
        help="URL du SVG (ex: https://img.plantuml.biz/plantuml/dsvg/...)"
    )
    parser.add_argument(
        "--parts",
        type=int,
        default=4,
        choices=[4, 16],
        help="Nombre de morceaux : 4 ou 16 (défaut: 4)"
    )
    parser.add_argument(
        "--zoom",
        type=float,
        default=2.0,
        help="Facteur de zoom/rendu pour chaque tuile (défaut: 2.0)"
    )
    parser.add_argument(
        "--output",
        default="plantuml_merged.png",
        help="Nom du PNG final (défaut: plantuml_merged.png)"
    )
    parser.add_argument(
        "--tiles-dir",
        default="tiles_output",
        help="Dossier de sortie des tuiles (défaut: tiles_output)"
    )

    args = parser.parse_args()

    try:
        split_and_merge(
            svg_url=args.url,
            parts=args.parts,
            zoom=args.zoom,
            output_png=args.output,
            tiles_dir=args.tiles_dir,
        )
    except Exception as e:
        print(f"[ERREUR] {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()