-- The name the user gave their pet when it arrived; NULL until then, which makes the Mini App show the arrival scene.
ALTER TABLE pets ADD COLUMN name TEXT;
