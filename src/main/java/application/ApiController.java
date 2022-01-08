package application;

import model.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import service.MessageService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class ApiController {
    @Autowired
    MessageService messageService;

    @GetMapping("/")
    public String root() {
        return "index";
    }

    // Gets messages
    @RequestMapping(value = "/populate", method = RequestMethod.GET)
    @ResponseBody
    List<MessageModel> getItems(HttpServletRequest request, HttpServletResponse response) {
        return messageService.getMessages();
    }

    //  Creates a new message
    @RequestMapping(value = "/purge", method = RequestMethod.GET)
    @ResponseBody
    String purgeMessages(HttpServletRequest request, HttpServletResponse response) {
        messageService.purge();
        return "Queue is purged";
    }

    //  Creates a new message
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    List<MessageModel>  addItems(HttpServletRequest request, HttpServletResponse response) {
        String user = request.getParameter("user");
        String message = request.getParameter("message");

        // generate the ID
        UUID uuid = UUID.randomUUID();
        String msgId = uuid.toString();

        MessageModel messageOb = new MessageModel();
        messageOb.setId(msgId);
        messageOb.setName(user);
        messageOb.setBody(message);

        messageService.processMessage(messageOb);
        return messageService.getMessages();
    }

    @GetMapping("/message")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", new MessageModel());
        return "message";
    }
}
