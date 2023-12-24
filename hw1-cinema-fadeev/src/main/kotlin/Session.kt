import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Session(val movie: Movie, val startTime: LocalDateTime, val totalSeats: Int) {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm")
    val tryStartTime = startTime.format(dateTimeFormatter)
    val id: Int = "$tryStartTime-${movie.id}".hashCode()
    val occupiedSeats = mutableListOf<Int>()
    val soldSeats = mutableListOf<Int>()

    fun isSeatOccupied(seat: Int): Boolean {
        return occupiedSeats.contains(seat)
    }

    fun markSeatOccupied(seat: Int) {
        occupiedSeats.add(seat)
    }

    fun markSeatVacant(seat: Int) {
        occupiedSeats.remove(seat)
        soldSeats.remove(seat)
    }

    fun isSeatSold(seat: Int) : Boolean {
        return soldSeats.contains(seat)
    }

    fun markSeatSold(seat: Int) {
        soldSeats.add(seat)
    }

}
