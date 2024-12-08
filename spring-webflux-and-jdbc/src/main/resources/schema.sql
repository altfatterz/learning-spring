create table if not exists payments
(
    id serial primary key,
    payer varchar(100) not null,
    payee varchar(100) not null,
    amount numeric(6,2) not null
);