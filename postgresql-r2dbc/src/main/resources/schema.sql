create table if not exists customers
(
    id serial primary key,
    firstname varchar(100)  not null,
    lastname varchar(100) not null,
    balance numeric(20,6) not null
);