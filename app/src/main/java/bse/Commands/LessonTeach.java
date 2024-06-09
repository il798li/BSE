package bse.Commands;

import bse.Data.Lesson;
import bse.Main;
import bse.Utility.JSONUtility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONObject;

import java.util.ArrayList;
public class LessonTeach {
    public static SlashCommandData slashCommandData () {
        SlashCommandData slashCommandData = Commands.slash ("teach", "Create a lesson for other people to learn.");
        OptionData subjectOptionData = new OptionData (OptionType.STRING, "subject", "The specific math subject your lesson teaches.", true);
        for (Main.Category category : Main.Category.values ()) {
            subjectOptionData.addChoice (category.name ().toLowerCase (), category.name ().toLowerCase ());
        }
        OptionData nameOptionData = new OptionData (OptionType.STRING, "name", "The name of your lesson.", true);
        slashCommandData.addOptions (subjectOptionData, nameOptionData);
        return slashCommandData;
    }

    public static void execute (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final User user = slashCommandInteractionEvent.getUser ();
        final long userID = user.getIdLong ();
        final boolean[] finished = {false};
        ArrayList <String> pages = new ArrayList <String> ();
        slashCommandInteractionEvent.reply ("_ _").queue (message -> {
            MessageBuilder messageBuilder = new MessageBuilder ();
            messageBuilder.setContent ("Please input each page of your lesson.");
            Button done = Button.primary ("done," + userID + "," + slashCommandInteractionEvent.getChannel ().getIdLong (), "Done");
            messageBuilder.setActionRows (ActionRow.of (done));
            message.editOriginal (messageBuilder.build ()).queue ();
        });
        JDA jda = slashCommandInteractionEvent.getJDA ();
        jda.addEventListener (new ListenerAdapter () {
            public void onMessageReceived (final MessageReceivedEvent messageReceivedEvent) {
                final long memberID = messageReceivedEvent.getMember ().getIdLong ();
                final long channelID = messageReceivedEvent.getChannel ().getIdLong ();
                if (memberID == userID && channelID == slashCommandInteractionEvent.getChannel ().getIdLong () && !finished[0]) {
                    pages.add (messageReceivedEvent.getMessage ().getContentDisplay ());
                    messageReceivedEvent.getMessage ().addReaction (Emoji.fromUnicode ("\u2705")).queue ();
                }
            }
        });
        OptionMapping optionMapping = slashCommandInteractionEvent.getOption ("name");
        String name = optionMapping.getAsString ();
        jda.addEventListener (new ListenerAdapter () {
            public void onButtonInteraction (final ButtonInteractionEvent buttonInteractionEvent) {
                JSONObject lessonsJSONObject = JSONUtility.load (JSONUtility.JSONFile.Lessons);
                final long memberID = buttonInteractionEvent.getMember ().getIdLong ();
                if (buttonInteractionEvent.getComponentId ().startsWith ("done")) {
                    final long channelID = buttonInteractionEvent.getChannel ().getIdLong ();
                    if (memberID == userID && channelID == slashCommandInteractionEvent.getChannel ().getIdLong ()) {
                        buttonInteractionEvent.reply ("Saving your lesson...").queue (message -> {
                            finished[0] = true;
                            Lesson lesson = new Lesson (userID, name, pages);
                            JSONObject jsonObject = JSONUtility.load (JSONUtility.JSONFile.Lessons);
                            JSONObject lessonJSONObject = new JSONObject ();
                            lessonJSONObject.put ("pages", lesson.pages ());
                            lessonJSONObject.put ("id", lesson.id ());
                            lessonJSONObject.put ("userID", userID);
                            lessonsJSONObject.put (name, lessonJSONObject);
                            JSONUtility.save (lessonsJSONObject, JSONUtility.JSONFile.Lessons);
                            message.editOriginal ("Successfully saved your lesson with this lesson ID:```" + lesson.id () + "```").queue ();
                        });
                    }
                }
            }
        });
    }
}
