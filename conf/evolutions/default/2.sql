# --- !Ups

ALTER TABLE code_snippets ADD COLUMN ip TEXT;

# --- !Downs

ALTER TABLE code_snippets DROP COLUMN ip;
