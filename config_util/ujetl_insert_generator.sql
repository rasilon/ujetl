CREATE OR REPLACE FUNCTION pg_temp.ujetl_insert(sch text, tabname text)
 RETURNS text
 LANGUAGE plpgsql
AS $function$
declare
    s text := '';
    header text := '';
    col_list text := '';
    vals text := '';
    sets text := '';
    changes text := '';
    is_first boolean := true;
    colinfo record;
    pks text;
begin
    SELECT
	  	array_to_string(array_agg(pg_attribute.attname::text ),', ') into pks
	FROM
		pg_index,
	   	pg_class,
	   	pg_attribute,
	   	pg_namespace
	WHERE
		pg_class.relname = tabname AND
		indrelid = pg_class.oid AND
		nspname = sch AND
		pg_class.relnamespace = pg_namespace.oid AND
		pg_attribute.attrelid = pg_class.oid AND
		pg_attribute.attnum = any(pg_index.indkey)
		AND indisprimary ;

    header := 'INSERT INTO '||sch||'.'||tabname||E' as t (\n    ';
    for colinfo in 
        select
            *
        from
            information_schema.columns
        where
            table_schema = 'bi_processing'
            and table_name = 'player'
        order by ordinal_position
    loop
        if not is_first then 
            col_list := col_list || E',\n    ';
            vals := vals || E',\n    ';
            sets := sets || E',\n    ';
            changes := changes || E'\n    OR ';
        end if;
        col_list := col_list || colinfo.column_name;
        vals := vals || '?::' || colinfo.data_type;
        sets := sets || colinfo.column_name || E' = EXCLUDED.' || colinfo.column_name;
        changes := changes || E't.' || colinfo.column_name || E' IS DISTINCT FROM EXCLUDED.' || colinfo.column_name;

        is_first = false;
    end loop;

    s := header ||
        col_list ||
        E'\n)VALUES(\n    ' ||
        vals ||
        E')\nON CONFLICT(' || pks || E') DO UPDATE\nSET\n    ' ||
        sets ||
        E'\nWHERE\n    '||
        changes;
    return s;
end;
$function$
;






