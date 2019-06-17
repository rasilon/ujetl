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

\c postgres
CREATE TABLE public.container_ready AS SELECT 1 FROM(VALUES(1)) AS a(a);
GRANT SELECT ON public.container_ready TO TEST;

