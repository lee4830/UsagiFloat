Manual gallery1.0.9

Uses Pillow, NumPy, OpenCV. Install the pinned dependencies from ../cutouts/requirements.txt in a local virtual environment.
Place the seven user-supplied GIFs named in report.json in a local directory, then run:
  USAGI_SOURCE_DIR=/path/to/gifs python process.py

The original five input assets are in originals/. New GIF originals are user-supplied, not downloaded by this script. Default output is generated/ (ignored by git). Inspect every output contact sheet before accepting a mask, especially ears, body edges and source-cropped parts. Character-specific masking is not a general-purpose background removal model.

To install processed output after visual review:
  Copy generated/*.webp into app/src/main/res/raw/ at the project root.
  Copy generated/*_thumb.png into app/src/main/res/drawable-nodpi/.
  Copy generated/report.json here, then run python verify.py.
verify.py checks frame totals/timing, transparent canvas and fixed silhouette bounds, and writes gallery-preview.jpg.

All frames share a fixed crop and scale per animation.512px output canvas with460px maximum sequence extent retains movement and avoids per-frame zoom. Small sources are enlarged with Lanczos interpolation; missing details are not reconstructed. Source proportions and preexisting cropped composition remain. Alpha is generated around original character color/ink pixels, without repainting the character.
