CREATE DATABASE  test;
\c test
CREATE ROLE test login password 'test';
CREATE UNLOGGED TABLE source (                     
	id bigserial primary key,
	test_int integer,
	test_text text,
	test_ts timestamp with time zone
);
CREATE UNLOGGED TABLE dest (                     
	id bigint primary key,
	test_int integer,
	test_text text,
	test_ts timestamp with time zone
);


GRANT SELECT ON source to test;
GRANT SELECT,INSERT,UPDATE,DELETE ON dest TO test;

INSERT INTO source(test_int,test_text,test_ts) SELECT 1,'banana',now() FROM generate_series(1,100000);

CREATE TABLE normalised_first_names(
    fid smallserial not null primary key,
    fname text not null unique
);
CREATE TABLE normalised_last_names(
    lid smallserial not null primary key,
    lname text not null unique
);
INSERT INTO normalised_first_names (fname) values ('Abigail'), ('Adam'), ('Beatrice'), ('Bruce'), ('Claire'), ('Clive'), ('Deborah'), ('Dave');
INSERT INTO normalised_last_names (lname)  values ('Adams'), ('Bellamy'), ('Clark'), ('Dabrowski');

CREATE TABLE normalised_personalia (
    person_id serial not null primary key,
    fid smallint not null references normalised_first_names(fid),
    lid smallint not null references normalised_last_names(lid)
);
insert into normalised_personalia(fid,lid) values (1,1), (1,2), (1,3), (1,4), (2,1), (2,2), (2,3), (2,4), (3,1), (3,2), (3,3), (3,4), (4,1), (4,2), (4,3), (4,4);

CREATE TABLE denormalised_personalia(
    person_id integer not null primary key,
    fname text,
    lname text
);

CREATE TABLE test_csvjdbc(
    id integer not null primary key,
    dat text
);

GRANT SELECT ON ALL TABLES IN SCHEMA public TO test;
GRANT SELECT,INSERT,UPDATE ON denormalised_personalia TO test;

\c postgres
CREATE TABLE public.container_ready AS SELECT 1 FROM(VALUES(1)) AS a(a);
GRANT SELECT ON public.container_ready TO TEST;

