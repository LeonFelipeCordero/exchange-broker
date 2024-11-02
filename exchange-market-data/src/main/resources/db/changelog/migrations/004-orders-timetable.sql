--liquibase formatted sql

--changeset LeonFelipeCordero:004-orders-timetable logicalFilePath:/db/changelog/migrations/004-orders-timetable.sql

select 1;
-- create table open_orders
-- (
--     order_reference text           not null,
--     instrument      text           not null,
--     nominals        numeric(15, 6) not null,
--     price           numeric(15, 6) not null,
--     price_key       numeric(15, 2) not null,
--     type            text           not null,
--     timestamp       timestamptz    not null,
--     created_at      timestamptz    not null,
--     updated_at      timestamptz    not null,
--
--     constraint open_orders_order_reference_pk primary key (order_reference)
-- );
--
-- select create_hypertable('open_orders', by_range('timestamp'));
--
-- create index open_orders_timetable_isin_timestamp_idx
--     on open_orders (instrument, price_key, timestamp desc);
