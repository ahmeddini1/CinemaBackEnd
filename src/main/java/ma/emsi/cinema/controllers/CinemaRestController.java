package ma.emsi.cinema.controllers;

import javassist.NotFoundException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ma.emsi.cinema.dao.FilmRepository;
import ma.emsi.cinema.dao.TicketRepository;
import ma.emsi.cinema.entities.Film;
import ma.emsi.cinema.entities.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@CrossOrigin("*")
public class CinemaRestController {
    @Autowired
    private FilmRepository filmRepository;
    @Autowired
    private TicketRepository ticketRepository;


    @GetMapping(path = "/imageFilm/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] image(@PathVariable(name = "id") Long id) throws IOException, NotFoundException {
        Optional<Film> film = filmRepository.findById(id);
        if (!film.isPresent()) {
            throw new NotFoundException("id="+id.toString()+"not found");
        }
        String photoName = film.get().getPhoto();
        File file = new File(System.getProperty("user.home") + "/cinema/images/" +photoName);
        Path path = Paths.get(file.toURI());
        return Files.readAllBytes(path);
    }

    @PostMapping(path = "/payerTickets")
    @Transactional
    public List<Ticket> payerTickets(@RequestBody TicketFrom ticketFrom)throws Exception{
        List<Ticket> tickets = new ArrayList<>();
        System.out.println(ticketFrom.toString());
        ticketFrom.getTicket_id().forEach(ticketId ->{
            Ticket ticket = ticketRepository.findById(ticketId).get();
            if (ticket.isReserve()) throw new IllegalArgumentException();

            ticket.setReserve(true);
            ticket.setNomClient(ticketFrom.getClientName());
            ticket.setCodePayement(ticketFrom.getCodePayment());
            ticketRepository.save(ticket);
            tickets.add(ticket);
        });

        return tickets;
    }

    @Data @NoArgsConstructor @ToString
    static class TicketFrom{
        private String clientName;
        private List<Long> ticket_id = new ArrayList<>();
        private Integer codePayment;
    }
}
