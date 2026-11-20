Source video frame extraction for1.0.7 (2026-11-20).
User expressly requested original motions after rejecting generated poses.
short.mp4: https://www.youtube.com/shorts/mwfyXAL1Alo (15seconds,608x1080,30fps)
compilation-opening.mp4: opening60seconds of https://www.youtube.com/watch?v=309nc2qTfwE (1280x720,30fps).
Video and audio are not included in source ZIP. Original footage character rights remain with their owners. Unofficial personal app.
Actual source clips: WALK28.6–29.0s(compilation), LOOK40.0–41.0s(compilation), WIGGLE4.2–6.8s(short), REST/bow3.2–4.2s(short).
Each output preserves original frame order and30fps. Background removed with per-frame foreground color seeds, graph cut and contour restoration; one fixed crop and uniform scale for each clip. No generated poses, interpolation, rotation, stretching or mirroring. No audio. The original scene framing/background/camera travel outside the selected crop is not reproduced on the phone. Overlay route remains an app feature.
For reproduction, place the two named inputs in work/video-reference relative to your current directory; create its processed subdirectory, install requirements, run build_clips.py. Output frames are256square,8columns. Source-segments.json records count, crop, timing and per-frame bounding boxes. Sources should be obtained lawfully; no login cookies or access-control bypass used.
Mask limitations: fine detached motion marks may be omitted; video compression remains. Outfit and visible chocolate marks are from the original footage and deliberately retained. Small jump clip has lower source resolution than the dance.
