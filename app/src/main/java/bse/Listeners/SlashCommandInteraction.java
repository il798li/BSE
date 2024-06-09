package bse.Listeners;

import bse.Commands.TestCreate;
import bse.Commands.LessonLearn;
import bse.Commands.LessonTeach;
import bse.Commands.TestTake;
import bse.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
public class SlashCommandInteraction extends ListenerAdapter {
    public void onSlashCommandInteraction (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final String name = slashCommandInteractionEvent.getName ();
        switch (name) {
            case "learn": {
                LessonLearn.execute (slashCommandInteractionEvent);
                break;
            }
            case "teach": {
                LessonTeach.execute (slashCommandInteractionEvent);
                break;
            }
            case "create_test": {
                TestCreate.execute (slashCommandInteractionEvent);
                break;
            }
            case "take_test": {
                TestTake.execute (slashCommandInteractionEvent);
                break;
            }
            default: {
                Main.debug (name);
            }
        }
    }
}
