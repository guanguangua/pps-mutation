package mutation.g1;

import javafx.util.Pair;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;


public class MyTree {
    public String action;
    public ArrayList<String> basePattern;
    public ArrayList<ArrayList<String>> patterns;

    public String sorted(String x) {
        char[] chars = x.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    public HashSet<String> combinations(String x) {
        HashSet<String> possibleStrings = new HashSet<>();
        possibleStrings.add(x);
        for(int y = 0; y < x.length(); y++) {
            String substring = x.substring(0, y) + x.substring(y + 1);
            possibleStrings.addAll(combinations(substring));
        }

        return possibleStrings;
    }

    public Pair<String, Double> computeBestPattern() {
        ArrayList<Double>precisionScores = new ArrayList<>();
        ArrayList<Double>compactnessScores = new ArrayList<>();

        ArrayList<String>shortestStrings = new ArrayList<>();
        for(ArrayList<String> strings : this.patterns) {
            int minSize = 10000;
            String minString = "";
            for(String string : strings) {
                if(string.length() < minSize) {
                    minSize = string.length();
                    minString = string;
                }
            }
            shortestStrings.add(minString);
        }

        int lengthSum = 0;
        int precisionSum = 0;
        for(int i = 0; i < this.patterns.size(); i++) {
            String shortestPattern = shortestStrings.get(i);
            int shortestLen = shortestPattern.length();
            precisionSum += 4 - shortestLen;
            lengthSum += shortestLen;
            compactnessScores.add((40.0 - lengthSum) / 40.0);
            precisionScores.add(precisionSum / 40.0);
        }

        int bestIdx = 0;
        double bestScore = 0.0;
        ArrayList<Double> jointScores = new ArrayList<>();
        for(int i = 0; i < this.patterns.size(); i++) {
            double jointScore = (compactnessScores.get(i) + precisionScores.get(i)) / 2.0;

            if(jointScore >= bestScore) {
                bestIdx = i;
                bestScore = jointScore;
            }
        }

        String bestPattern = "";

        for(int i=0; i<= bestIdx; i++) {
            bestPattern += shortestStrings.get(i);
            if(i < bestIdx) {
                bestPattern += ";";
            }
        }

        return new Pair(bestPattern, bestScore);
    }

    public void prune(String newPattern) {
        ArrayList<ArrayList<String>> prunedPatterns = new ArrayList<>();

        for(int i=0; i < this.patterns.size(); i++) {
            ArrayList<String> prunedPattern = new ArrayList<>();
            for(String pattern : this.patterns.get(i)) {
                if(pattern.contains(Character.toString(newPattern.charAt(i)))) {
                    prunedPattern.add(pattern);
                }
            }
            prunedPatterns.add(prunedPattern);
        }

        this.patterns = prunedPatterns;
    }

    public MyTree(ArrayList<String> basePattern) {
        this.patterns = new ArrayList<>();
        this.basePattern = basePattern;
        String bases = "acgt";
        this.action = null;
        for(int i = 0; i < 10; i++) {
            ArrayList<String>possibleChoices = new ArrayList<>();
            // Every combination
            String thisChar = basePattern.get(i);
            String otherChars = "";
            for(int j = 0; j < bases.length(); j++) {
                String base = Character.toString(bases.charAt(j));
                if(! base.equals(thisChar)) {
                    otherChars += base;
                }
            }

            HashSet<String> combos = this.combinations(otherChars);
            for (String temp : combos) {
                String sorted = this.sorted(temp + thisChar);
                possibleChoices.add(sorted);
            }

            this.patterns.add(possibleChoices);
        }
    }
}
