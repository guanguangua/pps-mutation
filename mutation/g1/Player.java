package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

    private ArrayList<String> getPossiblePattern(String windowStr){
        ArrayList<String> pattern = new ArrayList<>();
        for (int i = 0; i < windowStr.length(); i++){
            for (int j = i; j < windowStr.length(); j++){
                pattern.add(windowStr.substring(i, j+1));
            }
        }
        return pattern;
    }

    private ArrayList<String> getPossibleActions2(String beforeMutation, String afterMutation, int startIdx) {
        ArrayList<String> actions = new ArrayList<>();
        if(beforeMutation.length() == 0) {
            actions.add("");
            return actions;
        }

        char after = afterMutation.charAt(0);

        ArrayList<String>prefixActionStrings = new ArrayList<String>();
        prefixActionStrings.add(Character.toString(after));

        for(int j=0; j < beforeMutation.length(); j++) {
            if(beforeMutation.charAt(j) == after) {
                prefixActionStrings.add(Integer.toString(j + startIdx));
            }
        }

        ArrayList<String> actionStrings = getPossibleActions2(
                beforeMutation.substring(1), afterMutation.substring(1), startIdx + 1);

        for(String prefixString : prefixActionStrings) {
            for(String actionString : actionStrings) {
                String fullString = prefixString + actionString;
                actions.add(fullString);
            }
        }

        return actions;
    }

    private ArrayList<String> getPossibleActions(String beforeMutation, String afterMutation) {
        int lastChangeIdx = -1;
        for(int i=0; i < beforeMutation.length(); i++) {
            if(beforeMutation.charAt(i) != afterMutation.charAt(i)) {
                lastChangeIdx = i;
            }
        }
        String beforeMutationSub = beforeMutation.substring(0, lastChangeIdx + 1);
        String afterMutationSub = afterMutation.substring(0, lastChangeIdx + 1);
        return getPossibleActions2(beforeMutationSub, afterMutationSub, 0);
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

    public mutation.g1.MyTree buildTree(String beforeMutation, String afterMutation) {
        String action = this.getAction(beforeMutation, afterMutation);

        ArrayList<String> patternConstraint = new ArrayList<>();
        for(int j = 0; j < beforeMutation.length(); j++) {
            patternConstraint.add(Character.toString(beforeMutation.charAt(j)));
        }

        mutation.g1.MyTree tree = new mutation.g1.MyTree(patternConstraint);
        tree.action = action;
        return tree;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        ArrayList<mutation.g1.MyTree>trees = new ArrayList<>();

        boolean isCorrect;
        Mutagen mutation = new Mutagen();

        for (int i = 0; i < 25; ++ i) {
            String genome = randomString();
            String mutated = console.Mutate(genome);
            int initialIdx = 0;

            ArrayList<ArrayList<Integer>> possibleWindows = this.getPossibleWindows(genome, mutated, m);

            if(possibleWindows.size() == 0) {
                System.out.println("No mutations possible.");
                continue;
            }

            if(i == 0 || trees.size() == 0) {
                initialIdx = 1;
                ArrayList<Integer> firstWindows = possibleWindows.get(0);
                for(int startIdx : firstWindows) {
                    String beforeString = getWrappedSubstring(startIdx, startIdx + 10, genome);
                    String afterString = getWrappedSubstring(startIdx, startIdx + 10, mutated);
                    trees.add(this.buildTree(beforeString, afterString));
                }
            }

            for(int j = initialIdx; j < possibleWindows.size(); j ++) {
                boolean [] supportedTrees = new boolean[trees.size()];
                ArrayList<String>candidateActions = new ArrayList<>();
                ArrayList<String>candidatePatterns = new ArrayList<>();
                ArrayList<mutation.g1.MyTree>prunedTrees = new ArrayList<>();

                ArrayList<Integer> otherWindows = possibleWindows.get(j);
                for(int startIdx : otherWindows) {
                    String beforeString = getWrappedSubstring(startIdx, startIdx + 10, genome);
                    String afterString = getWrappedSubstring(startIdx, startIdx + 10, mutated);
                    String action = this.getAction(beforeString, afterString);
                    candidateActions.add(action);
                    candidatePatterns.add(beforeString);
                    for(int z = 0; z < trees.size(); z++) {
                        if(action.equals(trees.get(z).action)) {
                            supportedTrees[z] = true;
                            trees.get(z).prune(beforeString);
                        }
                    }
                }

                for(int y = 0; y < supportedTrees.length; y++) {
                    if(supportedTrees[y]) {
                        prunedTrees.add(trees.get(y));
                    }
                }

                if(prunedTrees.size() == 0) {
                    for(int z = 0; z < candidateActions.size(); z++) {
                        String candidateAction = candidateActions.get(z);
                        String candidatePattern = candidatePatterns.get(z);
                        for(int p = 0; p < trees.size(); p++) {
                            String ta = trees.get(p).action;
                            if(candidateAction.contains(ta) || ta.contains(candidateAction)) {
                                trees.get(p).prune(candidatePattern);
                                String longerAction = ta.length() > candidateAction.length() ? ta : candidateAction;
                                trees.get(p).action = longerAction;
                                prunedTrees.add(trees.get(p));
                            }
                        }
                    }
                }
                // We have to guess if pruned went to 0
                if(prunedTrees.size() > 0) {
                    trees = prunedTrees;
                } else {
                    System.out.println("Couldn't find any matching trees.  Can't filter any of them.");
                }
            }

            String bestPattern = "";
            double bestScore = 0.0;
            String bestAction = "";
            for(mutation.g1.MyTree tree : trees) {
                Pair<String, Double> candidate  = tree.computeBestPattern();
                String candidatePattern = candidate.getKey();
                double candidateScore = candidate.getValue();

                if(candidateScore >= bestScore) {
                    bestScore = candidateScore;
                    bestPattern = candidatePattern;
                    bestAction = tree.action;
                }
            }

            mutation = new Mutagen();
            mutation.add(bestPattern, bestAction);
            isCorrect = console.Guess(mutation);
            System.out.println("Mutation -->" + bestPattern + "@" + bestAction);
            if(isCorrect) {
                System.out.println("Congrats: correct!");
                break;
            } else {
                System.out.println("Incorrect!");
            }
        }

        return mutation;
    }
}
