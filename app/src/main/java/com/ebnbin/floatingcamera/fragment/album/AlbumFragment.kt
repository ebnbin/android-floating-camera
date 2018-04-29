package com.ebnbin.floatingcamera.fragment.album

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.util.BaseRuntimeException
import com.ebnbin.floatingcamera.util.FileUtil
import com.ebnbin.floatingcamera.util.displaySize
import com.ebnbin.floatingcamera.util.getColorAttr
import com.ebnbin.floatingcamera.util.res
import kotlinx.android.synthetic.main.album_fragment.recyclerView
import kotlinx.android.synthetic.main.album_fragment.swipeRefreshLayout
import java.io.File

class AlbumFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.album_fragment, container, false)
    }

    private var adapter: AlbumAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        swipeRefreshLayout.setColorSchemeColors(getColorAttr(context, R.attr.colorAccent))
        swipeRefreshLayout.setOnRefreshListener {
            adapter?.invalidateFile()
            swipeRefreshLayout.isRefreshing = false
        }

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

            val name = files[position]
            val file = File(FileUtil.getPath(), name)
            val isPhoto = when {
                name.endsWith(".mp4") -> false
                name.endsWith(".jpg") -> true
                else -> throw BaseRuntimeException()
            }

            Glide.with(context)
                    .load(file)
                    .into(holder.imageView)

            holder.imageView.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Uri.fromFile(file) else
                        FileProvider.getUriForFile(context, "com.ebnbin.floatingcamera.fileprovider", file)
                    val type = if (isPhoto) "image/*" else "video/*"
                    intent.setDataAndType(uri, type)
                    startActivity(intent)
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }
            }
            holder.imageView.setOnLongClickListener {
                AlertDialog.Builder(context)
                        .setTitle(if (isPhoto) R.string.delete_title_photo else R.string.delete_title_video)
                        .setMessage(res.getString(R.string.delete_message, name))
                        .setPositiveButton(R.string.delete_positive) { _, _ ->
                            try {
                                file.delete()
                                invalidateFile()
                            } catch (e: Exception) {
                                Crashlytics.logException(e)
                            }
                        }
                        .setNegativeButton(R.string.delete_negative, null)
                        .create()
                        .show()
                true
            }
        }

        fun invalidateFile() {
            files = FileUtil.getPath().list { _, name ->
                return@list name.endsWith(".mp4") || name.endsWith(".jpg")
            }.sortedArrayDescending()
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
