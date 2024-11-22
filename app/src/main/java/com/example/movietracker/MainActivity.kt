package com.example.movietracker

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var movieBookAdapter: MovieBookAdapter
    private lateinit var movieBooksList: MutableList<MovieBook>

    private lateinit var etTitle: EditText
    private lateinit var etGenre: EditText
    private lateinit var etDescription: EditText
    private lateinit var seekBarRating: SeekBar
    private lateinit var radioGroupType: RadioGroup
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etTitle = findViewById(R.id.etTitle)
        etGenre = findViewById(R.id.etGenre)
        etDescription = findViewById(R.id.etDescription)
        seekBarRating = findViewById(R.id.seekBarRating)
        radioGroupType = findViewById(R.id.radioGroupType)
        btnAdd = findViewById(R.id.btnAdd)
        recyclerView = findViewById(R.id.recyclerView)

        movieBooksList = loadMovieBooks()
        movieBookAdapter = MovieBookAdapter(movieBooksList, ::onItemClicked)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = movieBookAdapter

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString()
            val genre = etGenre.text.toString()
            val description = etDescription.text.toString()
            val rating = seekBarRating.progress
            val type = if (radioGroupType.checkedRadioButtonId == R.id.radioButtonMovie) "movie" else "book"

            if (title.isNotBlank() && genre.isNotBlank() && description.isNotBlank()) {
                val newMovieBook = MovieBook(title, genre, description, rating, type)
                movieBooksList.add(newMovieBook)
                movieBookAdapter.notifyItemInserted(movieBooksList.size - 1)
                saveMovieBooks(movieBooksList)
                clearForm()
            } else {
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMovieBooks(): MutableList<MovieBook> {
        val file = File(filesDir, "movies_books.json")
        if (file.exists()) {
            val json = file.readText()
            val type: Type = object : TypeToken<MutableList<MovieBook>>() {}.type
            return Gson().fromJson(json, type)
        }
        return mutableListOf()
    }

    private fun saveMovieBooks(movieBooksList: MutableList<MovieBook>) {
        val file = File(filesDir, "movies_books.json")
        val json = Gson().toJson(movieBooksList)
        file.writeText(json)
    }

    private fun clearForm() {
        etTitle.text.clear()
        etGenre.text.clear()
        etDescription.text.clear()
        seekBarRating.progress = 0
        radioGroupType.clearCheck()
    }

    private fun onItemClicked(movieBook: MovieBook) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(movieBook.title)
        dialog.setMessage("""
            Gatunek: ${movieBook.genre}
            Opis: ${movieBook.description}
            Ocena: ${movieBook.rating}/10
            Status: ${if (movieBook.isFinished) "Zakończone" else "W trakcie"}
        """.trimIndent())
        dialog.setPositiveButton("OK") { _, _ -> }
        dialog.show()
    }
}


data class MovieBook(
    val title: String,
    val genre: String,
    val description: String,
    val rating: Int,
    val type: String,
    val isFinished: Boolean = false
)
class MovieBookAdapter(
    private val movieBooksList: MutableList<MovieBook>,
    private val onItemClick: (MovieBook) -> Unit
) : RecyclerView.Adapter<MovieBookAdapter.MovieBookViewHolder>() {

    inner class MovieBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val genre: TextView = itemView.findViewById(R.id.tvTitle)
        val rating: TextView = itemView.findViewById(R.id.seekBarRating)

        fun bind(movieBook: MovieBook) {
            title.text = movieBook.title
            genre.text = movieBook.genre
            rating.text = "${movieBook.rating}/10"

            itemView.setOnClickListener {
                onItemClick(movieBook)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieBookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_main, parent, false)
        return MovieBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieBookViewHolder, position: Int) {
        val movieBook = movieBooksList[position]
        holder.bind(movieBook)
    }

    override fun getItemCount(): Int = movieBooksList.size

    fun updateList(newList: List<MovieBook>) {
        movieBooksList.clear()
        movieBooksList.addAll(newList)
        notifyDataSetChanged()
    }
}

