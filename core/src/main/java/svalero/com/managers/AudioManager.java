package svalero.com.managers;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

public class AudioManager {
    private final Audio audio;
    private final Preferences prefs;
    private final ObjectMap<String, Sound> sfx = new ObjectMap<>();
    private final ObjectMap<String, Music> musicTracks = new ObjectMap<>();
    private Music currentMusic;

    private boolean soundEnabled;
    private boolean musicEnabled;
    private float sfxVolume;
    private float musicVolume;

    public AudioManager() {
        this(Gdx.audio, Gdx.app.getPreferences("keyfinder-audio"));
    }


    public AudioManager(Audio audio, Preferences prefs) {
        this.audio = audio;
        this.prefs = prefs;
        ensureDefaults();
        refreshSettingsFromPrefs();
    }


    public void loadSfx (String id, String path){
        sfx.put(id, audio.newSound(Gdx.files.internal(path)));
    }

    public void loadMusic (String id, String path){
        refreshSettingsFromPrefs();
        Music m = audio.newMusic(Gdx.files.internal(path));
        m.setLooping(true);
        m.setVolume(musicVolume);
        musicTracks.put(id,m);
    }

    public void playSfx(String id){
        refreshSettingsFromPrefs();
        if (!soundEnabled){
            return;
        }

        Sound sound = sfx.get(id);
        if(sound!=null) sound.play(sfxVolume);
    }


    public void playMusic(String id, boolean loop) {
        refreshSettingsFromPrefs();
        Music next = musicTracks.get(id);
        if (next == null) return;

        if (currentMusic != null && currentMusic != next) currentMusic.stop();
        currentMusic = next;
        currentMusic.setLooping(loop);
        currentMusic.setVolume(musicVolume);

        if (musicEnabled) currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
    }

    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        save();
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (currentMusic != null) {
            if (enabled) {
                if (!currentMusic.isPlaying()) {
                    currentMusic.play();
                }
            }
            else currentMusic.pause();
        }
        save();
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp01(volume);
        save();
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);
        if (currentMusic != null) currentMusic.setVolume(musicVolume);
        save();
    }

    public boolean isSoundEnabled() {
        refreshSettingsFromPrefs();
        return soundEnabled;
    }

    public boolean isMusicEnabled() {
        refreshSettingsFromPrefs();
        return musicEnabled;
    }

    public void dispose() {
        for (Sound s : sfx.values()) s.dispose();
        for (Music m : musicTracks.values()) m.dispose();
        sfx.clear();
        musicTracks.clear();
        currentMusic = null;
    }

    private void save() {
        prefs.putBoolean("soundEnabled", soundEnabled);
        prefs.putBoolean("musicEnabled", musicEnabled);
        prefs.putFloat("sfxVolume", sfxVolume);
        prefs.putFloat("musicVolume", musicVolume);
        prefs.flush();
    }

    private void ensureDefaults() {
        boolean needsFlush = false;
        if (!prefs.contains("soundEnabled")) {
            prefs.putBoolean("soundEnabled", true);
            needsFlush = true;
        }
        if (!prefs.contains("musicEnabled")) {
            prefs.putBoolean("musicEnabled", true);
            needsFlush = true;
        }
        if (!prefs.contains("sfxVolume")) {
            prefs.putFloat("sfxVolume", 1f);
            needsFlush = true;
        }
        if (!prefs.contains("musicVolume")) {
            prefs.putFloat("musicVolume", 1f);
            needsFlush = true;
        }
        if (needsFlush) {
            prefs.flush();
        }
    }

    private void refreshSettingsFromPrefs() {
        soundEnabled = prefs.getBoolean("soundEnabled", true);
        musicEnabled = prefs.getBoolean("musicEnabled", true);
        sfxVolume = clamp01(prefs.getFloat("sfxVolume", 1f));
        musicVolume = clamp01(prefs.getFloat("musicVolume", 1f));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
