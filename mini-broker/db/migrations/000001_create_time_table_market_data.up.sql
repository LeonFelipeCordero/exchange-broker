create table instruments_timetable
(
    isin             text        not null,
    name             text        not null,
    instrument_state text        not null,
    currency         text        not null,
    timestamp        timestamptz not null,

    constraint instruments_isin_timestamp_pk primary key (isin, timestamp)
);

select create_hypertable('instruments_timetable', by_range('timestamp'));

create index instruments_isin_timestamp_idx on instruments_timetable (isin, timestamp desc);

create table quotes_timetable
(
    isin      text           not null,
    currency  text           not null,
    quote     numeric(15, 6) not null,
    timestamp timestamptz    not null,

    constraint quotes_isin_timestamp_pk primary key (isin, timestamp)
);

select create_hypertable('quotes_timetable', by_range('timestamp'));

create index quotes_isin_timestamp_idx on quotes_timetable (isin, timestamp desc);
