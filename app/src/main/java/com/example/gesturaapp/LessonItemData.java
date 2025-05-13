package com.example.gesturaapp;

import java.util.ArrayList;
import java.util.List;

public class LessonItemData {
    public static List<LessonItem> getItemsForCategory(String category) {
        List<LessonItem> items = new ArrayList<>();
        switch (category) {
            case "Alphabet":
                items.add(new LessonItem("A", "aletter"));
                items.add(new LessonItem("B", "bletter"));
                items.add(new LessonItem("C", "cletter"));
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
                items.add(new LessonItem("Bear/Oso", "bear"));
                items.add(new LessonItem("Bird/Ibon", "bird"));
                items.add(new LessonItem("Butterfly/Paruparo", "butterfly"));
                items.add(new LessonItem("Carabao/Kalabaw", "carabao"));
                items.add(new LessonItem("Dog/Aso", "dog"));
                items.add(new LessonItem("Duck/Pato", "duck"));
                items.add(new LessonItem("Fish/Isda", "fish"));
                items.add(new LessonItem("Worm/Uod", "worm"));
                break;
            case "Colors":
                items.add(new LessonItem("Black/Itim", "black"));
                items.add(new LessonItem("Blue/Asul", "blue"));
                items.add(new LessonItem("Brown/Kayumanggi", "brown"));
                items.add(new LessonItem("Gray/Kulay Abo", "gray"));
                items.add(new LessonItem("Green/Berde", "green"));
                items.add(new LessonItem("Orange/Kahel", "orange"));
                items.add(new LessonItem("Red/Pula", "red"));
                items.add(new LessonItem("Silver/Pilak", "silver"));
                items.add(new LessonItem("Violet/Lila", "violet"));
                items.add(new LessonItem("White/Puti", "white"));
                items.add(new LessonItem("Yellow/Dilaw", "yellow"));
                break;
            case "Emotions":
                items.add(new LessonItem("Angry/Galit", "angry"));
                items.add(new LessonItem("Annoy/Nakakinis", "annoy"));
                items.add(new LessonItem("Crazy/Baliw", "crazy"));
                items.add(new LessonItem("Excite", "excite"));
                items.add(new LessonItem("Shame/Kahihiyan", "shame"));
                items.add(new LessonItem("Strong/Malakas", "strong"));
                break;
            case "Greetings":
                items.add(new LessonItem("Good Afternoon/Magandang Tanghali", "goodafternoon"));
                items.add(new LessonItem("Good Evening/Magandang Gabi", "goodevening"));
                items.add(new LessonItem("Good Morning/Magandang Umaga", "goodmorning"));
                items.add(new LessonItem("Good Night/Magandang Gabi", "goodnight"));
                items.add(new LessonItem("Good Bye/Paalam", "goodbye"));
                items.add(new LessonItem("Hello", "hello"));
                items.add(new LessonItem("Hi", "hi"));
                items.add(new LessonItem("Thank You/Salamat", "thankyou"));
                items.add(new LessonItem("Welcome/Walang Anuman", "welcome"));
                break;
            case "Numbers":
                items.add(new LessonItem("one to ten/ Isa - Sampu", "onetoten"));
                break;
            case "Questions":
                items.add(new LessonItem("How?/Paano?", "how"));
                items.add(new LessonItem("How much?/magkano?", "howmuch"));
                items.add(new LessonItem("What?/Ano?", "what"));
                items.add(new LessonItem("When?/Kailan?", "when"));
                items.add(new LessonItem("Which/Alin?", "which"));
                items.add(new LessonItem("Who?/Sino?", "who"));
                items.add(new LessonItem("Why?/Bakit?", "why"));
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
