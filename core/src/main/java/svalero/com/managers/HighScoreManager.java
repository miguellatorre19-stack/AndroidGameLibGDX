package svalero.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HighScoreManager {
    private static final String PREFS_NAME = "keyfinder-highscores";
    private static final String SCORE_PREFIX = "score_";
    private static final int MAX_SCORES = 10;

    private final Preferences prefs;

    public HighScoreManager() {
        this(Gdx.app.getPreferences(PREFS_NAME));
    }

    HighScoreManager(Preferences prefs) {
        this.prefs = prefs;
    }

    public HighScoreRegistration registerScore(int score) {
        List<Integer> scores = loadScores();
        int normalizedScore = Math.max(0, score);
        int rank = 1;
        for (int savedScore : scores) {
            if (savedScore > normalizedScore) {
                rank++;
            }
        }

        scores.add(normalizedScore);
        scores.sort(Comparator.reverseOrder());
        boolean enteredTop10 = rank <= MAX_SCORES;

        if (scores.size() > MAX_SCORES) {
            scores = new ArrayList<>(scores.subList(0, MAX_SCORES));
        }

        saveScores(scores);
        return new HighScoreRegistration(scores, rank, enteredTop10);
    }

    public List<Integer> loadTopScores() {
        return loadScores();
    }

    private List<Integer> loadScores() {
        List<Integer> scores = new ArrayList<>();
        for (int i = 0; i < MAX_SCORES; i++) {
            String key = SCORE_PREFIX + i;
            if (!prefs.contains(key)) continue;
            scores.add(Math.max(0, prefs.getInteger(key, 0)));
        }
        scores.sort(Comparator.reverseOrder());
        return scores;
    }

    private void saveScores(List<Integer> scores) {
        for (int i = 0; i < MAX_SCORES; i++) {
            prefs.remove(SCORE_PREFIX + i);
        }
        for (int i = 0; i < scores.size() && i < MAX_SCORES; i++) {
            prefs.putInteger(SCORE_PREFIX + i, scores.get(i));
        }
        prefs.flush();
    }

    public record HighScoreRegistration(List<Integer> topScores, int rank, boolean enteredTop10) {
    }
}
