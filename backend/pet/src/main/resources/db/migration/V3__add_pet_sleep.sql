-- A pet put to bed sleeps until this moment and cannot be cared for meanwhile; NULL while it is awake.
ALTER TABLE pets ADD COLUMN asleep_until TIMESTAMPTZ;
