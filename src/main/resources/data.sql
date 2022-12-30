insert into MPA_RATING (NAME)
values ('G'),
       ('PG'),
       ('PG-13'),
       ('R'),
       ('NC-17');

insert into GENRE (NAME)
values ('Comedy'),
       ('Drama'),
       ('Cartoon'),
       ('Thriller'),
       ('Documentary'),
       ('Pseudo-documentary'),
       ('Action'),
       ('Adventure'),
       ('Sci-Fi');

insert into FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING_ID)
values ('Inception', 'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.', '2010-07-08', 148, 2),
       ('Interstellar', 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity''s survival.', '2014-10-26', 169, 2),
       ('District 9', 'Violence ensues after an extraterrestrial race forced to live in slum-like conditions on Earth finds a kindred spirit in a government agent exposed to their biotechnology.', '2009-08-13', 112, 3);

insert into FILM_GENRES
values (1, 6),
       (1, 7),
       (1, 8),
       (2, 8),
       (2, 7),
       (2, 6),
       (3, 6),
       (3, 4),
       (3, 9);

insert into USERS (LOGIN, NAME, EMAIL, BIRTHDAY)
values ('chernyshevsky', 'Nikolay Gavrilovich Chernyshevsky', 'ngchernyshevsky@sovremennik.re', '1828-07-24'),
       ('belinsky ', 'Vissarion Grigoryevich Belinsky', 'vgbelinsky@teleskop.re', '1811-06-11'),
       ('dolin', 'Anton', 'ant.dolin@gmail.com', '1976-01-23');

insert into FRIENDS
values (1, 2, true),
       (3, 2, false),
       (3, 1, false);

insert into FILM_LIKES
values (2, 3),
       (1, 3),
       (3, 3),
       (2, 1),
       (1, 2);