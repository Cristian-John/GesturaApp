package com.example.gesturaapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.airbnb.lottie.LottieAnimationView;


public class AssessmentFragment extends Fragment {

    private TextView questionTextView;
    private RadioGroup choicesGroup;
    private Button submitButton;
    private VideoView videoView;
    private TextView scoreTextView;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String selectedSubject = "Random";

    public AssessmentFragment() {}

   // String subject = getArguments() != null ? getArguments().getString("subject", "Random") : "Random";
   private String subject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assessment, container, false);
        subject = getArguments() != null ? getArguments().getString("subject", "Random") : "Random";

        questionTextView = view.findViewById(R.id.text_question);
        choicesGroup = view.findViewById(R.id.choices_group);
        submitButton = view.findViewById(R.id.submit_button);
        videoView = view.findViewById(R.id.questionVideoview);
        scoreTextView = view.findViewById(R.id.score_text);

        submitButton.setEnabled(false); // start disabled

        if (getArguments() != null) {
            selectedSubject = getArguments().getString("subject", "Random");
        }

        questionList = getQuestionsForSubject(selectedSubject);
        Collections.shuffle(questionList);
        showQuestion();

        submitButton.setOnClickListener(v -> checkAnswer());

        return view;
    }

    private void showQuestion() {
        if (currentQuestionIndex >= questionList.size()) {
            endQuiz();
            return;
        }

        Question currentQuestion = questionList.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.getQuestionText());

        // Generate RadioButtons dynamically
        choicesGroup.removeAllViews();
        List<String> choices = currentQuestion.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(choices.get(i));
            radioButton.setId(i);
            choicesGroup.addView(radioButton);
        }

        // ⬇️ Add this: disable button until choice is picked
        submitButton.setEnabled(false);
        choicesGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                submitButton.setEnabled(true);
            }
        });

        // Handle video
        if (currentQuestion.getVideoUri() != null && !currentQuestion.getVideoUri().isEmpty()) {
            videoView.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(currentQuestion.getVideoUri());
            videoView.setVideoURI(videoUri);

            MediaController mediaController = new MediaController(getContext());
            mediaController.setAnchorView(videoView);
            //videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true); // optional: loop video
                videoView.start();   // autoplay
            });
        } else {
            videoView.setVisibility(View.GONE);
        }
    }



    private void checkAnswer() {
        int selectedId = choicesGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        Question currentQuestion = questionList.get(currentQuestionIndex);
        if (selectedId == currentQuestion.getCorrectAnswerIndex()) {
            score++;
        }

        // ⬅️ Update the score text here
        scoreTextView.setText("Score: " + score);

        currentQuestionIndex++;

        // 🔑 Prevent loading another question after finishing
        if (currentQuestionIndex < questionList.size()) {
            submitButton.setEnabled(false); // ⬅️ disable until user picks again
            showQuestion();
        } else {
            endQuiz();
        }
    }



    private void endQuiz() {
        boolean isPassed = score >= 7;

        // Build the result string
        String result = "Your score: " + score + "/" + questionList.size() +
                (isPassed ? "\nYou passed!" : "\nYou failed.");

        // Inflate the custom dialog view with animation
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_quiz_result, null);

        TextView resultTextView = dialogView.findViewById(R.id.resultTextView);
        LottieAnimationView animationView = dialogView.findViewById(R.id.animationView);

        resultTextView.setText(result);

        // Choose animation depending on result
        if (isPassed) {
            animationView.setAnimation(R.raw.passed); // 🎉 success Lottie file
            animationView.playAnimation();
        } else {
            animationView.setAnimation(R.raw.fail); // ❌ red x-mark or shake animation
            animationView.playAnimation();
        }

        // Build AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        builder.setPositiveButton("Retry", (dialog, which) -> {
            currentQuestionIndex = 0;
            score = 0;
            showQuestion();
            scoreTextView.setText("Score: 0");
            submitButton.setEnabled(true);
        });

        builder.setNegativeButton("Back to Subjects", (dialog, which) -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        builder.setCancelable(false);
        builder.show();

        // Stop video playback when quiz ends
        videoView.stopPlayback();

        // Save result to database
        ZYQuizDatabaseHelper dbHelper = new ZYQuizDatabaseHelper(requireContext());
        String dateTaken = new java.text.SimpleDateFormat("MMM dd, yyyy h:mm a", java.util.Locale.getDefault())
                .format(new java.util.Date());
        dbHelper.insertQuizResult("Quiz", subject, score, questionList.size(), dateTaken);
    }



    private List<Question> getQuestionsForSubject(String subject) {
        List<Question> all = new ArrayList<>();

// ALPHABET
        List<Question> alphabet = new ArrayList<>();

        String videoPathA = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.aletter;
        String videoPathB = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.bletter;
        String videoPathC = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.cletter;
        String videoPathD = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.dletter;
        String videoPathE = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.eletter;
        String videoPathF = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.fletter;
        String videoPathG = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.gletter;
        String videoPathH = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.hletter;
        String videoPathI = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.iletter;
        String videoPathJ = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.jletter;

        alphabet.add(new Question("What is the sign for 'A'?", List.of("Closed fist", "Palm open", "Peace sign", "Thumbs up"), 0, videoPathA));
        alphabet.add(new Question("Which sign is shown with palm open?", List.of("B", "A", "F", "D"), 0, videoPathB));
        alphabet.add(new Question("Which letter is signed with a curled hand like a hook?", List.of("C", "H", "J", "L"), 0, videoPathC));
        alphabet.add(new Question("The sign with one finger pointing straight up is?", List.of("B", "D", "A", "F"), 1, videoPathD));
        alphabet.add(new Question("Which letter shows all fingers touching the thumb?", List.of("E", "I", "N", "K"), 0, videoPathE));
        alphabet.add(new Question("The sign with thumb and index making a circle is?", List.of("F", "C", "G", "B"), 0, videoPathF));
        alphabet.add(new Question("Which letter is signed with the index and thumb pointing sideways?", List.of("L", "V", "G", "R"), 2, videoPathG));
        alphabet.add(new Question("Which sign uses two fingers raised like a salute?", List.of("H", "D", "N", "K"), 0, videoPathH));
        alphabet.add(new Question("Which letter is the little finger up?", List.of("J", "I", "T", "F"), 1, videoPathI));
        alphabet.add(new Question("Which letter is signed by tracing a 'J' in the air?", List.of("H", "C", "J", "L"), 2, videoPathJ));

/// ANIMALS
        List<Question> animals = new ArrayList<>();

        String videoPathBear = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.bear;
        String videoPathBird = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.bird;
        String videoPathButterfly = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.butterfly;
        String videoPathCarabao = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.carabao;
        String videoPathDog = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.dog;
        String videoPathDuck = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.duck;
        String videoPathFish = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.fish;
        String videoPathWorm = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.worm;

        animals.add(new Question("Which animal sign shows claws scratching?", List.of("Bear/Oso", "Bird/Ibon", "Butterfly/Paruparo", "Dog/Aso"), 0, videoPathBear));
        animals.add(new Question("Which sign flaps hands like wings?", List.of("Butterfly/Paruparo", "Carabao/Kalabaw", "Bird/Ibon", "Fish/Isda"), 2, videoPathBird));
        animals.add(new Question("Which animal is shown by crossing thumbs and flapping fingers?", List.of("Butterfly/Paruparo", "Duck/Pato", "Worm/Uod", "Carabao/Kalabaw"), 0, videoPathButterfly));
        animals.add(new Question("Which sign shows two horns with the hands?", List.of("Dog/Aso", "Fish/Isda", "Carabao/Kalabaw", "Duck/Pato"), 2, videoPathCarabao));
        animals.add(new Question("Which animal is shown by panting or wagging hand?", List.of("Bird/Ibon", "Dog/Aso", "Bear/Oso", "Butterfly/Paruparo"), 1, videoPathDog));
        animals.add(new Question("Which sign uses a beak-like hand opening and closing?", List.of("Duck/Pato", "Worm/Uod", "Fish/Isda", "Bird/Ibon"), 0, videoPathDuck));
        animals.add(new Question("Which animal is shown with a hand swimming motion?", List.of("Fish/Isda", "Dog/Aso", "Carabao/Kalabaw", "Bear/Oso"), 0, videoPathFish));
        animals.add(new Question("Which sign is shown by wiggling the index finger like crawling?", List.of("Bird/Ibon", "Worm/Uod", "Duck/Pato", "Fish/Isda"), 1, videoPathWorm));
        animals.add(new Question("Which animal has horns and is used in farming?", List.of("Dog/Aso", "Carabao/Kalabaw", "Butterfly/Paruparo", "Bird/Ibon"), 1, videoPathCarabao));
        animals.add(new Question("Which two animals both use hand flapping?", List.of("Bird/Ibon and Butterfly/Paruparo", "Dog/Aso and Fish/Isda", "Bear/Oso and Carabao/Kalabaw", "Duck/Pato and Worm/Uod"), 0, videoPathBird));

// COLORS
        List<Question> colors = new ArrayList<>();

        String videoPathBlack = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.black;
        String videoPathBlue = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.blue;
        String videoPathBrown = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.brown;
        String videoPathGray = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.gray;
        String videoPathGreen = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.green;
        String videoPathOrange = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.orange;
        String videoPathRed = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.red;
        String videoPathSilver = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.silver;
        String videoPathViolet = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.violet;
        String videoPathWhite = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.white;
        String videoPathYellow = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.yellow;

        colors.add(new Question("Which sign brushes across the forehead?", List.of("Black/Itim", "Red/Pula", "Blue/Asul", "Green/Berde"), 0, videoPathBlack));
        colors.add(new Question("Which color is signed by shaking 'B' near the chin?", List.of("Yellow/Dilaw", "Blue/Asul", "White/Puti", "Brown/Kayumanggi"), 1, videoPathBlue));
        colors.add(new Question("Which color is signed by rubbing the cheek?", List.of("Brown/Kayumanggi", "Gray/Kulay Abo", "Violet/Lila", "Orange/Kahel"), 0, videoPathBrown));
        colors.add(new Question("Which sign mixes black and white together?", List.of("Silver/Pilak", "Gray/Kulay Abo", "Blue/Asul", "Orange/Kahel"), 1, videoPathGray));
        colors.add(new Question("Which sign shakes a 'G' hand by the chin?", List.of("Green/Berde", "Brown/Kayumanggi", "White/Puti", "Red/Pula"), 0, videoPathGreen));
        colors.add(new Question("Which color is signed by squeezing at the chin?", List.of("Orange/Kahel", "Black/Itim", "Violet/Lila", "Gray/Kulay Abo"), 0, videoPathOrange));
        colors.add(new Question("Which color is signed by brushing lips?", List.of("Red/Pula", "Yellow/Dilaw", "Blue/Asul", "White/Puti"), 0, videoPathRed));
        colors.add(new Question("Which sign is like brushing a ring finger?", List.of("Yellow/Dilaw", "Silver/Pilak", "Brown/Kayumanggi", "Green/Berde"), 1, videoPathSilver));
        colors.add(new Question("Which sign shakes a 'V' by the chin?", List.of("Blue/Asul", "Violet/Lila", "Orange/Kahel", "Gray/Kulay Abo"), 1, videoPathViolet));
        colors.add(new Question("Which sign pulls from chest outward?", List.of("White/Puti", "Yellow/Dilaw", "Green/Berde", "Black/Itim"), 0, videoPathWhite));

// EMOTIONS
        List<Question> emotions = new ArrayList<>();

        String videoPathAngry = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.angry;
        String videoPathAnnoy = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.annoy;
        String videoPathCrazy = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.crazy;
        String videoPathExcite = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.excite;
        String videoPathShame = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.shame;
        String videoPathStrong = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.strong;

        emotions.add(new Question("Which sign circles the fist on the chest?", List.of("Shame", "Sorry", "Crazy", "Angry"), 1, videoPathAnnoy)); // Sorry
        emotions.add(new Question("Which sign uses a claw hand at the face?", List.of("Excited", "Strong", "Happy", "Angry/Galit"), 3, videoPathAngry)); // Angry
        emotions.add(new Question("Which emotion sign circles near the temple?", List.of("Crazy/Baliw", "Shame", "Annoy", "Strong"), 0, videoPathCrazy)); // Crazy
        emotions.add(new Question("Which is the sign for Excite/Nasasabik?", List.of("Palm circles chest", "Arms flex", "Finger wiggles nose", "Hands brush chest alternately"), 3, videoPathExcite)); // Excite
        emotions.add(new Question("Which emotion shows strength by flexing arms?", List.of("Shame", "Happy", "Strong/Malakas", "Excited"), 2, videoPathStrong)); // Strong
        emotions.add(new Question("Which emotion is opposite of Happy?", List.of("Strong", "Excited", "Angry/Galit", "Crazy"), 2, videoPathAngry)); // Angry
        emotions.add(new Question("Which emotion is linked with embarrassment?", List.of("Strong", "Shame/Kahihiyan", "Angry", "Annoy"), 1, videoPathShame)); // Shame
        emotions.add(new Question("Which emotion is a positive feeling?", List.of("Excite/Nasasabik", "Angry/Galit", "Annoy/Nakakainis", "Crazy/Baliw"), 0, videoPathExcite)); // Excite
        emotions.add(new Question("Which sign brushes hand down the face?", List.of("Crazy/Baliw", "Shame/Kahihiyan", "Strong/Malakas", "Angry/Galit"), 1, videoPathShame)); // Shame
        emotions.add(new Question("Which two emotions are negative?", List.of("Excited and Strong", "Happy and Shame", "Crazy and Strong", "Annoy and Angry"), 3, videoPathAnnoy)); // Annoy & Angry

// GREETINGS
        List<Question> greetings = new ArrayList<>();

        String videoPathGoodMorning = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.goodmorning;
        String videoPathGoodAfternoon = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.goodafternoon;
        String videoPathGoodEvening = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.goodevening;
        String videoPathGoodNight = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.goodnight;
        String videoPathGoodbye = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.goodbye;
        String videoPathHi = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.hi;
        String videoPathThankYou = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.thankyou;
        String videoPathWelcome = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.welcome;

        greetings.add(new Question("Which greeting uses a wave from the forehead?", List.of("Welcome", "Thank You", "Hi", "Goodbye"), 2, videoPathHi)); // Hi
        greetings.add(new Question("Which greeting is shown by hand moving from chin outward?", List.of("Good Night", "Welcome", "Goodbye", "Thank You/Salamat"), 3, videoPathThankYou)); // Thank You
        greetings.add(new Question("Which greeting is for the evening?", List.of("Good Morning", "Good Night", "Good Afternoon", "Good Evening/Magandang Gabi"), 3, videoPathGoodEvening)); // Good Evening
        greetings.add(new Question("Which greeting is shown with a simple wave?", List.of("Welcome", "Thank You", "Goodbye", "Hi"), 3, videoPathHi)); // Hi
        greetings.add(new Question("Which greeting is for leaving?", List.of("Good Bye/Paalam", "Hi", "Welcome", "Thank You"), 0, videoPathGoodbye)); // Goodbye
        greetings.add(new Question("Which greeting is paired with 'Magandang Umaga'?", List.of("Good Morning", "Good Evening", "Good Night", "Good Afternoon"), 0, videoPathGoodMorning)); // Good Morning
        greetings.add(new Question("Which greeting pulls the hand toward the chest?", List.of("Thank You", "Welcome/Walang Anuman", "Goodbye", "Good Afternoon"), 1, videoPathWelcome)); // Welcome
        greetings.add(new Question("Which two greetings are both for night time?", List.of("Good Evening and Good Night", "Good Morning and Good Afternoon", "Hi and Goodbye", "Welcome and Thank You"), 0, videoPathGoodNight)); // Evening & Night
        greetings.add(new Question("Which greeting uses a fist circling the chest?", List.of("Please", "Sorry", "Good Afternoon", "Good Night"), 2, videoPathGoodAfternoon)); // Good Afternoon
        greetings.add(new Question("Which greeting means 'Salamat'?", List.of("Welcome", "Goodbye", "Thank You", "Hi"), 2, videoPathThankYou)); // Thank You

// NUMBERS
        List<Question> numbers = new ArrayList<>();

        String videoPathNumbers = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.onetoten;

        numbers.add(new Question("How is the number '1' signed?", List.of("Index finger up", "Thumb up", "Peace sign", "Palm open"), 0, videoPathNumbers)); // 1
        numbers.add(new Question("How is the number '2' signed?", List.of("Closed fist", "Peace sign", "Three fingers", "Palm open"), 1, videoPathNumbers)); // 2
        numbers.add(new Question("How is the number '3' signed?", List.of("Four fingers", "Thumb + two fingers", "Peace sign", "Palm open"), 1, videoPathNumbers)); // 3
        numbers.add(new Question("How is the number '4' signed?", List.of("Peace sign", "Thumb up", "Four fingers up", "Closed fist"), 2, videoPathNumbers)); // 4
        numbers.add(new Question("How is the number '5' signed?", List.of("Fist", "Palm sideways", "Spread hand", "Two fingers"), 2, videoPathNumbers)); // 5
        numbers.add(new Question("How is the number '6' signed?", List.of("Peace sign", "Thumb touches pinky", "Thumb touches ring", "Palm open"), 1, videoPathNumbers)); // 6
        numbers.add(new Question("How is the number '7' signed?", List.of("Index finger up", "Peace sign", "Thumb touches ring finger", "Palm open"), 2, videoPathNumbers)); // 7
        numbers.add(new Question("How is the number '8' signed?", List.of("Thumb touches middle finger", "Four fingers", "Peace sign", "Closed fist"), 0, videoPathNumbers)); // 8
        numbers.add(new Question("How is the number '9' signed?", List.of("Palm open", "Fist", "Thumb touches index finger", "Peace sign"), 2, videoPathNumbers)); // 9
        numbers.add(new Question("How is the number '10' signed?", List.of("Closed fist", "Peace sign", "Palm open", "Shake thumb up"), 3, videoPathNumbers)); // 10

// QUESTIONS
        List<Question> questions = new ArrayList<>();

        String videoPathHow = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.how;
        String videoPathHowMuch = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.howmuch;
        String videoPathWhat = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.what;
        String videoPathWhen = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.when;
        String videoPathWhich = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.which;
        String videoPathWho = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.who;
        String videoPathWhy = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.why;
        String videoPathYes = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.oo;
        String videoPathNo = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.hindi;

        questions.add(new Question("Which question word uses a circling index finger?", List.of("Where?/Saan", "What?/Ano", "Why?/Bakit", "When?/Kailan"), 3, videoPathWhen)); // When
        questions.add(new Question("Which question word wiggles the index finger?", List.of("Who?/Sino", "What?/Ano", "How?/Paano", "Where?/Saan"), 3, "where")); // Where (not in lesson list but you included earlier)
        questions.add(new Question("Which sign means 'Who?/Sino?'", List.of("Finger wiggles at chin", "Palm open", "Fist at chest", "Two fingers up"), 0, videoPathWho)); // Who
        questions.add(new Question("Which question word uses 'Y' hand from the forehead?", List.of("What?/Ano", "Why?/Bakit", "When?/Kailan", "How?/Paano"), 1, videoPathWhy)); // Why
        questions.add(new Question("Which is the opposite of 'Yes/Oo'?", List.of("Maybe", "What?", "No/Hindi", "How?"), 2, videoPathNo)); // No
        questions.add(new Question("Which sign rubs fingers together like money?", List.of("Why?", "Where?", "How much?/Magkano?", "Which?"), 2, videoPathHowMuch)); // How much
        questions.add(new Question("Which question word is signed by twisting hands together?", List.of("How?/Paano?", "When?", "Why?", "What?"), 0, videoPathHow)); // How
        questions.add(new Question("Which sign shows choices with thumbs?", List.of("Why?", "Who?", "Which?/Alin?", "When?"), 2, videoPathWhich)); // Which
        questions.add(new Question("Which sign is palms up shaking side to side?", List.of("Why?", "What?/Ano", "Where?", "Who?"), 1, videoPathWhat)); // What
        questions.add(new Question("Which are the two answer signs?", List.of("What and Why", "Yes and No", "When and Where", "How and Which"), 1, videoPathYes)); // Yes/No

// FOODS
        List<Question> foods = new ArrayList<>();

        String videoPathMilk = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.gatas;
        String videoPathHotdog = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.hotdog;
        String videoPathDrink = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.inumin;
        String videoPathEgg = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.itlog;
        String videoPathJuice = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.juice;
        String videoPathRice = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.kanin;
        String videoPathCoffee = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.kape;
        String videoPathSoda = "android.resource://" + requireContext().getPackageName() + "/" + R.raw.soda;

        foods.add(new Question("Which sign squeezes the fist open and closed?", List.of("Coffee", "Juice", "Hotdog", "Milk/Gatas"), 3, videoPathMilk)); // Milk
        foods.add(new Question("Which sign grinds fists together?", List.of("Rice", "Hotdog", "Coffee/Kape", "Soda"), 2, videoPathCoffee)); // Coffee
        foods.add(new Question("Which sign tilts a hand to the mouth?", List.of("Juice", "Rice", "Drink/Inumin", "Soda"), 2, videoPathDrink)); // Drink
        foods.add(new Question("Which sign cracks an egg?", List.of("Rice", "Juice", "Itlog/Egg", "Hotdog"), 2, videoPathEgg)); // Egg
        foods.add(new Question("Which sign peels like a banana (but item is hotdog)?", List.of("Juice", "Rice", "Hotdog", "Coffee"), 2, videoPathHotdog)); // Hotdog
        foods.add(new Question("Which sign scoops to the mouth?", List.of("Soup", "Juice", "Hotdog", "Rice/Kanin"), 3, videoPathRice)); // Rice
        foods.add(new Question("Which sign taps a finger into a fist opening?", List.of("Coffee", "Soda", "Juice", "Hotdog"), 1, videoPathSoda)); // Soda
        foods.add(new Question("Which sign uses J-hand to the mouth?", List.of("Milk", "Soda", "Juice", "Coffee"), 2, videoPathJuice)); // Juice
        foods.add(new Question("Which food is usually paired with 'itlog'?", List.of("Coffee", "Juice", "Hotdog", "Rice/Kanin"), 3, videoPathRice)); // Rice
        foods.add(new Question("Which two are both drinks?", List.of("Rice and Egg", "Hotdog and Coffee", "Milk and Rice", "Juice and Soda"), 3, videoPathJuice)); // Juice/Soda



        switch (subject) {
            case "Alphabet": return alphabet;
            case "Animals": return animals;
            case "Colors": return colors;
            case "Emotions": return emotions;
            case "Numbers": return numbers;
            case "Food and Drinks": return foods;
            case "Greetings": return greetings;
            case "Questions": return questions;
            case "Random":
            default:
                all.addAll(alphabet);
                all.addAll(animals);
                all.addAll(numbers);
                all.addAll(colors);
                all.addAll(emotions);
                all.addAll(foods);
                all.addAll(greetings);
                all.addAll(questions);
                Collections.shuffle(all);
                return all.subList(0, Math.min(10, all.size()));
        }
    }
}
