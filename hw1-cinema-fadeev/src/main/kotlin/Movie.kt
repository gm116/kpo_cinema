data class Movie(val title: String, val duration: Int) {
    val id: Int = title.hashCode() // Добавим идентификатор фильма
}