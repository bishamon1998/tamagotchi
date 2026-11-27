-- When the pet was last stroked in a way that cheered it up; strokes in between change nothing.
ALTER TABLE pets ADD COLUMN last_stroked_at TIMESTAMPTZ;
