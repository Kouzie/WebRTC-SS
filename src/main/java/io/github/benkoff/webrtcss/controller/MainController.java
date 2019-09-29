package io.github.benkoff.webrtcss.controller;

import io.github.benkoff.webrtcss.domain.ProcessRoomDto;
import io.github.benkoff.webrtcss.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
    private final MainService mainService;

    @GetMapping({"", "/", "/index", "/home", "/main"})
    public String displayMainPage(Model model, final Long id, final String uuid) {
        mainService.displayMainPage(model, id, uuid);
        return "main";
    }

    @PostMapping(value = "/room")
    public String processRoomSelection(Model model, @ModelAttribute ProcessRoomDto processRoomDto) {
        /*if (bindingResult.hasErrors()) {
            log.error("bindResult has error");
            return "redirect:/main";
        }*/
        mainService.processRoomSelection(model, processRoomDto);
        return "main";
    }

    @GetMapping("/room/{sid}/user/{uuid}")
    public String displaySelectedRoom(Model model, @PathVariable("sid") final String sid, @PathVariable("uuid") final String uuid) {
        mainService.displaySelectedRoom(model, sid, uuid);
        return "chat_room";
    }

    @GetMapping("/room/{sid}/user/{uuid}/exit")
    public String processRoomExit(Model model, @PathVariable("sid") final String sid, @PathVariable("uuid") final String uuid) {
        mainService.processRoomExit(model, sid, uuid);
        return "main";
    }

    @GetMapping("/room/random")
    public String requestRandomRoomNumber(Model model, @ModelAttribute("uuid") final String uuid) {
        mainService.requestRandomRoomNumber(model, uuid);
        return "main";
    }

    @GetMapping("/offer")
    public String displaySampleSdpOffer() {
        return "sdp_offer";
    }

    @GetMapping("/stream")
    public String displaySampleStreaming() {
        return "streaming";
    }
}
