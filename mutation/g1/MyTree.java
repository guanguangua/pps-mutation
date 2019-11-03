package mutation.g1;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;

import javafx.util.Pair;


public class MyTree {
    // Entropy Hyperparameters
    public final boolean useEntropy = true;
    public final int supportBeforeEntropy = 20;
    public final double lambda = 1.0;
    public final double entropyThreshold = 0.8;

    private final ArrayList<Character> bases = new ArrayList<Character>(Arrays.asList('a', 'c', 'g', 't'));
    public String action;
    public int support;
    public ArrayList<ArrayList<Integer>> patternCounts;

    public double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public double normalizedEntropy(ArrayList<Integer> counts) {
        double smoothing = lambda / Math.sqrt(this.support);
        double entropy = 0.0;
        double Z = 0.0;
        for(int c : counts) {
            Z += c + smoothing;
        }
        for(int c : counts) {
            double p = (c + smoothing) / Z;
            if(p > 0) {
                entropy -= p * this.log2(p);
            }
        }

        return entropy / this.log2(4.0);
    }

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

    public String generatePattern(ArrayList<String>shortestPatterns, int endIdx) {
        String pattern = "";
        for(int i=0; i <= endIdx; i++) {
            pattern += shortestPatterns.get(i);
            if(i < endIdx) {
                pattern += ";";
            }
        }

        return pattern;
    }

    public Pair<String, Double> computeBestPattern(Set<String> missedGuesses) {
      ArrayList<String> shortestPatterns = new ArrayList<>();

      for(int positionIdx = 0; positionIdx < 10; positionIdx++) {
          ArrayList<Integer>charCounts = this.patternCounts.get(positionIdx);
          String posString = "";
          for(int charIdx = 0; charIdx < 4; charIdx++) {
              int charCount = charCounts.get(charIdx);
              if(charCount >= 0.2 * this.support) {
                  posString += this.bases.get(charIdx);
              }
          }

          shortestPatterns.add(posString);
      }

      ArrayList<Double>precisionScores = new ArrayList<>();
      ArrayList<Double>compactnessScores = new ArrayList<>();
      ArrayList<Double>entropies = new ArrayList<>();
      ArrayList<Integer>predictionCounts = new ArrayList<>();
      int lengthSum = 0;
      int precisionSum = 0;
      for(int i = 0; i < 10; i++) {
          String shortestPattern = shortestPatterns.get(i);
          int shortestLen = shortestPattern.length();
          predictionCounts.add(shortestLen);
          precisionSum += 4 - shortestLen;
          lengthSum += shortestLen;
          compactnessScores.add((40.0 - lengthSum) / 40.0);
          precisionScores.add(precisionSum / (4.0 * (i + 1)));
          entropies.add(this.normalizedEntropy(this.patternCounts.get(i)));
      }

      int bestEntropyIdx = -1;
      for(int e = 0; e < entropies.size(); e++) {
          if(entropies.get(e) <= entropyThreshold) {
              bestEntropyIdx = e;
          }
      }

      int bestPrecisionIdx = -1;
      double bestScore = 0.0;
      for(int i = 0; i < 10; i++) {
          double jointScore = (compactnessScores.get(i) + precisionScores.get(i)) / 2.0;
          if(jointScore >= bestScore) {
              bestPrecisionIdx = i;
              bestScore = jointScore;
          }
      }

      boolean chooseEntropyIdx = bestEntropyIdx > -1 && this.support > supportBeforeEntropy && useEntropy;
      int bestIdx = chooseEntropyIdx ? bestEntropyIdx : bestPrecisionIdx;
      int altIdx = chooseEntropyIdx ? bestPrecisionIdx : bestEntropyIdx;
      if(altIdx == -1) {
          altIdx = 0;
      }
      String bestPattern = this.generatePattern(shortestPatterns, bestIdx);
      String resultStr = bestPattern + "@" + this.action;
      if(missedGuesses.contains(resultStr)) {
          bestPattern = this.generatePattern(shortestPatterns, altIdx);
      }

      return new Pair(bestPattern, bestScore);
    }

    public void mergeTree(MyTree t, int alignIdx) {
        this.support += t.support;
        for(int positionIdx = alignIdx; positionIdx < 10; positionIdx++) {
            for(int charIdx = 0; charIdx < 4; charIdx++) {
                int newValue = (this.patternCounts.get(positionIdx).get(charIdx) +
                        t.patternCounts.get(positionIdx - alignIdx).get(charIdx));
                this.patternCounts.get(positionIdx).set(charIdx, newValue);
            }
        }
    }

    public boolean matchesCharAtIdx(Character c, int posIdx) {
        int charIdx = this.bases.indexOf(c);

        for(int i = 0; i < 4; i++) {
            int count = this.patternCounts.get(posIdx).get(i);
            if(i == charIdx) {
                if(count == 0) {
                    return false;
                }
            } else {
                if(count > 0) {
                    return false;
                }
            }
        }

        return true;
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
