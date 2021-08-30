import com.soywiz.korau.sound.Sound
import com.soywiz.korau.sound.SoundChannel

suspend fun Sound.playFixed(): SoundChannel {
    return play().also { channel -> channel.volume = volume }
}