-- Add optional avatar/image path columns for serving images via existing resources
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_path TEXT;
ALTER TABLE services ADD COLUMN IF NOT EXISTS image_path TEXT;

