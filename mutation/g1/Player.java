package mutation.g1;

import mutation.sim.Console;
import mutation.sim.Mutagen;

import java.lang.Math;
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

    public void mergeTrees(HashMap<String, MyTree> trees) {
        // Merge tree a into tree b iff a --> <x>;(acgt)@<y> and b --> <x>@<y>(acgt)
        ArrayList<String>actionsToRemove = new ArrayList<String>();

        for(Map.Entry<String, MyTree> entry : trees.entrySet()) {
            String action = entry.getKey();
            MyTree t = entry.getValue();
            for(Map.Entry<String, MyTree> otherEntry : trees.entrySet()) {
                String otherAction = otherEntry.getKey();
                MyTree otherT = otherEntry.getValue();

                if(otherAction.equals(action)) {
                    continue;
                }

                int alignIdx = otherAction.indexOf(action);
                // TODO figure out why I had this
//                boolean ambiguousAlignIdx = otherAction.length() > 1 && otherAction.substring(1).contains(action);
                boolean ambiguousAlignIdx = false;
                if(alignIdx == 0 && ! ambiguousAlignIdx) {
                    boolean isValid = true;
                    for(int i = action.length(); i < otherAction.length(); i++) {
                        isValid = t.matchesCharAtIdx(otherAction.charAt(i), i);
                        if(!isValid) {
                            break;
                        }
                    }

                    if(isValid) {
                        // merge entry into otherEntry
//                        System.out.println("Merging " + action + " into " + otherAction);
                        otherT.mergeTree(t, alignIdx);
                        actionsToRemove.add(action);
                    }
                }
            }
        }

        for(String action: actionsToRemove) {
            trees.remove(action);
        }
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

    public boolean consumes(Pair<String, String>p1, Pair<String, String> p2) {
        /*
        p1 consumes p2 if of the form: p1= a;<x>@a<y> and p2=<x>@<y>
         */

        List<String> pp1 = Arrays.asList(p1.getKey().split(";"));
        String pa1 = p1.getValue();

        List<String> pp2 = Arrays.asList(p2.getKey().split(";"));
        String pa2 = p2.getValue();

        int alignIdx = pa1.indexOf(pa2);
        if(alignIdx > 0) {
            for(int i = 0; i < alignIdx; i++) {
                if(i >= pp1.size() || !Character.toString(pa1.charAt(i)).equals(pp1.get(i))) {
                    return false;
                }
            }

            for(int i = alignIdx; i < pp1.size(); i++) {
                String base = pp1.get(i);
                if(i - alignIdx < pp2.size()) {
                    String compare = pp2.get(i - alignIdx);
                    if(! base.contains(compare)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public Pair<String, ArrayList<MyTree>> proposeAlphaNumericAction(String pattern, ArrayList<MyTree> trees) {
        HashMap<String, ArrayList<MyTree>> numericActionToTrees = new HashMap<>();
        int mostHits = -1;
        String mostPopularAction = null;
        for(MyTree t : trees) {
            String action = t.proposeAlphaNumericAction();
            boolean containsNumbers = action.matches(".*\\d.*");

            if(containsNumbers) {
                if(numericActionToTrees.containsKey(action)) {
                    numericActionToTrees.get(action).add(t);
                } else {
                    ArrayList<MyTree>tmp = new ArrayList<>();
                    tmp.add(t);
                    numericActionToTrees.put(action, tmp);
                }
                int n = numericActionToTrees.get(action).size();
                if(n >= mostHits) {
                    mostHits = n;
                    mostPopularAction = action;
                }
            }
        }

        if(mostPopularAction == null) {
            return new Pair(null, null);
        } else {
            return new Pair<>(mostPopularAction, numericActionToTrees.get(mostPopularAction));
        }
    }

    public Mutagen generateGuess(ArrayList<Pair<String, String>>guesses) {
        Mutagen m = new Mutagen();
        for(Pair p : guesses) {
            m.add((String) p.getKey(), (String) p.getValue());
        }
        return m;
    }

    @Override
    public Mutagen Play(Console console, int m) {
        HashMap<String,MyTree>trees = new HashMap<>();

        boolean isCorrect;
        ArrayList<Pair<String, String>>guesses = new ArrayList<>();
        Set<String>incorrectGuesses = new HashSet<String>();

        int numMutations = 0;

        for (int iter = 0; iter < 100; iter++) {
            String genome = randomString();
            String mutated = console.Mutate(genome);

            numMutations += m;

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
                        tree.addActionIndices(beforeMutation);
                    } else {
                        MyTree tree = new MyTree(beforeMutation, action);
                        trees.put(action, tree);
                    }
                }
            }

            this.mergeTrees(trees);
            int maxSupport = 0;
            for(MyTree t: trees.values()) {
                maxSupport = Math.max(t.support, maxSupport);
            }

            int supportThreshold = (int) Math.round(maxSupport * 0.8);
            guesses = new ArrayList<>();

            ArrayList<String> actionCandidates = new ArrayList<>();
            ArrayList<String> patternCandidates = new ArrayList<>();

            HashMap<String, ArrayList<MyTree>> patternToTrees = new HashMap<>();
            HashSet<String> repeatedPatterns = new HashSet<>();

            for(MyTree t: trees.values()) {
                String pattern = t.computeBestPattern(incorrectGuesses);

                if(patternToTrees.containsKey(pattern)) {
                    patternToTrees.get(pattern).add(t);
                    repeatedPatterns.add(pattern);
                } else {
                    ArrayList<MyTree> tmp = new ArrayList<>();
                    tmp.add(t);
                    patternToTrees.put(pattern, tmp);
                }

                if(t.support >= supportThreshold) {
                    actionCandidates.add(t.action);
                    patternCandidates.add(pattern);
                }
            }

            for(int cIdx = 0; cIdx < actionCandidates.size(); cIdx++) {
                boolean isSubset = false;
                for(int otherIdx = 0; otherIdx < actionCandidates.size(); otherIdx++) {
                    Pair<String, String> p1 = new Pair<>(patternCandidates.get(otherIdx), actionCandidates.get(otherIdx));
                    Pair<String, String> p2 = new Pair<>(patternCandidates.get(cIdx), actionCandidates.get(cIdx));
                    if(otherIdx != cIdx && this.consumes(p1, p2)) {
                        isSubset = true;
                        break;
                    }
                }
                if(!isSubset) {
                    guesses.add(new Pair(patternCandidates.get(cIdx), actionCandidates.get(cIdx)));
                }
            }

            ArrayList<Pair<String, String>>alphaNumericGuesses = new ArrayList<Pair<String, String>>();
            ArrayList<String>guessesToRemove = new ArrayList<>();

            for(String pattern : patternToTrees.keySet()) {
                ArrayList<MyTree> potentialNumericTrees = patternToTrees.get(pattern);
                if(potentialNumericTrees.size() > 1) {
                    Pair<String, ArrayList<MyTree>> tmp = this.proposeAlphaNumericAction(pattern, potentialNumericTrees);
                    String alphaAction = tmp.getKey();
                    ArrayList<MyTree>contributingTrees = tmp.getValue();
                    int combinedSupport = 0;
                    if(contributingTrees != null) {
                        for(MyTree ct : contributingTrees) {
                            combinedSupport += ct.support;
                        }
                        if(combinedSupport >= supportThreshold * 0.5) {
                            Pair<String, String>p = new Pair<>(pattern, alphaAction);
                            alphaNumericGuesses.add(p);
                            for(MyTree ct : contributingTrees) {
                                guessesToRemove.add(pattern + "@" + ct.action);
                            }
                        }
                    }
                }
            }

            String resultStr = "\n";
            guesses.addAll(alphaNumericGuesses);
            ArrayList<Pair<String, String>>finalGuesses = new ArrayList<>();
            for(int gIdx=0; gIdx < guesses.size(); gIdx++) {
                String guess = guesses.get(gIdx).getKey() + "@" + guesses.get(gIdx).getValue();
                if(! guessesToRemove.contains(guess)) {
                    resultStr += guess;
                    if(gIdx < guesses.size() - 1) {
                        resultStr += "\n";
                    }
                    finalGuesses.add(guesses.get(gIdx));
                }
            }

            isCorrect = console.Guess(this.generateGuess(finalGuesses));
            if(isCorrect) {
                System.out.println("Correct: " + resultStr);
                break;
            } else {
                System.out.println("Incorrect: " + resultStr);
                incorrectGuesses.add(resultStr);
            }
        }

        return this.generateGuess(guesses);
    }
}
