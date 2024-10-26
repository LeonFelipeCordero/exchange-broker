--liquibase formatted sql

--changeset LeonFelipeCordero:003-transactional-outbox logicalFilePath:/db/changelog/migrations/003-transactional-outbox.sql

create table internal_transactional_outbox
(
    sequence   bigserial   not null,
    event      text        not null,
    message    text        not null,
    sent       boolean     not null default false,
    sent_at    timestamptz,
    created_at timestamptz not null default CURRENT_TIMESTAMP,
    updated_at timestamptz not null default CURRENT_TIMESTAMP,

    constraint iternal_transactional_outbox_pk primary key (sequence)
);
create index internal_transactional_outbox_sent_idx on internal_transactional_outbox (sent);
