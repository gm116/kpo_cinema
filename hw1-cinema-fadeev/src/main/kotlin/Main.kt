import java.time.LocalDateTime
import java.util.Scanner
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.InputMismatchException
import kotlin.system.exitProcess

fun main() {
    val cinema = Cinema()
    val scanner = Scanner(System.`in`)

    while (true) {
        try {
            printMenu(cinema, scanner)
        } catch (e: InputMismatchException) {
            println("Неверный ввод.")
            scanner.nextLine()
        }

    }
}

private fun printMenu(cinema: Cinema, scanner: Scanner) {
    val dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm")

    println("Меню:")
    println("1. Добавить фильм")
    println("2. Удалить фильм")
    println("3. Список фильмов")
    println("4. Добавить сеанс")
    println("5. Удалить сеанс")
    println("6. Список сеансов")
    println("7. Продать билет на сеанс")
    println("8. Вернуть билет")
    println("9. Список проданных билетов")
    println("10. Отобразить состояние мест для сеанса")
    println("11. Редактировать фильм или сеанс")
    println("12. Отметить места как занятые")
    println("0. Выход")

    when (scanner.nextInt()) {
        1 -> {
            println("Введите название фильма:")
            val title = readlnOrNull() ?: ""
            println("Введите продолжительность фильма (в минутах):")
            try {
                val duration = scanner.nextInt()
                val movie = Movie(title, duration)
                cinema.addMovie(movie)
                println("Фильм \"$title\" добавлен.")
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        2 -> {
            if (cinema.listMovies().isEmpty()) {
                println("Фильмов не найдено")
                return
            }

            println("Выберите фильм для удаления:")
            cinema.listMovies().forEachIndexed { index, movie ->
                println("$index. ${movie.title}")
            }

            println("Введите номер фильма для удаления:")
            try {
                val selectedIndex = scanner.nextInt()
                if (selectedIndex in 0..<cinema.listMovies().size) {
                    val movieToRemove = cinema.listMovies()[selectedIndex]
                    cinema.removeMovie(movieToRemove)
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }


        }

        3 -> {
            if (cinema.listMovies().isEmpty()) {
                println("Фильмов не найдено")
                return
            }
            println("Список фильмов:")
            cinema.listMovies().forEach { println(it.title) }
        }

        4 -> {
            println("Выберите фильм для добавления сеанса:")
            cinema.listMovies().forEachIndexed { index, movie ->
                println("$index. ${movie.title}")
            }
            try {
                val movieIndex = scanner.nextInt()
                if (movieIndex in 0..<cinema.listMovies().size) {
                    val selectedMovie = cinema.listMovies()[movieIndex]
                    println("Введите дату и время начала сеанса (дд-ММ-гггг-ЧЧ-мм):")
                    try {
                        val startTime = LocalDateTime.parse(scanner.next(), dateTimeFormat)
                        val session = Session(selectedMovie, startTime, 10)
                        cinema.addSession(session)
                    } catch (e: DateTimeParseException) {
                        println("Ошибка парсинга даты и времени. Убедитесь, что введенная строка соответствует формату (дд-ММ-гггг-ЧЧ-мм).")
                        return
                    }
                    println("Сеанс для фильма \"${selectedMovie.title}\" добавлен.")
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        5 -> {
            if (cinema.listSessions().isEmpty()) {
                println("Сеансов не найдено")
                return
            }
            println("Выберите сеанс для удаления:")
            cinema.listSessions().forEachIndexed { index, session ->
                println("$index. ${session.movie.title} (${session.startTime.format(dateTimeFormat)})")
            }
            try {
                val sessionIndex = scanner.nextInt()
                if (sessionIndex in 0..<cinema.listSessions().size) {
                    val selectedSession = cinema.listSessions()[sessionIndex]
                    cinema.removeSession(selectedSession)
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        6 -> {
            if (cinema.listSessions().isEmpty()) {
                println("Сеансов не найдено")
                return
            }
            println("Список сеансов:")
            cinema.listSessions().forEach { println("${it.movie.title} (${it.startTime.format(dateTimeFormat)})") }
        }

        7 -> {

            if (cinema.listSessions().isEmpty()) {
                println("Сеансов не найдено")
                return
            }

            println("Выберите сеанс для продажи билета:")
            cinema.listSessions().forEachIndexed { index, session ->
                println("$index. ${session.movie.title} (${session.startTime.format(dateTimeFormat)})")
            }
            try {
                val sessionIndex = scanner.nextInt()
                if (sessionIndex in 0..<cinema.listSessions().size) {
                    val selectedSession = cinema.listSessions()[sessionIndex]
                    cinema.displaySoldSeats()
                    println("Введите номер места:")
                    val seat = scanner.nextInt()
                    cinema.sellTicket(selectedSession, seat)
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        8 -> {
            val soldTickets = cinema.listSoldTickets()
            if (soldTickets.isEmpty()) {
                println("Проданных билетов не найдено")
                return
            }

            println("Список проданных билетов:")
            soldTickets.forEachIndexed { index, ticket ->
                println("$index. ${ticket.session.movie.title} (${ticket.session.startTime}), Место: ${ticket.seat}")
            }

            println("Введите номер билета для возврата:")
            try {
                val ticketNumber = scanner.nextInt()

                if (ticketNumber in soldTickets.indices) {
                    val selectedTicket = soldTickets[ticketNumber]
                    cinema.returnTicket(selectedTicket)
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        9 -> {
            if (cinema.listSoldTickets().isEmpty()) {
                println("Проданных билетов не найдено")
                return
            }
            println("Список проданных билетов:")
            cinema.listSoldTickets().forEachIndexed { index, ticket ->
                println("$index. ${ticket.session.movie.title} (${ticket.session.startTime.format(dateTimeFormat)}), Место: ${ticket.seat}")
            }
        }

        10 -> {
            if (cinema.listSessions().isEmpty()) {
                println("Сеансов не найдено")
                return
            }
            println("Выберите сеанс для отображения состояния мест:")
            cinema.listSessions().forEachIndexed { index, session ->
                println("$index. ${session.movie.title} (${session.startTime.format(dateTimeFormat)})")
            }
            try {
                val sessionIndex = scanner.nextInt()
                if (sessionIndex in 0..<cinema.listSessions().size) {
                    val selectedSession = cinema.listSessions()[sessionIndex]
                    cinema.displaySeatsStatus(selectedSession)
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        11 -> {
            println("Выберите объект для редактирования:")
            println("1. Фильм")
            println("2. Сеанс")
            when (scanner.nextInt()) {
                1 -> {
                    if (cinema.listMovies().isEmpty()) {
                        println("Фильмов не найдено")
                        return
                    }
                    println("Выберите фильм для редактирования:")
                    cinema.listMovies().forEachIndexed { index, movie ->
                        println("$index. ${movie.title}")
                    }
                    try {
                        val movieIndex = scanner.nextInt()
                        if (movieIndex in 0..<cinema.listMovies().size) {
                            val selectedMovie = cinema.listMovies()[movieIndex]
                            println("Введите новое название фильма:")
                            val newTitle = readlnOrNull() ?: ""
                            println("Введите новую продолжительность фильма (в минутах):")
                            val newDuration = scanner.nextInt()
                            cinema.editMovie(selectedMovie, newTitle, newDuration)
                        } else {
                            println("Некорректный выбор.")
                        }
                    } catch (e: InputMismatchException) {
                        println("Неверный ввод.")
                        scanner.nextLine()
                        return
                    }

                }

                2 -> {
                    if (cinema.listSessions().isEmpty()) {
                        println("Сеансов не найдено")
                        return
                    }
                    println("Выберите сеанс для редактирования:")
                    cinema.listSessions().forEachIndexed { index, session ->
                        println("$index. ${session.movie.title} (${session.startTime.format(dateTimeFormat)})")
                    }
                    try {
                        val sessionIndex = scanner.nextInt()
                        if (sessionIndex in 0..<cinema.listSessions().size) {
                            val selectedSession = cinema.listSessions()[sessionIndex]
                            println("Введите дату и время начала сеанса (дд-ММ-гггг-ЧЧ-мм):")
                            try {
                                val newStartTime = LocalDateTime.parse(scanner.next(), dateTimeFormat)
                                cinema.editSession(selectedSession, newStartTime)
                            } catch (e: DateTimeParseException) {
                                println("Ошибка парсинга даты и времени. Убедитесь, что введенная строка соответствует формату (дд-ММ-гггг-ЧЧ-мм).")
                                return
                            }

                        } else {
                            println("Некорректный выбор.")
                        }
                    } catch (e: InputMismatchException) {
                        println("Неверный ввод.")
                        scanner.nextLine()
                        return
                    }

                }

                else -> println("Некорректный выбор.")
            }
        }

        12 -> {
            if (cinema.listSessions().isEmpty()) {
                println("Сеансов не найдено")
                return
            }
            println("Выберите сеанс для отметки мест как занятых:")
            cinema.listSessions().forEachIndexed { index, session ->
                println("$index. ${session.movie.title} (${session.startTime.format(dateTimeFormat)})")
            }
            try {
                val sessionIndex = scanner.nextInt()
                if (sessionIndex in 0..<cinema.listSessions().size) {
                    val selectedSession = cinema.listSessions()[sessionIndex]
                    println("Введите номера мест через запятую:")
                    val seatsInput = scanner.next()
                    val seats = seatsInput.split(",").map { it.toInt() }
                    cinema.markSeatsOccupied(selectedSession, seats)
                } else {
                    println("Некорректный выбор.")
                }
            } catch (e: InputMismatchException) {
                println("Неверный ввод.")
                scanner.nextLine()
                return
            }

        }

        0 -> {
            cinema.saveDataToCsv() // Сохранение данных при выходе
            println("Спасибо за использование приложения. До свидания!")
            exitProcess(0)
        }

        else -> println("Некорректный выбор.")
    }
}