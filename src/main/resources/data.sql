-- Вот почему-то с merge into не работают тесты. Почитал про merge into, в одной статье писали, что оно уже не
-- поддерживается и можно использовать on conflict do nothing, но идея упорно считает это ошибкой.

insert into MPA_RATINGS (NAME)
values ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

insert into GENRES (NAME)
values ('Комедия'),
       ('Драма'),
       ('Мультфильм'),
       ('Триллер'),
       ('Документальный'),
       ('Боевик');