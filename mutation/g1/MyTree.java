package mutation.g1;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;


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
    public ArrayList<ArrayList<Integer>> actionIndexCounts;

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

        this.actionIndexCounts = new ArrayList<>();
        for(int i = 0; i < action.length(); i++) {
            ArrayList<Integer>ac = new ArrayList<>();
            for(int j = 0; j < 10; j++) {
                ac.add(0);
            }
            this.actionIndexCounts.add(ac);
        }

        this.action = action;
        this.addPattern(pattern);
        this.addActionIndices(pattern);
    }

    public String proposeAlphaNumericAction() {
        String newAction = "";
        for(int i=0; i < this.action.length(); i++) {
            int maxActionIdx = -1;
            int maxCount = -1;
            for(int actionIdx=0; actionIdx < 10; actionIdx++) {
                int count = this.actionIndexCounts.get(i).get(actionIdx);
                if(count >= maxCount) {
                    maxActionIdx = actionIdx;
                    maxCount = count;
                }
            }

            if(maxCount >= Math.max(2.0, 0.8 * this.support)) {
                newAction += Integer.toString(maxActionIdx);
            } else {
                newAction += this.action.charAt(i);
            }
        }

        return newAction;
    }

    public void addActionIndices(String pattern) {
        ArrayList<ArrayList<Integer>> charIdxs = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            charIdxs.add(new ArrayList<>());
        }
        for(int posIdx = 0; posIdx < pattern.length(); posIdx++) {
            char c = pattern.charAt(posIdx);
            charIdxs.get(this.bases.indexOf(c)).add(posIdx);
        }

        for(int actionIdx = 0; actionIdx < this.action.length(); actionIdx++) {
            int actionCharIdx = this.bases.indexOf(this.action.charAt(actionIdx));
            ArrayList<Integer>indicesToAdd = charIdxs.get(actionCharIdx);
            for(int idx : indicesToAdd) {
                int currCount = this.actionIndexCounts.get(actionIdx).get(idx);
                this.actionIndexCounts.get(actionIdx).set(idx, currCount + 1);
            }
        }
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

    public String computeBestPattern(Set<String> missedGuesses) {
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

      return bestPattern;
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
