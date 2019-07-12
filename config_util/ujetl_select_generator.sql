CREATE OR REPLACE FUNCTION pg_temp.ujetl_select(sch text, tabname text)
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

    header := E'SELECT\n    ';
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
        end if;
        col_list := col_list || quote_ident(colinfo.column_name);

        is_first = false;
    end loop;

    s := header ||
        coalesce(col_list,'col_list failed') ||
        E'\nFROM\n    ' ||
        quote_ident(sch)||'.'||quote_ident(tabname)||E' as t \n    '||
        E'WHERE\n    insert criteria here >= ?::datatype';
    return s;
end;
$function$
;






