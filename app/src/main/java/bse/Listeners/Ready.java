package bse.Listeners;

import bse.Commands.TestCreate;
import bse.Commands.LessonLearn;
import bse.Commands.LessonTeach;
import bse.Commands.TestTake;
import bse.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
public class Ready extends ListenerAdapter {
    public void onReady (ReadyEvent readyEvent) {
        final JDA jda = readyEvent.getJDA ();
        final User user = jda.getSelfUser ();
        final String name = user.getName ();
        Main.debug (name + " is ready!");

        final Guild guild = jda.getGuildById (774023174131679242L);
        CommandListUpdateAction commandListUpdateAction = guild.updateCommands ();
        commandListUpdateAction.addCommands (LessonLearn.slashCommandData());
        commandListUpdateAction.addCommands (LessonTeach.slashCommandData ());
        commandListUpdateAction.addCommands (TestCreate.slashCommandData ());
        commandListUpdateAction.addCommands (TestTake.slashCommandData ());
        commandListUpdateAction.queue ();
    }
}
