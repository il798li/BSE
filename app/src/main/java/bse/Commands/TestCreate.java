package bse.Commands;

import bse.Data.Test;
import bse.Main;
import bse.Utility.JSONUtility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
public class TestCreate {
    public static SlashCommandData slashCommandData () {
        SlashCommandData slashCommandData = Commands.slash ("create_test", "Create a test for others to guage their knowledge.");
        OptionData optionData = new OptionData (OptionType.STRING, "subject", "The subject your test is about.", true);
        for (Main.Category category : Main.Category.values ()) {
            optionData.addChoice (category.name ().toLowerCase (), category.name ().toLowerCase ());
        }
        slashCommandData.addOptions (optionData);
        slashCommandData.addOption (OptionType.STRING, "name", "The name of your test.", true);
        return slashCommandData;
    }

    public static void execute (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final User user = slashCommandInteractionEvent.getUser ();
        final long userID = user.getIdLong ();
        final MessageChannel messageChannel = slashCommandInteractionEvent.getChannel ();
        final long messageChannelID = messageChannel.getIdLong ();
        final Button[] buttons = {
            null,
            null
        };
        slashCommandInteractionEvent.reply ("_ _").queue (message -> {
            MessageBuilder messageBuilder = new MessageBuilder ();
            messageBuilder.setContent ("Use the buttons below to add questions to your test, then click `Done` when you have finished.");
            buttons[0] = Button.primary ("testdone," + userID + "," + messageChannelID, "Done");
            buttons[1] = Button.primary ("testadd" + userID + "," + messageChannelID, "Add Question");
            messageBuilder.setActionRows (ActionRow.of (buttons[1], buttons[0]));
            message.editOriginal (messageBuilder.build ()).queue ();
        });
        final ArrayList <String[]> questions = new ArrayList <String[]> ();
        final boolean[] typingQuestion = {false};
        final OptionMapping nameOptionMapping = slashCommandInteractionEvent.getOption ("name");
        final String testName = nameOptionMapping.getAsString ();
        final String[] thisQuestion = {null, null};
        jda.addEventListener (new ListenerAdapter () {
            public void onButtonInteraction (final ButtonInteractionEvent buttonInteractionEvent) {
                final Button button = buttonInteractionEvent.getButton ();
                final long buttonUserID = buttonInteractionEvent.getUser ().getIdLong ();
                final long buttonChannelID = buttonInteractionEvent.getChannel ().getIdLong ();
                if (userID != buttonUserID || buttonChannelID != messageChannelID) {
                    return;
                }
                if (button.equals (buttons[0])) { // save test
                    buttonInteractionEvent.reply ("Saving your test...").queue (message -> {
                        JSONObject testsJSONObject = JSONUtility.load (JSONUtility.JSONFile.Tests);
                        JSONObject testJSONObject = new JSONObject ();
                        Test test = new Test (testName, userID, questions);
                        testJSONObject.put ("id", test.id ());
                        testJSONObject.put ("userID", userID);
                        JSONArray questionsJSONArray = new JSONArray ();
                        for (String[] question : questions) {
                            JSONArray questionJSONArray = new JSONArray ();
                            questionJSONArray.put (question[0]);
                            questionJSONArray.put (question[1]);
                            questionsJSONArray.put (questionJSONArray);
                        }
                        testJSONObject.put ("questions", questionsJSONArray);
                        testsJSONObject.put (testName, testJSONObject);
                        JSONUtility.save (testsJSONObject, JSONUtility.JSONFile.Tests);
                        buttons[0] = buttons[0].withDisabled (true);
                        buttons[1] = buttons[1].withDisabled (true);
                        {
                            MessageBuilder messageBuilder = new MessageBuilder (buttonInteractionEvent.getMessage ());
                            messageBuilder.setActionRows (ActionRow.of (buttons[1].withDisabled (true), buttons[0].withDisabled (true)));
                            buttonInteractionEvent.getHook ().editOriginal (messageBuilder.build ()).queue ();
                        }
                        MessageBuilder messageBuilder = new MessageBuilder ();
                        messageBuilder.setContent ("Saved your test with this ID:```" + test.id () + "```");
                        message.editOriginal (messageBuilder.build ()).queue ();
                    });
                } else if (button.equals (buttons[1])) {
                    typingQuestion[0] = true; // add a question
                    buttonInteractionEvent.deferReply (true).queue ();
                    buttonInteractionEvent.getHook().sendMessage("Please input the problem :question: and solution :white_check_mark: in separate messages.").queue ();
                    jda.addEventListener (new ListenerAdapter () {
                        public void onMessageReceived (MessageReceivedEvent messageReceivedEvent) {
                            if (messageReceivedEvent.getMember ().getIdLong () != userID || messageReceivedEvent.getChannel ().getIdLong () != messageChannelID || !typingQuestion[0]) {
                                return;
                            }
                            // now i know that we must save the user's msg as an answer or question
                            final String content = messageReceivedEvent.getMessage ().getContentRaw ();
                            Main.debug ("Content: " + content);
                            if (thisQuestion[0] == null) {
                                thisQuestion[0] = content;
                                messageReceivedEvent.getMessage ().addReaction (Emoji.fromUnicode ("\u2753")).queue ();
                            } else if (!thisQuestion[0].equals (content)) {
                                thisQuestion[1] = content;
                                questions.add (thisQuestion.clone ());
                                for (int index = 0; index < thisQuestion.length; index++) {
                                    thisQuestion[index] = null;
                                }
                                typingQuestion[0] = false;
                                messageReceivedEvent.getMessage ().addReaction (Emoji.fromUnicode ("\u2705")).queue ();
                            }
                        }
                    });
                }
            }
        });
    }

    public static String simplifyAnswer(String answer) {
        String[] split = answer.split (" ");
        answer = "";
        for (String splitItem: split) {
            answer += splitItem;
        }
        return answer.toLowerCase ();
    }
}
