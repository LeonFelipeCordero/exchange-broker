create user broker with password 'broker';

create database broker owner broker;

\connect broker;
GRANT USAGE ON SCHEMA public TO broker;
GRANT ALL PRIVILEGES ON SCHEMA public TO broker;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO broker;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO broker;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO broker;

\connect exchange;
