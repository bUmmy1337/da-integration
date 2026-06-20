package net.bummy1337.testconsole;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.DonationAlertsIntegrateFactory;
import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.daintegrate.EventProcessor;
import net.bummy1337.daintegrate.listeners.DonationAlertsEventListener;
import net.bummy1337.dontaionalerts.DonationAlertsClient;

import java.io.*;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        BufferedReader reader = new BufferedReader(new FileReader("token.txt"));
        var token = reader.readLine();

        var source = DonationAlertsIntegrateFactory.create(
                "DonationAlerts/src/main/resources/",
                "settings.yaml",
                System.out::println);

        DonationAlertsIntegrate.configure(Constants.ModId, Constants.ModUrl)
                .registerHandler(new MessageHandlerTest())
                .registerHandler(new CommandHandlerTest());

        source.startListening();
        var listener = new DonationAlertsEventListener(new EventProcessor());
        try {
            var client = new DonationAlertsClient("https://socket.donationalerts.ru:443", listener);
            client.connect(token);

            while (true)
                Thread.sleep(1000);
        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
