CREATE USER monitoring WITH PASSWORD 'metrics-12345';
GRANT pg_monitor TO monitoring;
GRANT SELECT ON pg_stat_database TO monitoring;

