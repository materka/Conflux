package se.materka.conflux.db.repository

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.grushetsky.m3uparser.M3uLexer
import ru.grushetsky.m3uparser.M3uParser
import se.materka.conflux.PlaylistUtil
import se.materka.conflux.db.entity.Station
import se.materka.conflux.writeLn
import java.io.BufferedReader
import java.io.File

class StationFileRepository(private val context: Context) : Repository<Station> {
    private val stations: MutableLiveData<MutableList<Station>> = MutableLiveData<MutableList<Station>>().apply {
        value = mutableListOf()
    }

    override fun save(item: Station): LiveData<Boolean> {
        val list: MutableList<Station> = stations.value!!
        item.id = list.size.toLong()
        list.add(item)
        //File(context.assets.open("conflux.m3u")).buffere().use { out -> out.write(fileContent) }
        stations.postValue(list)
        return MutableLiveData<Boolean>().apply { postValue(true) }
    }

    override fun get(): LiveData<List<Station>> {
        //if member variable is empty, read from file and populate member variable
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(id: Long): LiveData<Station> {
        // try to get index from member variable
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(item: Station): LiveData<Boolean> {
        // change item in member variable with id == index
        // save to file, and reload stations form file
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(item: Station): LiveData<Boolean> {
        // delete index from member variable
        // save to file and reload stations from file
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exists(url: String): LiveData<Boolean> {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun readFile() {
        val file = File(context.filesDir, "context.m3u")
        if (file.exists()) {
            PlaylistUtil.getPlaylist(file.toUri())
        }
    }

    private fun writeFile() {
        val file = File(context.filesDir, "context.m3u")
        if (file.exists()) {
            file.bufferedWriter().use { writer ->
                writer.writeLn("#EXTM3U")
                stations.value?.forEach { station ->
                    writer.writeLn("#EXTINF:-1, ${station.name}")
                    writer.writeLn("${station.url}")
                }
            }
        }
    }

    private fun getParser(input: String, initMode: Int = 0): M3uParser {
        val m3uLexer = M3uLexer(CharStreams.fromString(input))
        m3uLexer.mode(initMode)
        return M3uParser(CommonTokenStream(m3uLexer))
    }
}