package com.ebnbin.floatingcamera.fragment.album

import android.os.Bundle
import android.os.FileObserver
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.displaySize
import com.ebnbin.floatingcamera.util.res
import kotlinx.android.synthetic.main.album_fragment.recyclerView
import java.io.File

class AlbumFragment : Fragment() {
    private lateinit var fileObserver: FileObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileObserver = object : FileObserver(FileUtil.getPath().absolutePath, FileObserver.MODIFY) {
            override fun onEvent(event: Int, path: String?) {
                when (event) {
                    FileObserver.MODIFY -> {
                        if (!isAdded) return
                        adapter?.invalidateFile()
                    }
                }
            }
        }
        fileObserver.startWatching()
    }

    override fun onDestroy() {
        fileObserver.stopWatching()

        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.album_fragment, container, false)
    }

    private var adapter: AlbumAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        val spanCount = (displaySize.width().toFloat() / res.displayMetrics.density / 160).toInt()
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        adapter = AlbumAdapter(spanCount)
        recyclerView.adapter = adapter
    }

    private inner class AlbumAdapter(private val spanCount: Int) : RecyclerView.Adapter<AlbumViewHolder>() {
        private lateinit var files: Array<String>
        init {
            invalidateFile()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false)
            return AlbumViewHolder(itemView, spanCount)
        }

        override fun getItemCount(): Int {
            return files.size
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            val context = context ?: return

            Glide.with(context)
                    .load(File(FileUtil.getPath(), files[position]))
                    .into(holder.imageView)
        }

        fun invalidateFile() {
            files = FileUtil.getPath().list().sortedArrayDescending()
            notifyDataSetChanged()
        }
    }

    private inner class AlbumViewHolder(itemView: View, spanCount: Int) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        init {
            val params = imageView.layoutParams
            params.height = (displaySize.width().toFloat() / spanCount).toInt()
            imageView.layoutParams = params
        }
    }
}
