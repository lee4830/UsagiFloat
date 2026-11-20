# Original video motion1.0.7 implementation plan
Goal: replace generated poses with actual frame sequences from the user's two references, preserving shape, order and30fps timing.
Architecture: transparent fixed-crop atlases, one per behavior; MotionClips supplies frame counts/timing to the existing cancellable WanderMotion uptime engine. WanderDrawable draws source frames without rotation/stretch/mirroring. Existing random/manual/settings/lifecycle retained. Video footage is used only for original character frames; no audio.
Execution: inline via executing-plans workflow. User explicitly requested actual reference motion; no additional design decision needed.
- Extract isolated whole-body sequences; preserve relative in-clip translation and one uniform scale per clip. Visually inspect all sampled masks. No generated pixels.
- Add regression tests for30fps playback unaffected by speed setting and complete idle-clip duration; observe old engine failure.
- Implement MotionClips, replace renderer, remove generated references, keep drop2s stationary using neutral source frame.
- Build signed1.0.7; unit/lint checks; Android11 actual frame/drag/wake/house tests and independent review.
- Deliver signed APK, transparent motion previews, sources and exact provenance/limitations. No claim that camera framing or footage background is retained; character pose sequences and source timing are retained.
