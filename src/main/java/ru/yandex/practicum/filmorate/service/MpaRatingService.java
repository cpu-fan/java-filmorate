package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.MpaRatingDao;
import ru.yandex.practicum.filmorate.storage.impl.MpaRatingDaoImpl;

import java.util.List;

@Service
@Slf4j
public class MpaRatingService {
    private final MpaRatingDao mpaRatingDao;

    @Autowired
    public MpaRatingService(MpaRatingDaoImpl mpaRatingDao) {
        this.mpaRatingDao = mpaRatingDao;
    }

    public List<MpaRating> getMpaRatings() {
        log.info("Запрошен список возрастных рейтингов");
        return mpaRatingDao.getMpaRatings();
    }

    public MpaRating getMpaRatingById(int id) {
        log.info("Запрошен возрастной рейтинг id = " + id);
        return mpaRatingDao.getMpaRatingById(id);
    }
}
