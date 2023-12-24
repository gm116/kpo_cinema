import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Scanner

class Cinema {
    val dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm")
    private val dataFolderPath = System.getProperty("user.dir") + "/csv-database"
    private var movies = mutableListOf<Movie>()
    private var sessions = mutableListOf<Session>()
    private var soldTickets = mutableListOf<Ticket>()

    init {
        loadDataIfNeeded()
    }

    fun loadDataIfNeeded() {
        println("Хотите ли вы загрузить данные из файлов CSV? (yes/no)")
        val scanner = Scanner(System.`in`)
        val loadFromCsv = scanner.next().equals("yes", ignoreCase = true)

        if (loadFromCsv) {
            loadDataFromCsv(
                "$dataFolderPath/movies.csv",
                "$dataFolderPath/sessions.csv",
                "$dataFolderPath/sold-tickets.csv"
            )
        }
    }

    // Функции для работы с фильмами
    fun addMovie(movie: Movie) {
        movies.add(movie)
        saveMovies()
    }

    fun removeMovie(movie: Movie) {
        val sessionsToRemove = sessions.filter { it.movie == movie }
        sessions.removeAll(sessionsToRemove)
        soldTickets.removeAll { it.session.movie == movie }

        movies.remove(movie)
        saveMovies()
        saveSessions()
        saveSoldTickets()

        println("Фильм \"${movie.title}\" удален вместе со связанными сеансами и билетами.")
    }

    fun listMovies(): List<Movie> {
        return movies
    }

    // Функции для работы с сеансами
    fun addSession(session: Session) {
        sessions.add(session)
        saveSessions()
    }

    fun removeSession(session: Session) {
        soldTickets.removeAll { it.session == session }

        sessions.remove(session)
        saveSessions()
        saveSoldTickets()

        println("Сеанс для фильма \"${session.movie.title}\" (${session.startTime.format(dateTimeFormat)}) удален вместе с билетами.")
    }

    fun listSessions(): List<Session> {
        return sessions
    }

    // Функции для работы с билетами
    fun sellTicket(session: Session, seat: Int) {
        if (seat in 1..session.totalSeats && session.isSeatSold(seat)) {
            val ticket = Ticket(session, seat)
            soldTickets.add(ticket)
            session.markSeatSold(seat)
            saveSoldTickets()
            println("Билет на место $seat продан.")
        } else {
            println("Место $seat уже занято.")
        }
    }

    fun returnTicket(ticket: Ticket) {
        if (ticket.session.startTime.isAfter(LocalDateTime.now())) {
            ticket.session.markSeatVacant(ticket.seat)
            soldTickets.remove(ticket)
            saveSoldTickets()
            println("Билет на место ${ticket.seat} возвращен.")
        } else {
            println("Билет на место ${ticket.seat} нельзя вернуть, так как сеанс уже начался.")
        }
    }

    fun listSoldTickets(): List<Ticket> {
        return soldTickets
    }

    fun displaySoldSeats() {
        val totalSeats = 1..10
        val soldSeats = listSoldTickets().map { it.seat }
        val availableSeats = totalSeats.filter { it !in soldSeats }
        if (availableSeats.isNotEmpty()) {
            println("Свободные места: $availableSeats")
        } else {
            println("Все места проданы.")
        }
    }

    fun displaySeatsStatus(session: Session) {
        val occupiedSeats = session.occupiedSeats
        val totalSeats = 1..10 // Предположим, что всего 10 мест в зале
        val availableSeats = totalSeats.minus(occupiedSeats)

        println("Свободные места для сеанса ${session.movie.title} (${session.startTime.format(dateTimeFormat)}): $availableSeats")
        println("Занятые места для сеанса ${session.movie.title} (${session.startTime.format(dateTimeFormat)}): $occupiedSeats")
    }

    fun editMovie(movie: Movie, newTitle: String, newDuration: Int) {
        movies.remove(movie)
        val editedMovie = Movie(newTitle, newDuration)
        movies.add(editedMovie)
        saveMovies()
        println("Данные о фильме отредактированы.")
    }

    fun editSession(session: Session, newStartTime: LocalDateTime) {
        sessions.remove(session)
        val editedSession = Session(session.movie, newStartTime, 10)
        sessions.add(editedSession)
        saveSessions()
        println("Данные о сеансе отредактированы.")
    }

    fun markSeatsOccupied(session: Session, seats: List<Int>) {
        seats.forEach { seat ->
            if (!session.isSeatOccupied(seat)) {
                session.markSeatOccupied(seat)
                println("Место $seat занято.")
            } else {
                println("Место $seat уже занято.")
            }
        }
    }

    private fun saveMovies() {
        val file = File("$dataFolderPath/movies.csv")
        val existingMovies = loadMovies(file.path) // Загружаем существующие фильмы из файла
        val combinedMovies = (existingMovies + movies).distinctBy { it.title } // Объединяем и убираем дубликаты
        file.writeText(combinedMovies.joinToString("\n") { "${it.title},${it.duration}" })
    }

    private fun loadMovies(filePath: String): MutableList<Movie> {
        val file = File(filePath)
        val loadedMovies = mutableListOf<Movie>()
        if (file.exists()) {
            file.readLines().forEach {
                val (title, duration) = it.split(",")
                loadedMovies.add(Movie(title, duration.toInt()))
            }
        }
        return loadedMovies
    }

    private fun saveSessions() {
        val file = File("$dataFolderPath/sessions.csv")
        val existingSessions = loadSessions(file.path)
        val combinedSessions = (existingSessions + sessions).distinctBy { "${it.movie.title},${it.startTime}" }
        file.writeText(combinedSessions.joinToString("\n") { "${it.movie.title},${it.startTime}" })
    }

    private fun loadSessions(filePath: String): MutableList<Session> {
        val file = File(filePath)
        val loadedSessions = mutableListOf<Session>()
        if (file.exists()) {
            val existingMovies = loadMovies("$dataFolderPath/movies.csv")
            file.readLines().forEach {
                val (movieTitle, startTime) = it.split(",")
                val movie = existingMovies.find { movie -> movie.title == movieTitle }
                movie?.let { movie ->
                    loadedSessions.add(Session(movie, LocalDateTime.parse(startTime.format()), 10))
                }
            }
        }
        return loadedSessions
    }

    private fun saveSoldTickets() {
        val file = File("$dataFolderPath/sold-tickets.csv")
        val existingSoldTickets = loadSoldTickets(file.path)
        val combinedSoldTickets = (existingSoldTickets + soldTickets).distinctBy { "${it.session.id},${it.seat}" }
        file.writeText(combinedSoldTickets.joinToString("\n") { "${it.session.id},${it.seat}" })
    }

    private fun loadSoldTickets(filePath: String): MutableList<Ticket> {
        val file = File(filePath)
        val loadedSoldTickets = mutableListOf<Ticket>()
        if (file.exists()) {
            val existingSessions = loadSessions("$dataFolderPath/sessions.csv")
            file.readLines().forEach {
                val (sessionId, seat) = it.split(",")
                val session = existingSessions.find { session -> session.id == sessionId.toInt() }
                session?.let { session ->
                    loadedSoldTickets.add(Ticket(session, seat.toInt()))
                }
            }
        }
        return loadedSoldTickets
    }


    private fun loadDataFromCsv(moviesPath: String, sessionsPath: String, soldTicketsPath: String) {
        movies = loadMovies(moviesPath)
        sessions = loadSessions(sessionsPath)
        soldTickets = loadSoldTickets(soldTicketsPath)
    }

    fun saveDataToCsv() {
        saveMovies()
        saveSessions()
        saveSoldTickets()
    }


}