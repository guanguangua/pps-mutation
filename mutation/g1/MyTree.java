package mutation.g1;

import java.util.Arrays;
import java.util.ArrayList;

import javafx.util.Pair;


public class MyTree {
    private final ArrayList<Character> bases = new ArrayList<Character>(Arrays.asList('a', 'c', 'g', 't'));
    public String action;
    public int support;
    public ArrayList<ArrayList<Integer>> patternCounts;

    public MyTree(String pattern, String action) {
        this.patternCounts = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            ArrayList<Integer>charCounts = new ArrayList<>();
            for(int j = 0; j < 4; j++) {
                charCounts.add(0);
            }
            this.patternCounts.add(charCounts);
        }

        this.action = action;
        this.addPattern(pattern);
    }

    public Pair<String, Double> computBestPattern() {
      ArrayList<String> shortestPatterns = new ArrayList<>();

      for(int positionIdx = 0; positionIdx < 10; positionIdx++) {
          ArrayList<Integer>charCounts = this.patternCounts.get(positionIdx);
          String posString = "";
          for(int charIdx = 0; charIdx < 4; charIdx++) {
              int charCount = charCounts.get(charIdx);
              if(charCount / (float) this.support > 0.1) {
                  posString += this.bases.get(charIdx);
              }
          }

          shortestPatterns.add(posString);
      }

      ArrayList<Double>precisionScores = new ArrayList<>();
      ArrayList<Double>compactnessScores = new ArrayList<>();
      int lengthSum = 0;
      int precisionSum = 0;
      for(int i = 0; i < 10; i++) {
          String shortestPattern = shortestPatterns.get(i);
          int shortestLen = shortestPattern.length();
          precisionSum += 4 - shortestLen;
          lengthSum += shortestLen;
          compactnessScores.add((40.0 - lengthSum) / 40.0);
          precisionScores.add(precisionSum / 40.0);
        }
      int bestIdx = 0;
      double bestScore = 0.0;
      ArrayList<Double> jointScores = new ArrayList<>();
      for(int i = 0; i < 10; i++) {
          double jointScore = (compactnessScores.get(i) + precisionScores.get(i)) / 2.0;
          if(jointScore >= bestScore) {
              bestIdx = i;
              bestScore = jointScore;
          }
      }

      String bestPattern = "";
      for(int i=0; i<= bestIdx; i++) {
          bestPattern += shortestPatterns.get(i);
          if(i < bestIdx) {
              bestPattern += ";";
          }
      }

      return new Pair(bestPattern, bestScore);
    }

    public void addPattern(String newPattern) {
        this.support += 1;
        for(int positionIdx = 0; positionIdx < newPattern.length(); positionIdx++) {
            int charIdx = this.bases.indexOf(newPattern.charAt(positionIdx));
            int newValue = this.patternCounts.get(positionIdx).get(charIdx) + 1;
            this.patternCounts.get(positionIdx).set(charIdx, newValue);
        }
    }
}
