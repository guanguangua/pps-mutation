package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
import java.lang.reflect.Array;
import java.util.*;

import javafx.util.Pair;


public class Player extends mutation.sim.Player {
    private Random random;

    public Player() {
        random = new Random();
    }

    private String randomString() {
        char[] pool = {'a', 'c', 'g', 't'};
        String result = "";
        for (int i = 0; i < 1000; ++ i)
            result += pool[Math.abs(random.nextInt() % 4)];
        return result;
    }

    private String defaultString() {
        return "aaaaaaaaaaccccccccccggggggggggttttttttttacacacacacacacacacacagagagagagagagagagagatatatatatatatatatat" +
                "cgcgcgcgcgcgcgcgcgcgctctctctctctctctctctgtgtgtgtgtgtgtgtgtgtacgacgacgaacgacgacgaacgacgacgaacgacgacga" +
                "acgacgacgaacgacgacgaactactactaactactactaactactactaactactactaactactactaactactactaagtagtagtaagtagtagta" +
                "agtagtagtaagtagtagtaagtagtagtaagtagtagtacgtcgtcgtccgtcgtcgtccgtcgtcgtccgtcgtcgtccgtcgtcgtccgtcgtcgtc" +
                "acgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtac" +
                "acgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtacacgtacgtac" +
                "acgtacgtacacgtacgtacacgtacgtacacgtacgtacaaaaaaaaaaccccccccccggggggggggttttttttttacacacacacacacacacac" +
                "agagagagagagagagagagatatatatatatatatatatcgcgcgcgcgcgcgcgcgcgctctctctctctctctctctgtgtgtgtgtgtgtgtgtgt" +
                "acgacgacgaacgacgacgaacgacgacgaacgacgacgaacgacgacgaacgacgacgaactactactaactactactaactactactaactactacta" +
                "actactactaactactactaagtagtagtaagtagtagtaagtagtagtaagtagtagtaagtagtagtaagtagtagtacgtcgtcgtccgtcgtcgtc";
    }

    public Integer getWrappedIdx(int idx, int n) {
        if(idx < 0) {
            return n + idx;
        } else if(idx >= n) {
            return idx - n;
        } else {
            return idx;
        }
    }

    private ArrayList<ArrayList<Integer>> getPossibleWindows(String beforeMutation, String afterMutation, int m) {
        int n = beforeMutation.length();
        ArrayList<ArrayList<Integer>>possibleWindows = new ArrayList<>();
        int startIdx = 9999999;
        int endIdx = -9999999;
        Set<Integer>accountedFor = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            if (beforeMutation.charAt(i) != afterMutation.charAt(i) && ! accountedFor.contains(i)) {
                accountedFor.add(i);
                startIdx = Math.min(startIdx, i);
                endIdx = Math.max(endIdx, i);
                for(int windowIdx = i - 9; windowIdx <= i + 9; windowIdx++) {
                    int wrappedIdx = getWrappedIdx(windowIdx, n);
                    if (beforeMutation.charAt(wrappedIdx) != afterMutation.charAt(wrappedIdx)) {
                        accountedFor.add(wrappedIdx);
                        startIdx = Math.min(startIdx, windowIdx);
                        endIdx = Math.max(endIdx, windowIdx);
                    }
                }
                ArrayList<Integer>newW = new ArrayList<>();
                for(int windowStart = endIdx - 9; windowStart <= startIdx; windowStart++) {
                    newW.add(this.getWrappedIdx(windowStart, n));
                }
                possibleWindows.add(newW);
                startIdx = 9999999;
                endIdx = -9999999;
            }
        }

        return possibleWindows;
    }

    public String getWrappedSubstring(int s, int e, String string) {
        if(s < 0) {
            int realStart = getWrappedIdx(s, string.length());
            return string.substring(realStart) + string.substring(0, e);
        } else if (e >= string.length()) {
            int realEnd = getWrappedIdx(e, string.length());
            return string.substring(s) + string.substring(0, realEnd);
        } else {
            return string.substring(s, e);
        }
    }

    public String getAction(String beforeMutation, String afterMutation) {
        int lastChangeIdx = -1;
        for(int i = 0; i < afterMutation.length(); i++) {
            if(beforeMutation.charAt(i) != afterMutation.charAt(i)) {
                lastChangeIdx = i;
            }
        }

        String action = afterMutation.substring(0, lastChangeIdx + 1);
        return action;
    }

    public Mutagen generateGuess(String p, String a) {
        Mutagen m = new Mutagen();
        m.add(p, a);
        return m;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        HashMap<String,MyTree>trees = new HashMap<>();

        boolean isCorrect;
        String bestPattern = "";
        String bestAction = "";
        Set<String>incorrectGuesses = new HashSet<String>();

        for (int iter = 0; iter < 100; iter++) {
            String genome = randomString();
            String mutated = console.Mutate(genome);

            ArrayList<ArrayList<Integer>> possibleWindows = this.getPossibleWindows(genome, mutated, m);
            if(possibleWindows.size() == 0) {
                System.out.println("No mutations possible.");
                continue;
            }

            for(ArrayList<Integer> windowSet : possibleWindows) {
                for(int startIdx : windowSet) {
                    String beforeMutation = getWrappedSubstring(startIdx, startIdx + 10, genome);
                    String afterMutation = getWrappedSubstring(startIdx, startIdx + 10, mutated);
                    String action = this.getAction(beforeMutation, afterMutation);
                    if(trees.containsKey(action)) {
                        MyTree tree = trees.get(action);
                        tree.addPattern(beforeMutation);
                    } else {
                        MyTree tree = new MyTree(beforeMutation, action);
                        trees.put(action, tree);
                    }
                }
            }

            int maxSupport = 0;
            for(MyTree t: trees.values()) {
                maxSupport = Math.max(t.support, maxSupport);
            }

            double bestScore = -1;
            for(MyTree t: trees.values()) {
                if(t.support == maxSupport) {
                    Pair<String, Double> p = t.computeBestPattern(incorrectGuesses);
                    String pattern = p.getKey();
                    double score = p.getValue();
                    if(score >= bestScore) {
                        bestScore = score;
                        bestPattern = pattern;
                        bestAction = t.action;
                    }
                }
            }

            String resultStr = bestPattern + "@" + bestAction;
            isCorrect = console.Guess(this.generateGuess(bestPattern, bestAction));
            if(isCorrect) {
                System.out.println("Correct: " + resultStr);
                break;
            } else {
                System.out.println("Incorrect: " + resultStr);
                incorrectGuesses.add(resultStr);
            }
        }

        return this.generateGuess(bestPattern, bestAction);
    }
}
