package io.github.benkoff.webrtcss.service;

import io.github.benkoff.webrtcss.domain.ProcessRoomDto;
import io.github.benkoff.webrtcss.domain.Room;
import io.github.benkoff.webrtcss.domain.RoomService;
import io.github.benkoff.webrtcss.util.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {
    private static final String REDIRECT = "redirect:/";

    private final RoomService roomService;
    private final Parser parser;

    public Model displayMainPage(Model model, final Long id, final String uuid) {
        model.addAttribute("id", id);
        model.addAttribute("rooms", roomService.getRooms());
        model.addAttribute("uuid", uuid);
        return model;
    }


    public Model processRoomSelection(Model model, ProcessRoomDto processRoomDto) {
        String sid = processRoomDto.getSid();
        String uuid = processRoomDto.getUuid();
        Optional<Long> optionalId = parser.parseId(sid);
        optionalId.ifPresent(id -> Optional.ofNullable(uuid).ifPresent(name -> roomService.addRoom(new Room(id))));

        return displayMainPage(model, optionalId.orElse(null), uuid);
    }


    public Model displaySelectedRoom(Model model, final String sid, final String uuid) {
        // redirect to main page if provided data is invalid
        //Model Model = new Model(REDIRECT);

        if (parser.parseId(sid).isPresent()) {
            Room room = roomService.findRoomByStringId(sid).orElse(null);
            if (room != null && uuid != null && !uuid.isEmpty()) {
                log.debug("User {} is going to join Room #{}", uuid, sid);
                // open the chat room
                //Model = new Model("chat_room", "id", sid);
                model.addAttribute("id", sid);
                model.addAttribute("uuid", uuid);
            }
        }
        return model;
    }


    public Model processRoomExit(Model model, final String sid, final String uuid) {
        if (sid != null && uuid != null) {
            log.debug("User {} has left Room #{}", uuid, sid);
            // implement any logic you need
        }
        // return new Model(REDIRECT);
        return model;
    }


    public Model requestRandomRoomNumber(Model model, final String uuid) {
        return this.displayMainPage(model, randomValue(), uuid);
    }

    private Long randomValue() {
        return ThreadLocalRandom.current().nextLong(0, 100);
    }
}
