package bse.Commands;

import bse.Data.Lesson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LessonLearn {
    private SlashCommandData slashCommandData = slashCommandData ();

    public static SlashCommandData slashCommandData () {
        SlashCommandData slashCommandData = Commands.slash ("learn", "Learn a new lesson!");
        slashCommandData.addOption (OptionType.STRING, "lesson_id", "The ID of the lesson you want to learn.", true);
        return slashCommandData;
    }

    public static void execute (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        OptionMapping optionMapping = slashCommandInteractionEvent.getOption ("lesson_id");
        String lessonID = optionMapping.getAsString ();
        Lesson lesson = Lesson.get (lessonID);
        User user = slashCommandInteractionEvent.getUser ();
        long userID = user.getIdLong ();
        if (lesson == null) {
            slashCommandInteractionEvent.reply ("I could not find a lesson with this ID:```" + lessonID + "```").queue ();
            return;
        }

        int page = 1;
        final String[] pages = lesson.pages ();
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        Button previous = Button.primary (userID + "," + lessonID + "," + (page - 1), "<");
        Button next = Button.primary (userID + "," + lessonID + "," + (page + 1), ">");
        ActionRow actionRow = ActionRow.of (previous, next);

        jda.addEventListener (new ListenerAdapter () {
            public void onButtonInteraction (ButtonInteractionEvent buttonInteractionEvent) {
                if (buttonInteractionEvent.getComponentId ().startsWith (userID + "," + lessonID + ",")) {
                    String[] info = buttonInteractionEvent.getComponentId ().split (",");
                    String pageString = info[info.length - 1];
                    int page = Integer.parseInt (pageString);
                    Lesson lesson = Lesson.get(info[1]);
                    Button previous = Button.primary (userID + "," + lessonID + "," + (page - 1), "<");
                    Button next = Button.primary (userID + "," + lessonID + "," + (page + 1), ">");
                    MessageBuilder messageBuilder = new MessageBuilder ();
                    messageBuilder.setContent (lesson.pages()[page - 1]);
                    messageBuilder.setActionRows (ActionRow.of (previous, next));
                    buttonInteractionEvent.editMessage (messageBuilder.build ()).queue ();
                }
            }
        });
        slashCommandInteractionEvent.reply ("_ _").queue (message -> {
            MessageBuilder messageBuilder = new MessageBuilder ();

            messageBuilder.setContent (pages[page - 1]);
            messageBuilder.setActionRows (actionRow);
            message.editOriginal (messageBuilder.build ()).queue ();
        });
    }

}
