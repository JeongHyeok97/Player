package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)

data class Vod(
    @JsonProperty("id") val id: String? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("time") val time: String? = null,
    @JsonProperty("censored") val censored: String? = null,
    @JsonProperty("category_id") val categoryId: String? = null,
    @JsonProperty("genre_id") val genreId: String? = null,
    @JsonProperty("tmdb_id") val tmdbId: String? = null,
    @JsonProperty("kinopoisk_id") val kinopoiskId:String? = null,
    @JsonProperty("age") val age: String? = null,
    @JsonProperty("is_series") val isSeries: String? = null,
    @JsonProperty("screenshot_uri") var screenshotUri: String? = null,
    @JsonProperty("genres_str") val genresStr: String? = null,
    @JsonProperty("cmd") var cmd: String? = null,
    @JsonProperty("added") val added: String? = null,
): Serializable{

    companion object{
        const val TYPE = "type"
        const val TYPE_VOD = "vod"
        const val TYPE_SERIES = "series"
        const val INTENT_VOD_TAG = "vod_intent"

        const val KINOPOISK_API = ""
    }

    var history: History? = null



    fun getType() : String{
        return if (isSeries == "1"){
            TYPE_SERIES
        } else {
            TYPE_VOD
        }
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Detail(
        @JsonProperty("backdrop_path")
        val backdropPath: String? = null,
        @JsonProperty("posterUrl")
        val kinBackdropPath:String? = null,
        @JsonProperty("shortDescription")
        val kinOverView:String? = null,
        @JsonProperty("overView")
        val overView:String? = null,
        @JsonProperty("poster_path")
        val posterPath:String? = null,
        @JsonProperty("posterUrlPreview")
        val kinPosterPath:String? = null,
        @JsonProperty("nameOriginal")
        val kinName:String? = null,
        @JsonProperty("title")
        val title:String? = null): Serializable{
        companion object{
            const val TYPE_TMDB = 0
            const val TYPE_KINOPOISK = 1

            const val TYPE_MOVIE = "movie"
            const val TYPE_SERIES = "tv"
        }
    }
}
