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
        array_to_string(array_agg(quote_ident(pg_attribute.attname::text) ),', ') into pks
    FROM
        pg_index,
        pg_class,
        pg_attribute,
        pg_namespace
    WHERE
        pg_class.relname = tabname
        AND indrelid = pg_class.oid
        AND nspname = sch
        AND pg_class.relnamespace = pg_namespace.oid
        AND pg_attribute.attrelid = pg_class.oid
        AND pg_attribute.attnum = any(pg_index.indkey)
        AND indisprimary ;

    header := E'INSERT INTO '||quote_ident(sch)||'.'||quote_ident(tabname)||E' as t (\n    ';
    for colinfo in 
        select
            *
        from
            information_schema.columns
        where
            table_schema = sch
            and table_name = tabname
        order by ordinal_position
    loop
        if not is_first then 
            col_list := col_list || E',\n    ';
            vals := vals || E',\n    ';
            sets := sets || E',\n    ';
            changes := changes || E'\n    OR ';
        end if;
        col_list := col_list || quote_ident(colinfo.column_name);
        vals := vals || '?::' || colinfo.data_type;
        sets := sets || quote_ident(colinfo.column_name) ||
            E' = EXCLUDED.' || quote_ident(colinfo.column_name);
        changes := changes || E't.' || quote_ident(colinfo.column_name) ||
            E' IS DISTINCT FROM EXCLUDED.' || quote_ident(colinfo.column_name);

        is_first = false;
    end loop;

    s := coalesce(header,'header failed') ||
        coalesce(col_list,'col_list failed') ||
        E'\n)VALUES(\n    ' ||
        coalesce(vals,'vals failed') ||
        E')\nON CONFLICT(' || coalesce(pks,'No primary keys found') || E') DO UPDATE\nSET\n    ' ||
        coalesce(sets,'sets failed') ||
        E'\nWHERE\n    '||
        coalesce(changes,'changes failed');
    return s;
end;
$function$
;






