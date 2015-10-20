# --- !Ups

CREATE TABLE code_snippets(
  id SERIAL PRIMARY KEY,
  label TEXT NOT NULL,
  code TEXT NOT NULL
);

CREATE TABLE code_snippet_analysis_results(
  id SERIAL PRIMARY KEY,
  snippet_id INTEGER UNIQUE NOT NULL REFERENCES code_snippets
);

CREATE TABLE code_snippet_analysis_complaints(
  id SERIAL PRIMARY KEY,
  analysis_id INTEGER NOT NULL REFERENCES code_snippet_analysis_results,
  line INTEGER NOT NULL,
  col INTEGER NOT NULL,
  message TEXT NOT NULL,
  severity TEXT NOT NULL
);

# --- !Downs

DROP TABLE code_snippet_analysis_complaints;
DROP TABLE code_snippet_analysis_results;
DROP TABLE code_snippets;
