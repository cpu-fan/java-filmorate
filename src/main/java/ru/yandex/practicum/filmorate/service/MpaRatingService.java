package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.impl.MpaRatingDaoImpl;

import java.util.List;

@Service
public class MpaRatingService {
    private final MpaRatingDaoImpl mpaRatingDao;

    @Autowired
    public MpaRatingService(MpaRatingDaoImpl mpaRatingDao) {
        this.mpaRatingDao = mpaRatingDao;
    }

    public List<MpaRating> getMpaRatings() {
        return mpaRatingDao.getMpaRatings();
    }

    public MpaRating getMpaRatingById(int id) {
        return mpaRatingDao.getMpaRatingById(id);
    }
}
