--liquibase formatted sql

--changeset LeonFelipeCordero:001-order-tables logicalFilePath:/db/changelog/migrations/001-order-tables.sql

create table orders
(
    order_reference    text           not null,
    -- how can I make this better? is it as easy as create and respond?
    external_reference text           not null,
    instrument         text           not null,
    nominals           numeric(15, 6) not null,
    price              numeric(15, 6) not null,
    amount             numeric(15, 2) not null,
    currency           text           not null,
    type               text           not null,
    trader             text           not null,
    institution        text           not null,
    state              text           not null,
    timestamp          timestamptz    not null,
    created_at         timestamptz    not null default CURRENT_TIMESTAMP,
    updated_at         timestamptz    not null default CURRENT_TIMESTAMP,

    constraint orders_order_reference_pk primary key (order_reference)
);
create index order_external_reference_idx on orders (external_reference);

create table matched_orders
(
    buy_reference    text           not null,
    sell_reference   text           not null,
    matched_nominals numeric(15, 6) not null,
    created_at       timestamptz    not null default CURRENT_TIMESTAMP,
    updated_at       timestamptz    not null default CURRENT_TIMESTAMP,

    constraint order_matching_pk primary key (buy_reference, sell_reference),
    constraint orders_matching_orders_fk1 foreign key (buy_reference) references orders (order_reference),
    constraint orders_matching_orders_fk2 foreign key (sell_reference) references orders (order_reference)
)