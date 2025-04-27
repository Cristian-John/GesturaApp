package com.example.gesturaapp;

import java.util.ArrayList;
import java.util.List;

public class LessonItemData {
    public static List<LessonItem> getItemsForCategory(String category) {
        List<LessonItem> items = new ArrayList<>();
        switch (category) {
            case "Alphabet":
                items.add(new LessonItem("A", "lettera"));
                items.add(new LessonItem("B", "letterb"));
                items.add(new LessonItem("C", "letterc"));
                items.add(new LessonItem("D", "dletter"));
                items.add(new LessonItem("E", "eletter"));
                items.add(new LessonItem("F", "fletter"));
                items.add(new LessonItem("G", "gletter"));
                items.add(new LessonItem("H", "hletter"));
                items.add(new LessonItem("I", "iletter"));
                items.add(new LessonItem("J", "jletter"));
                items.add(new LessonItem("K", "kletter"));
                items.add(new LessonItem("L", "lletter"));
                items.add(new LessonItem("M", "mletter"));
                items.add(new LessonItem("N", "nletter"));
                items.add(new LessonItem("Ñ", "enyeletter"));
                items.add(new LessonItem("NG", "ngletter"));
                items.add(new LessonItem("O", "oletter"));
                items.add(new LessonItem("P", "pletter"));
                items.add(new LessonItem("Q", "qletter"));
                items.add(new LessonItem("R", "rletter"));
                items.add(new LessonItem("S", "sletter"));
                items.add(new LessonItem("T", "tletter"));
                items.add(new LessonItem("U", "uletter"));
                items.add(new LessonItem("V", "vletter"));
                items.add(new LessonItem("W", "wletter"));
                items.add(new LessonItem("X", "xletter"));
                items.add(new LessonItem("Y", "yletter"));
                items.add(new LessonItem("Z", "zletter"));
                break;
            case "Animals":
                items.add(new LessonItem("Bear", "bear"));
                items.add(new LessonItem("Bird", "bird"));
                items.add(new LessonItem("Butterfly", "butterfly"));
                items.add(new LessonItem("Carabao", "carabao"));
                items.add(new LessonItem("Dog", "dog"));
                items.add(new LessonItem("Duck", "duck"));
                items.add(new LessonItem("Fish", "fish"));
                items.add(new LessonItem("Worm", "worm"));
                break;
            case "Colors":
                items.add(new LessonItem("Black", "black"));
                items.add(new LessonItem("Blue", "blue"));
                items.add(new LessonItem("Brown", "brown"));
                items.add(new LessonItem("Gray", "gray"));
                items.add(new LessonItem("Green", "green"));
                items.add(new LessonItem("Orange", "orange"));
                items.add(new LessonItem("Red", "red"));
                items.add(new LessonItem("Violet", "violet"));
                break;
            case "Emotions":
                items.add(new LessonItem("angry", "angry"));
                items.add(new LessonItem("Annoy", "annoy"));
                items.add(new LessonItem("Crazy", "crazy"));
                items.add(new LessonItem("Excite", "excite"));
                items.add(new LessonItem("Shame", "shame"));
                items.add(new LessonItem("Strong", "strong"));
                break;
            case "Greetings":
                items.add(new LessonItem("Good Afternoon", "goodafternoon"));
                items.add(new LessonItem("Good Evening", "goodevening"));
                items.add(new LessonItem("Good Morning", "goodmorning"));
                items.add(new LessonItem("Good Night", "goodnight"));
                items.add(new LessonItem("Good Bye", "goodbye"));
                items.add(new LessonItem("Hello", "hello"));
                items.add(new LessonItem("Hi", "hi"));
                items.add(new LessonItem("Thank You", "thankyou"));
                items.add(new LessonItem("Welcome", "welcome"));
                break;
            case "Numbers":
                items.add(new LessonItem("one to ten", "onetoten"));
                break;
            case "Questions":
                items.add(new LessonItem("How?", "how"));
                items.add(new LessonItem("How much?", "howmuch"));
                items.add(new LessonItem("What?", "what"));
                items.add(new LessonItem("When?", "when"));
                items.add(new LessonItem("Which", "which"));
                items.add(new LessonItem("Who?", "who"));
                items.add(new LessonItem("Why?", "why"));
                break;
            case "Foods":
                items.add(new LessonItem("Gatas", "gatas"));
                items.add(new LessonItem("Hotdog", "hotdog"));
                items.add(new LessonItem("Inumin/Drink", "inumin"));
                items.add(new LessonItem("Itlog", "itlog"));
                items.add(new LessonItem("Juice", "juice"));
                items.add(new LessonItem("Kanin", "kanin"));
                items.add(new LessonItem("Kape", "kape"));
                items.add(new LessonItem("Soda", "soda"));
                break;
            default:
                break;
        }
        return items;
    }
}
