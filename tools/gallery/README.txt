Body calibration1.0.10
body-calibration.json records the main rounded-body width/centre and independent measurement row in1.0.9reference coordinates. normalization109.json maps these back to original source pixels. process.py reads original inputs, masks them, and applies one fixed transform per sequence onto640px canvas,300px reference body width, common anchor(320,420). Manual window/preview reserve640/512times the former space, so ear padding does not shrink the body. verify.py independently measures alpha row spans (296–316px) and checks original timing and unclipped margins. Existing source-edge crops are preserved.

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

All frames share a fixed crop and scale per animation.640px output canvas with300px calibrated body width retains movement and avoids per-frame zoom. Small sources are enlarged with Lanczos interpolation; missing details are not reconstructed. Source proportions and preexisting cropped composition remain. Alpha is generated around original character color/ink pixels, without repainting the character.
