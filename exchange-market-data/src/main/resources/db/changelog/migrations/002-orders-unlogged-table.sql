--liquibase formatted sql

--changeset LeonFelipeCordero:002-orders-unlogged-table logicalFilePath:/db/changelog/migrations/002-orders-unlogged-table.sql

create table open_orders
(
    order_reference text           not null,
    instrument      text           not null,
    nominals        numeric(15, 6) not null,
    price           numeric(15, 6) not null,
    price_key       numeric(15, 2) not null,
    type            text           not null,
    timestamp       timestamptz    not null,
    created_at      timestamptz    not null,
    updated_at      timestamptz    not null,

    constraint open_orders_order_reference_pk primary key (order_reference)
);

-- create index open_orders_isin_range_direction_idx
--     on open_orders (instrument, price_key, type);

create unlogged table order_matching_semaphore
(
    order_reference text        not null,
    created_at      timestamptz not null,

    constraint order_matching_semaphore_order_reference_pk primary key (order_reference)
);