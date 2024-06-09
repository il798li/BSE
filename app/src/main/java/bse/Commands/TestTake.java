package bse.Commands;

import bse.Data.Test;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
public class TestTake {
    public static SlashCommandData slashCommandData () {
        SlashCommandData slashCommandData = Commands.slash ("take_test", "Take a test to gauge your knowledge on a subject.");
        slashCommandData.addOption (OptionType.STRING, "test_id", "The ID of the test you want to take.");
        return slashCommandData;
    }

    public static void execute (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping testIDOptionMapping = slashCommandInteractionEvent.getOption ("test_id");
        final String testID = testIDOptionMapping.getAsString ();
        Test test = Test.get (testID);
        if (test == null) {
            slashCommandInteractionEvent.reply ("I could not find a test with this ID:```" + testID + "```").queue ();
            return;
        }
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        String[] responses = new String[test.problems ().length];
        final int[] index = {0};
        ArrayList <Long> received = new ArrayList <Long> ();
        slashCommandInteractionEvent.reply ("You are now taking the following test:```" + test.name () + "```").queue (message -> {
            String thisProblem = test.problems ()[index[0]];
            try {
                message.retrieveOriginal ().complete (true).reply (thisProblem).queue ();
            } catch (RateLimitedException | IllegalStateException illegalStateException) {
                slashCommandInteractionEvent.getChannel ().sendMessage (thisProblem).queue ();
            }
            jda.addEventListener (new ListenerAdapter () {
                public void onMessageReceived (final MessageReceivedEvent event) {
                    if (event.getChannel ().getIdLong () != slashCommandInteractionEvent.getChannel ().getIdLong () || event.getMember ().getIdLong () != slashCommandInteractionEvent.getUser ().getIdLong ()) {
                        return;
                    }
                    final Message answerMessage = event.getMessage ();
                    final long messageIDLong = answerMessage.getIdLong ();
                    if (received.contains (messageIDLong)) {
                        return;
                    } else {
                        received.add (messageIDLong);
                    }
                    final String response = answerMessage.getContentRaw ();
                    responses[index[0]] = response;
                    index[0]++;
                    if (index[0] >= responses.length) {
                        answerMessage.reply ("Grading your score...").queue (gradingMessage -> {
                            int[] score = test.grade (responses);
                            gradingMessage.editMessage ("Final score: " + score[0] + "/" + score[1] + " -> " + ( (int) ((score[0] * 100.0 / score[1]))) + "%").queue ();
                        });
                    } else {
                        answerMessage.addReaction (Emoji.fromUnicode ("\u2705")).queue ();
                        answerMessage.reply (test.problems ()[index[0]]).queue ();
                    }
                }
            });
        });
    }
}
