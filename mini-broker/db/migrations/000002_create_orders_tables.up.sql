create table orders
(
    reference        text           not null,
    instrument       text           not null,
    nominals         numeric(15, 2) not null,
    price            numeric(15, 6) not null,
    amount           numeric(15, 2) not null,
    currency         text           not null,
    type             text           not null,
    trader           text           not null,
    institution      text           not null,
    timestamp        timestamptz    not null,
    order_reference  text,
    filled           boolean        not null,
    filled_timestamp timestamptz,
    constraint orders_pk primary key (reference)
);

create index order_order_reference_idx on orders(order_reference);
