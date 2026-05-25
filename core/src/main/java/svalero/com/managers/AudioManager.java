package svalero.com.managers;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

        soundEnabled = prefs.getBoolean("soundEnabled", true);
        musicEnabled = prefs.getBoolean("musicEnabled", true);
        sfxVolume = prefs.getFloat("sfxVolume", 1f);
        musicVolume = prefs.getFloat("musicVolume", 1f);
    }


    public void loadSfx (String id, String path){
        sfx.put(id, audio.newSound(Gdx.files.internal(path)));
    }

    public void loadMusic (String id, String path){
        Music m = audio.newMusic(Gdx.files.internal(path));
        m.setLooping(true);
        m.setVolume(musicVolume);
        musicTracks.put(id,m);
    }

    public void playSfx(String id){
        if (!soundEnabled){
            return;
        }

        Sound sound = sfx.get(id);
        if(sound!=null) sound.play(sfxVolume);
    }


    public void playMusic(String id, boolean loop) {
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
            if (enabled) currentMusic.play();
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

    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }

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

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
